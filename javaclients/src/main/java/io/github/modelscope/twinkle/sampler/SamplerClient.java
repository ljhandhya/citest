package io.github.modelscope.twinkle.sampler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.modelscope.twinkle.transport.HttpTransport;
import io.github.modelscope.twinkle.transport.JsonValue;
import io.github.modelscope.twinkle.types.LoraConfig;
import io.github.modelscope.twinkle.types.SampleRequest;
import io.github.modelscope.twinkle.types.SampleResult;
import io.github.modelscope.twinkle.types.SampledSequence;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 单个远程采样器的同步操作接口。 */
public final class SamplerClient {

    private final HttpTransport transport;
    private final String basePath;
    private String adapterName;

    SamplerClient(HttpTransport transport, String modelId) {
        if (modelId == null || modelId.isBlank()) {
            throw new IllegalArgumentException("modelId 不能为空");
        }
        this.transport = transport;
        String normalized = stripScheme(modelId);
        this.basePath =
            "/sampler/" +
            URLEncoder.encode(normalized, StandardCharsets.UTF_8).replace("+", "%20") +
            "/twinkle";
        post("/create", Map.of());
    }

    public SamplerClient useAdapter(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("adapterName 不能为空");
        }
        adapterName = value;
        return this;
    }

    public JsonObject addAdapter(String name, LoraConfig config) {
        JsonObject result = post(
            "/add_adapter_to_sampler",
            Map.of("adapter_name", name, "config", config)
        ).getAsJsonObject();
        adapterName = name;
        return result;
    }

    public List<SampleResult> sample(SampleRequest request) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("inputs", request.inputs().stream().map(JsonValue::new).toList());
        data.put("sampling_params", request.samplingParams());
        data.put("adapter_name", request.adapterName());
        data.put("num_samples", request.numSamples());
        if (request.adapterUri() != null) {
            data.put("adapter_uri", request.adapterUri());
        }
        JsonArray samples = post("/sample", data).getAsJsonObject().getAsJsonArray("samples");
        List<SampleResult> results = new ArrayList<>();
        for (JsonElement sample : samples) {
            results.add(parseResult(sample.getAsJsonObject()));
        }
        return List.copyOf(results);
    }

    public void setTemplate(String templateClass, String adapter, Map<String, ?> options) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("template_cls", templateClass);
        data.put("adapter_name", adapter == null ? "" : adapter);
        if (options != null) {
            data.putAll(options);
        }
        post("/set_template", data);
    }

    public void applyPatch(String patchClass) {
        post(
            "/apply_patch",
            Map.of("patch_cls", patchClass, "adapter_name", adapterName == null ? "" : adapterName)
        );
    }

    private JsonElement post(String endpoint, Map<String, ?> data) {
        return transport.post(basePath + endpoint, data);
    }

    private static SampleResult parseResult(JsonObject value) {
        List<SampledSequence> sequences = new ArrayList<>();
        for (JsonElement item : value.getAsJsonArray("sequences")) {
            JsonObject sequence = item.getAsJsonObject();
            List<Integer> tokens = new ArrayList<>();
            for (JsonElement token : sequence.getAsJsonArray("tokens")) {
                tokens.add(token.getAsInt());
            }
            sequences.add(
                new SampledSequence(
                    string(sequence, "stop_reason"),
                    List.copyOf(tokens),
                    string(sequence, "decoded"),
                    sequence
                )
            );
        }
        return new SampleResult(List.copyOf(sequences));
    }

    private static String string(JsonObject value, String key) {
        return value.has(key) && !value.get(key).isJsonNull()
            ? value.get(key).getAsString()
            : null;
    }

    private static String stripScheme(String value) {
        int index = value.indexOf("://");
        return index >= 0 ? value.substring(index + 3) : value;
    }
}
