package io.github.modelscope.twinkle.types;

import com.google.gson.JsonObject;

/** 检查点摘要；服务端新增字段保留在 raw 中。 */
public record Checkpoint(
    String checkpointId,
    String checkpointType,
    String twinklePath,
    JsonObject raw
) {}
