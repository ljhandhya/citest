package io.github.modelscope.twinkle.transport;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.modelscope.twinkle.config.ClientConfig;
import io.github.modelscope.twinkle.exception.TwinkleIterationExhaustedException;
import io.github.modelscope.twinkle.exception.TwinkleProtocolException;
import io.github.modelscope.twinkle.exception.TwinkleServiceException;
import io.github.modelscope.twinkle.exception.TwinkleTransportException;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/** 基于 OkHttp 的同步传输实现，集中处理请求头和异常。 */
public final class OkHttpTransport implements HttpTransport {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final ClientConfig config;
    private final OkHttpClient client;
    private final TwinkleJsonCodec codec = new TwinkleJsonCodec();
    private final String requestId = UUID.randomUUID().toString();
    private volatile String sessionId;

    public OkHttpTransport(ClientConfig config) {
        this.config = config;
        this.client = new OkHttpClient.Builder()
            .connectTimeout(config.connectTimeout())
            .readTimeout(config.requestTimeout())
            .writeTimeout(config.requestTimeout())
            .callTimeout(config.requestTimeout())
            .build();
    }

    @Override
    public JsonElement get(String path, Map<String, ?> query) {
        HttpUrl.Builder url = url(path).newBuilder();
        if (query != null) {
            query.forEach((key, value) -> {
                if (value != null) {
                    url.addQueryParameter(key, String.valueOf(value));
                }
            });
        }
        return execute(new Request.Builder().url(url.build()).get());
    }

    @Override
    public JsonElement post(String path, Map<String, ?> payload) {
        String body = codec.gson().toJson(codec.encode(payload));
        return execute(new Request.Builder().url(url(path)).post(RequestBody.create(body, JSON)));
    }

    @Override
    public JsonElement delete(String path) {
        return execute(new Request.Builder().url(url(path)).delete());
    }

    @Override
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String sessionId() {
        return sessionId;
    }

    @Override
    public void close() {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
    }

    private HttpUrl url(String path) {
        String normalized = path.startsWith("/") ? path : "/" + path;
        HttpUrl parsed = HttpUrl.parse(config.apiBaseUrl() + normalized);
        if (parsed == null) {
            throw new IllegalArgumentException("无效的请求地址: " + path);
        }
        return parsed;
    }

    private JsonElement execute(Request.Builder request) {
        String token = config.apiKeySupplier().get();
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("apiKey 不能为空");
        }
        String authorization = "Bearer " + token;
        request
            .header("Authorization", authorization)
            .header("Twinkle-Authorization", authorization)
            .header("x-request-id", requestId)
            .header("X-Ray-Serve-Request-Id", requestId)
            .header("serve_multiplexed_model_id", requestId)
            .header("Serve-Multiplexed-Model-Id", requestId);
        if (sessionId != null && !sessionId.isBlank()) {
            request.header("X-Twinkle-Session-Id", sessionId);
        }
        try (Response response = client.newCall(request.build()).execute()) {
            String text = response.body() == null ? "" : response.body().string();
            URI endpoint = response.request().url().uri();
            if (!response.isSuccessful()) {
                String detail = detail(text);
                if (response.code() == 410) {
                    throw new TwinkleIterationExhaustedException(endpoint, requestId, detail);
                }
                throw new TwinkleServiceException(response.code(), endpoint, requestId, detail);
            }
            try {
                return text.isBlank() ? new JsonObject() : JsonParser.parseString(text);
            } catch (RuntimeException error) {
                throw new TwinkleProtocolException(
                    "服务端响应不是合法 JSON: " + endpoint,
                    error
                );
            }
        } catch (TwinkleServiceException | TwinkleProtocolException error) {
            throw error;
        } catch (IOException error) {
            throw new TwinkleTransportException(
                "HTTP 请求失败: " + request.build().url(),
                error
            );
        }
    }

    private String detail(String text) {
        try {
            JsonElement json = JsonParser.parseString(text);
            if (json.isJsonObject() && json.getAsJsonObject().has("detail")) {
                return json.getAsJsonObject().get("detail").getAsString();
            }
        } catch (RuntimeException ignored) {
            /* 响应正文不是 JSON 时直接使用原文。 */
        }
        return text.isBlank() ? "服务端未返回错误详情" : text;
    }
}
