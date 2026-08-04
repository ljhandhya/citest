package io.github.modelscope.twinkle.transport;

import com.google.gson.JsonElement;
import java.util.Map;

/** 资源客户端使用的最小 HTTP 抽象。 */
public interface HttpTransport extends AutoCloseable {
    JsonElement get(String path, Map<String, ?> query);
    JsonElement post(String path, Map<String, ?> payload);
    JsonElement delete(String path);
    void setSessionId(String sessionId);
    String sessionId();

    @Override
    void close();
}
