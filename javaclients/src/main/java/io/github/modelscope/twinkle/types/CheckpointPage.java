package io.github.modelscope.twinkle.types;

import com.google.gson.JsonObject;
import java.util.List;

/** 检查点分页响应。 */
public record CheckpointPage(
    List<Checkpoint> checkpoints,
    Cursor cursor,
    JsonObject extensions
) {
}
