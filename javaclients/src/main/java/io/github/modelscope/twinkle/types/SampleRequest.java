package io.github.modelscope.twinkle.types;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** 采样请求参数。 */
public record SampleRequest(
    List<JsonElement> inputs,
    Map<String, ?> samplingParams,
    String adapterName,
    String adapterUri,
    int numSamples
) {
    public SampleRequest {
        Objects.requireNonNull(inputs, "inputs 不能为空");
        if (numSamples <= 0) throw new IllegalArgumentException("numSamples 必须大于 0");
        adapterName = adapterName == null ? "" : adapterName;
    }
}
