package io.github.modelscope.twinkle.exception;

/** 网络连接、超时或本地 HTTP 传输失败。 */
public final class TwinkleTransportException extends TwinkleException {

    public TwinkleTransportException(String message, Throwable cause) {
        super(message, cause);
    }
}
