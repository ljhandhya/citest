package io.github.modelscope.twinkle.types;

import com.google.gson.JsonObject;

/** 训练任务摘要；扩展字段保留在 raw 中。 */
public record TrainingRun(
    String trainingRunId,
    String baseModel,
    String modelOwner,
    JsonObject raw
) {}
