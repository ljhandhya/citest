package io.github.modelscope.twinkle.types;

import com.google.gson.JsonObject;

/** 服务端支持的基础模型。 */
public record SupportedModel(
    String modelName,
    JsonObject extensions
) {
}
