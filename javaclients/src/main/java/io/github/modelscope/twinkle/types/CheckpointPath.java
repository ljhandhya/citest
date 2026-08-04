package io.github.modelscope.twinkle.types;

import com.google.gson.JsonObject;

/** 检查点标识符对应的本地路径和 Twinkle 路径。 */
public record CheckpointPath(
    String path,
    String twinklePath,
    String trainingRunId,
    String checkpointType,
    String checkpointId,
    JsonObject extensions
) {}
