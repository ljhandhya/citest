package io.github.modelscope.twinkle.session;

import com.google.gson.JsonElement;
import io.github.modelscope.twinkle.transport.HttpTransport;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/** 创建会话并管理后台心跳，关闭时不会再发起心跳。 */
public final class SessionManager implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(SessionManager.class.getName());
    private final HttpTransport transport;
    private final String routePrefix;
    private final ScheduledExecutorService executor;
    private final String sessionId;

    public SessionManager(
        HttpTransport transport,
        String routePrefix,
        Duration interval,
        Map<String, ?> metadata,
        String existingSessionId
    ) {
        this.transport = transport;
        this.routePrefix = routePrefix;
        this.sessionId = existingSessionId == null || existingSessionId.isBlank()
            ? create(metadata)
            : existingSessionId;
        transport.setSessionId(sessionId);
        this.executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "TwinkleSessionHeartbeat");
            thread.setDaemon(true);
            return thread;
        });
        long delay = interval.toMillis();
        executor.scheduleWithFixedDelay(this::heartbeat, delay, delay, TimeUnit.MILLISECONDS);
    }

    public String sessionId() {
        return sessionId;
    }

    private String create(Map<String, ?> metadata) {
        JsonElement response = transport.post(
            routePrefix + "/create_session",
            Map.of("metadata", metadata)
        );
        return response.getAsJsonObject().get("session_id").getAsString();
    }

    private void heartbeat() {
        try {
            transport.post(routePrefix + "/session_heartbeat", Map.of("session_id", sessionId));
        } catch (RuntimeException error) {
            LOG.log(Level.WARNING, "Twinkle 会话心跳失败", error);
        }
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
