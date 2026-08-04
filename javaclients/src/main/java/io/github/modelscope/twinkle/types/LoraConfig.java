package io.github.modelscope.twinkle.types;

import com.google.gson.JsonObject;
import io.github.modelscope.twinkle.transport.TwinkleSerializable;

/** 与 Python 简化客户端一致的 LoRA 适配器配置。 */
public record LoraConfig(
    int rank,
    int loraAlpha,
    Object targetModules,
    double loraDropout,
    String bias,
    String taskType
)
    implements TwinkleSerializable {
    public LoraConfig {
        if (rank <= 0) {
            throw new IllegalArgumentException("rank 必须大于 0");
        }
    }

    /** 使用 Python 客户端相同默认值创建配置。 */
    public LoraConfig() {
        this(8, 32, "all-linear", 0.0, "none", null);
    }

    @Override
    public JsonObject toTwinkleJson() {
        JsonObject value = new JsonObject();
        value.addProperty("_TWINKLE_TYPE_", "LoraConfig");
        value.addProperty("r", rank);
        value.addProperty("lora_alpha", loraAlpha);
        value.add("target_modules", new com.google.gson.Gson().toJsonTree(targetModules));
        value.addProperty("lora_dropout", loraDropout);
        value.addProperty("bias", bias);
        if (taskType != null) {
            value.addProperty("task_type", taskType);
        }
        return value;
    }
}
