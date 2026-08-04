package io.github.modelscope.twinkle;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * 新版客户端的服务联调测试。
 *
 * <p>仅在同时设置 {@code TWINKLE_SERVER_URL} 和 {@code TWINKLE_SERVER_TOKEN} 时执行；
 * 未设置时会自动跳过，避免默认构建访问网络。</p>
 */
class TwinkleClientTest {

    /** 验证服务健康检查和会话创建。 */
    @Test
    void 应能创建会话并访问健康检查接口() {
        try (TwinkleClient client = createClientOrSkip()) {
            assertNotNull(client.sessionId());
            assertNotNull(client.healthCheck());
        }
    }

    /** 验证服务能力和容量接口可返回 JSON 对象。 */
    @Test
    void 应能读取服务能力和容量信息() {
        try (TwinkleClient client = createClientOrSkip()) {
            assertNotNull(client.serverCapabilities());
            assertNotNull(client.capacityInfo());
        }
    }

    /** 验证训练任务列表 API 的新版调用方式。 */
    @Test
    void 应能查询训练任务列表() {
        try (TwinkleClient client = createClientOrSkip()) {
            var page = client.trainingRuns().list(10, 0, false);
            assertNotNull(page.runs());
            assertNotNull(page.cursor());
        }
    }

    /** 根据环境变量构建客户端；缺少联调配置时跳过测试。 */
    private static TwinkleClient createClientOrSkip() {
        String baseUrl = System.getenv("TWINKLE_SERVER_URL");
        String token = System.getenv("TWINKLE_SERVER_TOKEN");
        Assumptions.assumeTrue(
            baseUrl != null && !baseUrl.isBlank(),
            "未设置 TWINKLE_SERVER_URL，跳过服务联调测试"
        );
        Assumptions.assumeTrue(
            token != null && !token.isBlank(),
            "未设置 TWINKLE_SERVER_TOKEN，跳过服务联调测试"
        );
        return TwinkleClient.builder()
            .baseUrl(baseUrl)
            .apiKey(token)
            .sessionMetadata(Map.of("client", "twinkle-client-java-test"))
            .build();
    }
}
