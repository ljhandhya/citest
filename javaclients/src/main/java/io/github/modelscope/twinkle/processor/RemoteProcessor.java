package io.github.modelscope.twinkle.processor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.modelscope.twinkle.transport.HttpTransport;
import java.util.LinkedHashMap;
import java.util.Map;

/** 所有远程处理器共享的调用封装。 */
abstract class RemoteProcessor {

    protected final HttpTransport transport;
    protected final String processorId;

    RemoteProcessor(HttpTransport transport, String processorId) {
        this.transport = transport;
        this.processorId = processorId;
    }

    public String processorId() {
        return processorId;
    }

    protected JsonElement call(String function, Map<String, ?> arguments) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("processor_id", processorId);
        payload.put("function", function);
        if (arguments != null) {
            payload.putAll(arguments);
        }
        JsonObject response = transport.post("/processor/twinkle/call", payload).getAsJsonObject();
        return response.has("result") ? response.get("result") : response;
    }
}
