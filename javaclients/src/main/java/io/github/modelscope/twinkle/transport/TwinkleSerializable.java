package io.github.modelscope.twinkle.transport;

import com.google.gson.JsonObject;

/** 需要按 Twinkle 特殊 JSON 字符串协议传输的值对象。 */
public interface TwinkleSerializable {
    JsonObject toTwinkleJson();
}
