package com.mate.admin.system.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;

@Slf4j
@Component
public class AlertUtil {

    @Resource
    private RestTemplate restTemplate;

    @Value("${alert.webhook.url:}")
    private String webhookUrl;

    private static AlertUtil INSTANCE;

    @PostConstruct
    public void init() {
        INSTANCE = this;
    }

    /**
     * 发送 CRITICAL 级别告警——补偿删除失败等需要人工介入的场景。
     * 如果 webhook 未配置，仅记录本地日志。
     */
    public static void critical(String title, String detail) {
        String msg = String.format(
            "[CRITICAL] %s | time=%s | detail=%s",
            title, LocalDateTime.now(), detail
        );
        log.error(msg);

        if (INSTANCE == null || INSTANCE.webhookUrl.isEmpty()) {
            return;
        }

        try {
            String body = String.format(
                "{\"msgtype\":\"text\",\"text\":{\"content\":\"%s\"}}",
                msg.replace("\"", "\\\"")
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            INSTANCE.restTemplate.postForEntity(
                INSTANCE.webhookUrl,
                new HttpEntity<>(body, headers),
                String.class
            );
        } catch (Exception e) {
            log.error("告警推送失败", e);
        }
    }
}
