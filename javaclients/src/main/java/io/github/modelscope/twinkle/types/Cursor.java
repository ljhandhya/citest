package io.github.modelscope.twinkle.types;

/** 列表接口返回的分页游标。 */
public record Cursor(
    int limit,
    int offset,
    int totalCount
) {
}
