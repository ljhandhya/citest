package io.github.modelscope.twinkle.types;

import com.google.gson.JsonObject;

/** 删除检查点后的服务端确认信息。 */
public record DeleteCheckpointResult(
    boolean success,
    String message,
    JsonObject extensions
) {
}
