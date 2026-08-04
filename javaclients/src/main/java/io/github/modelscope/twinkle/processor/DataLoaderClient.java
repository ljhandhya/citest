package io.github.modelscope.twinkle.processor;

import com.google.gson.JsonElement;
import io.github.modelscope.twinkle.exception.TwinkleIterationExhaustedException;
import io.github.modelscope.twinkle.transport.HttpTransport;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/** 支持 Java for-each 的远程数据加载器。 */
public final class DataLoaderClient extends RemoteProcessor implements Iterable<JsonElement> {

    DataLoaderClient(HttpTransport transport, String id) {
        super(transport, id);
    }

    public int length() {
        return call("__len__", Map.of()).getAsInt();
    }

    public JsonElement setProcessor(String processorClass, Map<String, ?> options) {
        return call(
            "set_processor",
            options == null
                ? Map.of("processor_cls", processorClass)
                : merge(processorClass, options)
        );
    }

    public JsonElement skipConsumedSamples(int count) {
        return call("skip_consumed_samples", Map.of("consumed_train_samples", count));
    }

    public JsonElement state() {
        return call("get_state", Map.of());
    }

    @Override
    public Iterator<JsonElement> iterator() {
        call("__iter__", Map.of());
        return new Iterator<>() {
            private boolean exhausted;

            @Override
            public boolean hasNext() {
                return !exhausted;
            }

            @Override
            public JsonElement next() {
                try {
                    return call("__next__", Map.of());
                } catch (TwinkleIterationExhaustedException error) {
                    exhausted = true;
                    throw new NoSuchElementException(error.getMessage());
                }
            }
        };
    }

    private static Map<String, Object> merge(String processorClass, Map<String, ?> options) {
        java.util.LinkedHashMap<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("processor_cls", processorClass);
        result.putAll(options);
        return result;
    }
}
