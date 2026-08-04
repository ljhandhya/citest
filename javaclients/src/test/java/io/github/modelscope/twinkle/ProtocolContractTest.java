package io.github.modelscope.twinkle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.modelscope.twinkle.types.DatasetMeta;
import io.github.modelscope.twinkle.types.LoraConfig;
import java.util.Map;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;

/** 与 Python 简化客户端保持网络协议一致的离线测试。 */
class ProtocolContractTest {

    /** 管理类接口必须携带 /twinkle 路由前缀。 */
    @Test
    void 管理接口应使用Twinkle路由前缀() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.start();
            try (
                TwinkleClient client = TwinkleClient.builder()
                    .baseUrl(server.url("/").toString())
                    .apiKey("token")
                    .existingSessionId("session-1")
                    .build()
            ) {
                server.enqueue(new MockResponse().setBody("{}"));
                client.serverCapabilities();
                assertEquals(
                    "/api/v1/twinkle/get_server_capabilities",
                    server.takeRequest().getPath()
                );
            }
        }
    }

    /** DatasetMeta 必须保留 Python 客户端支持的切片与内存数据字段。 */
    @Test
    void 数据集元数据应完整序列化() {
        var meta = new DatasetMeta(
            "",
            "default",
            "train",
            DatasetMeta.range(10, 20, 2),
            Map.of("text", "你好")
        );
        var json = meta.toTwinkleJson();
        assertEquals("DatasetMeta", json.get("_TWINKLE_TYPE_").getAsString());
        assertEquals("range", json.getAsJsonObject("data_slice").get("_slice_type_").getAsString());
        assertTrue(json.has("data"));
    }

    /** LoRA 配置必须使用服务端所需的字段名。 */
    @Test
    void LoRA配置应使用Python协议字段名() {
        var json = new LoraConfig(16, 32, "all-linear", 0.1, "none", null).toTwinkleJson();
        assertEquals(16, json.get("r").getAsInt());
        assertEquals(32, json.get("lora_alpha").getAsInt());
        assertEquals("all-linear", json.get("target_modules").getAsString());
    }
}
