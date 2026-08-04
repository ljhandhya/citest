package io.github.modelscope.twinkle.exception;

/** 服务端响应不是预期 JSON 协议格式。 */
public final class TwinkleProtocolException extends TwinkleException {

    public TwinkleProtocolException(String message, Throwable cause) {
        super(message, cause);
    }
}
