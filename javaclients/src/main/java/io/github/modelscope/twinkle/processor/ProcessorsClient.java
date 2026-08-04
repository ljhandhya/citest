package io.github.modelscope.twinkle.processor;

import com.google.gson.JsonObject;
import io.github.modelscope.twinkle.transport.HttpTransport;
import io.github.modelscope.twinkle.types.DatasetKind;
import java.util.LinkedHashMap;
import java.util.Map;

/** 创建远程数据集、加载器和输入处理器。 */
public final class ProcessorsClient {

    private final HttpTransport transport;

    public ProcessorsClient(HttpTransport transport, String ignoredRoutePrefix) {
        this.transport = transport;
    }

    public DatasetClient dataset(DatasetKind kind, Map<String, ?> options) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("processor_type", "dataset");
        data.put("class_type", kind.serverClassName());
        if (options != null) {
            data.putAll(options);
        }
        return new DatasetClient(transport, create(data), kind);
    }

    public DataLoaderClient dataLoader(String datasetProcessorId, Map<String, ?> options) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("processor_type", "dataloader");
        data.put("class_type", "DataLoader");
        data.put("dataset", datasetProcessorId);
        if (options != null) {
            data.putAll(options);
        }
        return new DataLoaderClient(transport, create(data));
    }

    public InputProcessorClient inputProcessor(Map<String, ?> options) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("processor_type", "processor");
        data.put("class_type", "InputProcessor");
        if (options != null) {
            data.putAll(options);
        }
        return new InputProcessorClient(transport, create(data));
    }

    private String create(Map<String, ?> data) {
        JsonObject response = transport.post("/processor/twinkle/create", data).getAsJsonObject();
        return response.get("processor_id").getAsString();
    }
}
