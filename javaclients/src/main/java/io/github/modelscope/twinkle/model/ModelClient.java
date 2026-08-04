package io.github.modelscope.twinkle.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.modelscope.twinkle.transport.HttpTransport;
import io.github.modelscope.twinkle.transport.JsonValue;
import io.github.modelscope.twinkle.types.LoraConfig;
import io.github.modelscope.twinkle.types.SaveResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** 单个远程训练模型的同步操作接口。 */
public final class ModelClient {

    private final HttpTransport transport;
    private final String modelId;
    private final String basePath;
    private String adapterName;

    ModelClient(HttpTransport transport, String modelId) {
        if (modelId == null || modelId.isBlank()) {
            throw new IllegalArgumentException("modelId 不能为空");
        }
        this.transport = transport;
        this.modelId = stripScheme(modelId);
        this.basePath = "/model/" + pathSegment(this.modelId) + "/twinkle";
        post("/create", Map.of());
    }

    public String modelId() {
        return modelId;
    }

    public String adapterName() {
        return adapterName;
    }

    public ModelClient useAdapter(String name) {
        adapterName = require(name, "adapterName");
        return this;
    }

    public void addAdapter(String name, LoraConfig config) {
        addAdapter(name, config, Map.of());
    }

    public void addAdapter(String name, LoraConfig config, Map<String, ?> options) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("adapter_name", require(name, "adapterName"));
        payload.put("config", config);
        payload.putAll(options == null ? Map.of() : options);
        post("/add_adapter_to_model", payload);
        adapterName = name;
    }

    public JsonElement forward(JsonElement inputs) {
        return result(post("/forward", withAdapter(Map.of("inputs", new JsonValue(inputs)))));
    }

    public JsonElement forwardOnly(JsonElement inputs) {
        return result(post("/forward_only", withAdapter(Map.of("inputs", new JsonValue(inputs)))));
    }

    public JsonElement forwardBackward(JsonElement inputs) {
        return result(
            post("/forward_backward", withAdapter(Map.of("inputs", new JsonValue(inputs))))
        );
    }

    public void backward() {
        post("/backward", withAdapter(Map.of()));
    }

    public double calculateLoss() {
        return result(post("/calculate_loss", withAdapter(Map.of()))).getAsDouble();
    }

    public JsonObject calculateMetric(boolean training) {
        return result(
            post("/calculate_metric", withAdapter(Map.of("is_training", training)))
        ).getAsJsonObject();
    }

    public void setLoss(String lossClass) {
        post("/set_loss", withAdapter(Map.of("loss_cls", require(lossClass, "lossClass"))));
    }

    public void setOptimizer(String optimizerClass, Map<String, ?> options) {
        post(
            "/set_optimizer",
            withAdapter(
                merge(Map.of("optimizer_cls", require(optimizerClass, "optimizerClass")), options)
            )
        );
    }

    public void setLrScheduler(String schedulerClass, Map<String, ?> options) {
        post(
            "/set_lr_scheduler",
            withAdapter(
                merge(Map.of("scheduler_cls", require(schedulerClass, "schedulerClass")), options)
            )
        );
    }

    public void step() {
        post("/step", withAdapter(Map.of()));
    }

    public void zeroGrad() {
        post("/zero_grad", withAdapter(Map.of()));
    }

    public void lrStep() {
        post("/lr_step", withAdapter(Map.of()));
    }

    public String clipGradNorm(double maxGradNorm, int normType) {
        return result(
            post(
                "/clip_grad_norm",
                withAdapter(Map.of("max_grad_norm", maxGradNorm, "norm_type", normType))
            )
        ).getAsString();
    }

    public void clipGradAndStep(double maxGradNorm, int normType) {
        post(
            "/clip_grad_and_step",
            withAdapter(Map.of("max_grad_norm", maxGradNorm, "norm_type", normType))
        );
    }

    public void setTemplate(String templateClass, Map<String, ?> options) {
        post(
            "/set_template",
            withAdapter(
                merge(
                    Map.of(
                        "template_cls",
                        require(templateClass, "templateClass"),
                        "model_id",
                        modelId
                    ),
                    options
                )
            )
        );
    }

    public void setProcessor(String processorClass, Map<String, ?> options) {
        post(
            "/set_processor",
            withAdapter(
                merge(Map.of("processor_cls", require(processorClass, "processorClass")), options)
            )
        );
    }

    public void addMetric(String metricClass, Boolean training) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("metric_cls", require(metricClass, "metricClass"));
        if (training != null) {
            data.put("is_training", training);
        }
        post("/add_metric", withAdapter(data));
    }

    public void applyPatch(String patchClass) {
        post("/apply_patch", withAdapter(Map.of("patch_cls", require(patchClass, "patchClass"))));
    }

    public JsonObject stateDict() {
        return result(post("/get_state_dict", withAdapter(Map.of()))).getAsJsonObject();
    }

    public String trainConfigs() {
        return result(post("/get_train_configs", withAdapter(Map.of()))).getAsString();
    }

    public SaveResponse save(String name, boolean saveOptimizer) {
        JsonObject value = post(
            "/save",
            withAdapter(Map.of("name", require(name, "name"), "save_optimizer", saveOptimizer))
        ).getAsJsonObject();
        return new SaveResponse(string(value, "twinkle_path"), string(value, "checkpoint_dir"));
    }

    public void load(String name, boolean loadOptimizer) {
        post(
            "/load",
            withAdapter(Map.of("name", require(name, "name"), "load_optimizer", loadOptimizer))
        );
    }

    public JsonObject resumeFromCheckpoint(String name, boolean resumeOnlyModel) {
        return result(
            post(
                "/resume_from_checkpoint",
                withAdapter(
                    Map.of("name", require(name, "name"), "resume_only_model", resumeOnlyModel)
                )
            )
        ).getAsJsonObject();
    }

    private JsonElement post(String endpoint, Map<String, ?> payload) {
        return transport.post(basePath + endpoint, payload);
    }

    private Map<String, Object> withAdapter(Map<String, ?> values) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.putAll(values);
        if (adapterName != null) {
            result.put("adapter_name", adapterName);
        }
        return result;
    }

    private static Map<String, Object> merge(Map<String, ?> first, Map<String, ?> second) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.putAll(first);
        if (second != null) {
            result.putAll(second);
        }
        return result;
    }

    private static JsonElement result(JsonElement value) {
        return value.isJsonObject() && value.getAsJsonObject().has("result")
            ? value.getAsJsonObject().get("result")
            : value;
    }

    private static String stripScheme(String value) {
        int index = value.indexOf("://");
        return index >= 0 ? value.substring(index + 3) : value;
    }

    private static String pathSegment(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static String require(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " 不能为空");
        }
        return value;
    }

    private static String string(JsonObject value, String name) {
        return value.has(name) && !value.get(name).isJsonNull()
            ? value.get(name).getAsString()
            : null;
    }
}
