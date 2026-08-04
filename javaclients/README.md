# Twinkle Java Client

Twinkle 训练服务的 Java 17 同步客户端。该项目面向国内开发者发布：使用中文文档、中文 JavaDoc 与中文源代码注释；Java API 名称和 HTTP 协议字段保持英文，以便与 Java 生态及 Twinkle 服务端兼容。

## 特性

- 自动创建并维护服务端会话心跳，客户端关闭时自动停止。
- 提供模型训练、LoRA、采样、训练任务、检查点、数据集、DataLoader 与输入处理器 API。
- 所有网络失败和服务端失败均转换为携带上下文的运行时异常。
- 通过 `TWINKLE_SERVER_URL` 与 `TWINKLE_SERVER_TOKEN` 读取默认服务地址和认证令牌。

## 引入依赖

发布到 Maven Central 后，可在项目中引入：

```xml
<dependency>
  <groupId>io.github.modelscope</groupId>
  <artifactId>twinkle-client-java</artifactId>
  <version>1.0.0</version>
</dependency>
```

在发布前，可直接将本项目导入 IntelliJ IDEA 作为 Maven 项目。

## 最小示例

```java
try (TwinkleClient client = TwinkleClient.builder()
    .baseUrl(System.getenv("TWINKLE_SERVER_URL"))
    .apiKey(System.getenv("TWINKLE_SERVER_TOKEN"))
    .build()) {
  if (!client.healthCheck()) {
    throw new IllegalStateException("Twinkle 服务不可用");
  }

  var model = client.models().open("Qwen/Qwen3.6-27B");
  model.addAdapter("default", new LoraConfig(8, 16, "all-linear", 0.01, "none", null));
  model.setLoss("CrossEntropyLoss");
  model.setOptimizer("Adam", Map.of("lr", 1e-4));
}
```

## 数据加载与训练

```java
var dataset = client.processors().dataset(
    DatasetKind.DATASET,
    Map.of("dataset_meta", DatasetMeta.of("ms://your-dataset")));
dataset.setTemplate("Qwen3_5Template", Map.of("model_id", "Qwen/Qwen3.6-27B"));
dataset.encode(false, Map.of("batched", true));

var loader = client.processors().dataLoader(dataset.processorId(), Map.of("batch_size", 4));
for (var batch : loader) {
  model.forwardBackward(batch);
  model.clipGradAndStep(1.0, 2);
}
```

## 配置

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `TWINKLE_SERVER_URL` | `http://127.0.0.1:8000` | 服务根地址；客户端自动补充 `/api/v1`。 |
| `TWINKLE_SERVER_TOKEN` | `EMPTY_TOKEN` | 服务端认证令牌。 |
| `routePrefix` | `/twinkle` | 会话和训练任务管理 API 的路由前缀。 |

请勿将真实令牌、内网地址、数据集本地路径写入源码、Issue 或提交历史。

## 与旧版原型的迁移

| 原型 API | 新 API |
| --- | --- |
| `new TwinkleClient(url, token)` | `TwinkleClient.builder().baseUrl(url).apiKey(token).build()` |
| `createModel(id)` | `client.models().open(id)` |
| `createSampler(id)` | `client.samplers().open(id)` |
| `createDataset(type, args)` | `client.processors().dataset(type, args)` |
| `Map<String, Object>` 响应 | 稳定字段使用 record，开放字段使用 `JsonObject` / `JsonElement`。 |

## 许可证

本项目采用 [Apache License 2.0](LICENSE)。
