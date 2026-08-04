package io.github.modelscope.twinkle.processor;

import com.google.gson.JsonElement;
import io.github.modelscope.twinkle.transport.HttpTransport;
import io.github.modelscope.twinkle.types.DatasetKind;
import io.github.modelscope.twinkle.types.DatasetMeta;
import java.util.LinkedHashMap;
import java.util.Map;

/** 远程数据集操作接口。 */
public final class DatasetClient extends RemoteProcessor {

    private final DatasetKind kind;

    DatasetClient(HttpTransport transport, String id, DatasetKind kind) {
        super(transport, id);
        this.kind = kind;
    }

    public DatasetKind kind() {
        return kind;
    }

    public JsonElement setTemplate(String templateFunction, Map<String, ?> options) {
        return call("set_template", merge(values("template_func", templateFunction), options));
    }

    public JsonElement encode(boolean addGenerationPrompt, Map<String, ?> options) {
        return call("encode", merge(Map.of("add_generation_prompt", addGenerationPrompt), options));
    }

    public JsonElement check(Map<String, ?> options) {
        return call("check", options);
    }

    public JsonElement castColumn(String column, boolean decode) {
        return call("cast_column", Map.of("column", column, "decode", decode));
    }

    public JsonElement map(
        String function,
        DatasetMeta meta,
        Map<String, ?> initArgs,
        Map<String, ?> options
    ) {
        return call(
            "map",
            merge(
                values("preprocess_func", function, "dataset_meta", meta, "init_args", initArgs),
                options
            )
        );
    }

    public JsonElement filter(
        String function,
        DatasetMeta meta,
        Map<String, ?> initArgs,
        Map<String, ?> options
    ) {
        return call(
            "filter",
            merge(
                values("filter_func", function, "dataset_meta", meta, "init_args", initArgs),
                options
            )
        );
    }

    public JsonElement addDataset(DatasetMeta meta, Map<String, ?> options) {
        return call("add_dataset", merge(Map.of("dataset_meta", meta), options));
    }

    public JsonElement mixDataset(boolean interleave) {
        return call("mix_dataset", Map.of("interleave", interleave));
    }

    public JsonElement saveAs(
        String outputPath,
        String format,
        int batchSize,
        String mode,
        Map<String, ?> options
    ) {
        return call(
            "save_as",
            merge(
                values(
                    "output_path",
                    outputPath,
                    "format",
                    format,
                    "batch_size",
                    batchSize,
                    "mode",
                    mode
                ),
                options
            )
        );
    }

    public JsonElement flushSave() {
        return call("flush_save", Map.of());
    }

    public JsonElement getItem(int index) {
        return call("__getitem__", Map.of("idx", index));
    }

    public int length() {
        return call("__len__", Map.of()).getAsInt();
    }

    public JsonElement packDataset() {
        return call("pack_dataset", Map.of());
    }

    private static Map<String, Object> merge(Map<String, ?> first, Map<String, ?> second) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.putAll(first);
        if (second != null) {
            result.putAll(second);
        }
        return result;
    }

    private static Map<String, Object> values(Object... pairs) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int index = 0; index < pairs.length; index += 2) {
            result.put(
                (String) pairs[index],
                pairs[index + 1]
            );
        }
        return result;
    }
}
