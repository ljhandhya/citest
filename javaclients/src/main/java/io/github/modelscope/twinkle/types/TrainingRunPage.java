package io.github.modelscope.twinkle.types;

import com.google.gson.JsonObject;
import java.util.List;

/** 训练任务分页响应。 */
public record TrainingRunPage(
    List<TrainingRun> runs,
    Cursor cursor,
    JsonObject extensions
) {
}
