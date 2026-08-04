package io.github.modelscope.twinkle.types;

import com.google.gson.JsonObject;
import java.util.List;

/** 服务端支持能力的固定描述。 */
public record ServerCapabilities(
    List<SupportedModel> supportedModels,
    JsonObject extensions
) {
}
