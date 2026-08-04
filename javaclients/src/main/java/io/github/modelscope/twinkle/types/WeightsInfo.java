package io.github.modelscope.twinkle.types;

import com.google.gson.JsonObject;

/** 权重所属训练任务的元数据。 */
public record WeightsInfo(
    String trainingRunId,
    String baseModel,
    String modelOwner,
    boolean isLora,
    Integer loraRank,
    JsonObject extensions
) {}
