package io.github.modelscope.twinkle.exception;

import java.net.URI;

/** HTTP 410，表示服务端远程迭代器已耗尽。 */
public final class TwinkleIterationExhaustedException extends TwinkleServiceException {

    public TwinkleIterationExhaustedException(URI endpoint, String requestId, String detail) {
        super(410, endpoint, requestId, detail);
    }
}
