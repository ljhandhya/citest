package io.github.modelscope.twinkle.transport;

import com.google.gson.JsonElement;
import java.util.Objects;

/** 显式传递原始 JSON 的包装类型，避免被再次转换为字符串。 */
public record JsonValue(JsonElement value) {
    public JsonValue {
        Objects.requireNonNull(value, "value 不能为空");
    }
}
