package io.github.modelscope.twinkle.types;

import java.util.List;

/** 单个输入对应的一组采样结果。 */
public record SampleResult(
    List<SampledSequence> sequences
) {
}
