package io.github.modelscope.twinkle.types;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.modelscope.twinkle.transport.TwinkleSerializable;
import java.util.List;

/** 远程数据集的定位信息。 */
public record DatasetMeta(
    String datasetId,
    String subsetName,
    String split,
    Object dataSlice,
    Object data
)
    implements TwinkleSerializable {
    public DatasetMeta {
        if (
            (datasetId == null || datasetId.isBlank()) && data == null
        ) {
            throw new IllegalArgumentException("datasetId 和 data 不能同时为空");
        }
    }

    public static DatasetMeta of(String datasetId) {
        return new DatasetMeta(datasetId, "default", "train", null, null);
    }

    /** 创建与 Python range 等价的数据切片。 */
    public static JsonObject range(int start, int stop, int step) {
        if (step == 0) {
            throw new IllegalArgumentException("step 不能为 0");
        }
        JsonObject value = new JsonObject();
        value.addProperty("_slice_type_", "range");
        value.addProperty("start", start);
        value.addProperty("stop", stop);
        value.addProperty("step", step);
        return value;
    }

    /** 创建由下标列表构成的数据切片。 */
    public static JsonObject indices(List<Integer> values) {
        JsonObject value = new JsonObject();
        value.addProperty("_slice_type_", "list");
        JsonArray array = new JsonArray();
        values.forEach(array::add);
        value.add("values", array);
        return value;
    }

    @Override
    public JsonObject toTwinkleJson() {
        JsonObject value = new JsonObject();
        value.addProperty("_TWINKLE_TYPE_", "DatasetMeta");
        if (datasetId != null) {
            value.addProperty("dataset_id", datasetId);
        }
        if (subsetName != null) {
            value.addProperty("subset_name", subsetName);
        }
        if (split != null) {
            value.addProperty("split", split);
        }
        if (dataSlice != null) {
            value.add("data_slice", new Gson().toJsonTree(dataSlice));
        }
        if (data != null) {
            value.add("data", new Gson().toJsonTree(data));
        }
        return value;
    }
}
