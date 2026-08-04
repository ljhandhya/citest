package io.github.modelscope.twinkle.types;

/** 保存模型或采样器后返回的路径信息。 */
public record SaveResponse(
    String twinklePath,
    String checkpointDir
) {
}
