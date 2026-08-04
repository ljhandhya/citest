package io.github.modelscope.twinkle.config;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Supplier;

/** 客户端不可变配置及其构建器。 */
public record ClientConfig(
    String apiBaseUrl,
    String routePrefix,
    Supplier<String> apiKeySupplier,
    Duration connectTimeout,
    Duration requestTimeout,
    Duration heartbeatInterval
) {
    public static Builder builder() {
        return new Builder();
    }

    /** 用于在构建前完成参数校验与默认值处理。 */
    public static final class Builder {

        private String baseUrl;
        private String routePrefix = "/twinkle";
        private Supplier<String> apiKeySupplier;
        private Duration connectTimeout = Duration.ofSeconds(30);
        private Duration requestTimeout = Duration.ofMinutes(10);
        private Duration heartbeatInterval = Duration.ofSeconds(10);

        public Builder baseUrl(String value) {
            this.baseUrl = value;
            return this;
        }

        public Builder routePrefix(String value) {
            this.routePrefix = value;
            return this;
        }

        public Builder apiKey(String value) {
            this.apiKeySupplier = () -> value;
            return this;
        }

        public Builder apiKeySupplier(Supplier<String> value) {
            this.apiKeySupplier = value;
            return this;
        }

        public Builder connectTimeout(Duration value) {
            this.connectTimeout = value;
            return this;
        }

        public Builder requestTimeout(Duration value) {
            this.requestTimeout = value;
            return this;
        }

        public Builder heartbeatInterval(Duration value) {
            this.heartbeatInterval = value;
            return this;
        }

        public ClientConfig build() {
            String server = normalizeBaseUrl(
                baseUrl == null ? System.getenv("TWINKLE_SERVER_URL") : baseUrl
            );
            String prefix = normalizePrefix(routePrefix);
            Supplier<String> supplier = apiKeySupplier == null
                ? () -> System.getenv().getOrDefault("TWINKLE_SERVER_TOKEN", "EMPTY_TOKEN")
                : apiKeySupplier;
            validateDuration(connectTimeout, "connectTimeout");
            validateDuration(requestTimeout, "requestTimeout");
            validateDuration(heartbeatInterval, "heartbeatInterval");
            String token = Objects.requireNonNull(supplier.get(), "apiKey 不能为空").trim();
            if (token.isEmpty()) {
                throw new IllegalArgumentException("apiKey 不能为空");
            }
            return new ClientConfig(
                server,
                prefix,
                supplier,
                connectTimeout,
                requestTimeout,
                heartbeatInterval
            );
        }

        private static String normalizeBaseUrl(String value) {
            String result = value == null || value.isBlank()
                ? "http://127.0.0.1:8000"
                : value.trim();
            result = result.replaceAll("/+$", "");
            return result.endsWith("/api/v1") ? result : result + "/api/v1";
        }

        private static String normalizePrefix(String value) {
            if (value == null || value.isBlank()) {
                return "";
            }
            String result = value.trim().replaceAll("/+$", "");
            if (!result.startsWith("/")) {
                throw new IllegalArgumentException("routePrefix 必须以 / 开头");
            }
            return result;
        }

        private static void validateDuration(Duration value, String name) {
            if (
                value == null || value.isZero() || value.isNegative()
            ) {
                throw new IllegalArgumentException(name + " 必须大于 0");
            }
        }
    }
}
