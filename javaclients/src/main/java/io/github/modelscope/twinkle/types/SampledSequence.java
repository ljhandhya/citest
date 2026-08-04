package io.github.modelscope.twinkle.types;

import com.google.gson.JsonElement;
import java.util.List;

/** 单条采样序列。 */
public record SampledSequence(
    String stopReason,
    List<Integer> tokens,
    String decoded,
    JsonElement raw
) {}
