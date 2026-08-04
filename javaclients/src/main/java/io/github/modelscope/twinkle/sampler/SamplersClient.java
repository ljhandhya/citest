package io.github.modelscope.twinkle.sampler;

import io.github.modelscope.twinkle.transport.HttpTransport;

/** 用于打开服务端采样器的工厂。 */
public final class SamplersClient {

    private final HttpTransport transport;

    public SamplersClient(HttpTransport transport, String ignoredRoutePrefix) {
        this.transport = transport;
    }

    public SamplerClient open(String modelId) {
        return new SamplerClient(transport, modelId);
    }
}
