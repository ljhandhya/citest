package io.github.modelscope.twinkle.types;

import com.google.gson.JsonObject;

/** 服务端 LoRA 容量信息。 */
public record CapacityInfo(
    int maxLoras,
    int usedLoras,
    int freeLoras,
    JsonObject extensions
) {
}
