package io.github.modelscope.twinkle.exception;

import java.net.URI;

/** 服务端返回非成功状态码时抛出的异常。 */
public class TwinkleServiceException extends TwinkleException {

    private final int statusCode;
    private final URI endpoint;
    private final String requestId;
    private final String serviceDetail;

    public TwinkleServiceException(int statusCode, URI endpoint, String requestId, String detail) {
        super(detail);
        this.statusCode = statusCode;
        this.endpoint = endpoint;
        this.requestId = requestId;
        this.serviceDetail = detail;
    }

    public int statusCode() {
        return statusCode;
    }

    public URI endpoint() {
        return endpoint;
    }

    public String requestId() {
        return requestId;
    }

    public String serviceDetail() {
        return serviceDetail;
    }
}
