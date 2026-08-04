package io.github.modelscope.twinkle.model;

import io.github.modelscope.twinkle.transport.HttpTransport;

/** 用于打开服务端训练模型的工厂。 */
public final class ModelsClient {

    private final HttpTransport transport;

    public ModelsClient(HttpTransport transport, String ignoredRoutePrefix) {
        this.transport = transport;
    }

    public ModelClient open(String modelId) {
        return new ModelClient(transport, modelId);
    }
}
