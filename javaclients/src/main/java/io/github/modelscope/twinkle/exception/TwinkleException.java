package io.github.modelscope.twinkle.exception;

/** Twinkle 客户端所有运行时异常的基类。 */
public class TwinkleException extends RuntimeException {

    public TwinkleException(String message) {
        super(message);
    }

    public TwinkleException(String message, Throwable cause) {
        super(message, cause);
    }
}
