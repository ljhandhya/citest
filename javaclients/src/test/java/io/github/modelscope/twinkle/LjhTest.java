package io.github.modelscope.twinkle;

import com.google.gson.JsonElement;
import io.github.modelscope.twinkle.types.DatasetKind;
import io.github.modelscope.twinkle.types.DatasetMeta;
import io.github.modelscope.twinkle.types.LoraConfig;
import java.util.Map;

/**
 * 可在 IntelliJ IDEA 中直接运行的完整 LoRA 训练示例。
 *
 * <p>
 * 所有运行参数均来自环境变量，禁止将令牌、内网地址或本机数据路径写入本类。
 * </p>
 */
public final class LjhTest {

    private LjhTest() {}

    /** 启动一次端到端的远程 LoRA 训练。 */
    public static void main(String[] args) {
        String baseUrl = requiredEnv("TWINKLE_SERVER_URL");
        String token = requiredEnv("TWINKLE_SERVER_TOKEN");
        String modelId = env("TWINKLE_BASE_MODEL", "Qwen/Qwen3.6-27B");
        String datasetId = requiredEnv("TWINKLE_DATASET_ID");
        String template = env("TWINKLE_TEMPLATE", "Qwen3_5Template");
        int batchSize = Integer.parseInt(env("TWINKLE_BATCH_SIZE", "4"));
        int epochs = Integer.parseInt(env("TWINKLE_EPOCHS", "1"));
        double learningRate = Double.parseDouble(env("TWINKLE_LEARNING_RATE", "0.0001"));

        try (
            TwinkleClient client = TwinkleClient.builder().baseUrl(baseUrl).apiKey(token).build()
        ) {
            if (!client.healthCheck()) {
                throw new IllegalStateException("Twinkle 服务健康检查失败");
            }
            //测试一下修改 java 的文件触发的 ci

            var capabilities = client.serverCapabilities();
            System.out.println("服务支持的模型：");
            capabilities
                .supportedModels()
                .forEach(supportedModel -> System.out.println("- " + supportedModel.modelName()));

            var capacity = client.capacityInfo();
            System.out.printf(
                "LoRA 容量：总数=%d，已用=%d，空闲=%d%n",
                capacity.maxLoras(),
                capacity.usedLoras(),
                capacity.freeLoras()
            );

            var existingRuns = client.trainingRuns().list(10, 0, false);
            System.out.printf(
                "当前可见训练任务：%d 个%n",
                existingRuns.cursor().totalCount()
            );

            var dataset = client
                .processors()
                .dataset(DatasetKind.DATASET, Map.of("dataset_meta", DatasetMeta.of(datasetId)));
            dataset.setTemplate(template, Map.of("model_id", modelId));
            dataset.encode(false, Map.of("batched", true));

            var dataLoader = client
                .processors()
                .dataLoader(dataset.processorId(), Map.of("batch_size", batchSize));
            var model = client.models().open(modelId);
            model.addAdapter(
                "default",
                new LoraConfig(8, 16, "all-linear", 0.01, "none", null),
                Map.of("gradient_accumulation_steps", 1)
            );
            model.setTemplate(template, Map.of());
            model.setProcessor("InputProcessor", Map.of("padding_side", "right"));
            model.setLoss("CrossEntropyLoss");
            model.setOptimizer("Adam", Map.of("lr", learningRate));

            for (int epoch = 0; epoch < epochs; epoch++) {
                int step = 0;
                for (JsonElement batch : dataLoader) {
                    model.forwardBackward(batch);
                    model.clipGradAndStep(1.0, 2);
                    if (step % 10 == 0) System.out.printf(
                        "第 %d 轮，第 %d 步，指标：%s%n",
                        epoch + 1,
                        step,
                        model.calculateMetric(true)
                    );
                    step++;
                }
                System.out.printf("第 %d 轮训练完成，共 %d 步%n", epoch + 1, step);
            }

            var saved = model.save("twinkle-java-final", true);
            System.out.println("检查点 Twinkle 路径：" + saved.twinklePath());
            if (saved.checkpointDir() != null) {
                System.out.println("检查点本地目录：" + saved.checkpointDir());
            }
        }
    }

    /** 获取必填环境变量。 */
    private static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new IllegalStateException(
            "请设置环境变量 " + name
        );
        return value;
    }

    /** 获取带默认值的环境变量。 */
    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
