package com.mate.admin.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Configuration
@RefreshScope
public class SentinelRuleConfig {

    @Value("${sentinel.qps.login:40}")
    private double loginQps;
    @Value("${sentinel.qps.user-page:500}")
    private double userPageQps;
    @Value("${sentinel.qps.role-assign:50}")
    private double roleAssignQps;

    @PostConstruct
    public void initRules() {
        // 定义 API 分组
        Set<ApiDefinition> apis = new HashSet<>();
        apis.add(new ApiDefinition("login-api")
                .setPredicateItems(Collections.singleton(
                        new ApiPathPredicateItem()
                                .setPattern("/auth/login")
                                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_EXACT)
                )));
        apis.add(new ApiDefinition("user-page-api")
                .setPredicateItems(Collections.singleton(
                        new ApiPathPredicateItem()
                                .setPattern("/system/user/page")
                                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_EXACT)
                )));
        apis.add(new ApiDefinition("role-assign-api")
                .setPredicateItems(Collections.singleton(
                        new ApiPathPredicateItem()
                                .setPattern("/system/role/**")
                                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX)
                )));
        GatewayApiDefinitionManager.loadApiDefinitions(apis);

        // 按接口差异化限流，阈值均从配置读取
        Set<GatewayFlowRule> rules = new HashSet<>();
        rules.add(new GatewayFlowRule("login-api")
                .setCount(loginQps).setIntervalSec(1));
        rules.add(new GatewayFlowRule("user-page-api")
                .setCount(userPageQps).setIntervalSec(1));
        rules.add(new GatewayFlowRule("role-assign-api")
                .setCount(roleAssignQps).setIntervalSec(1));
        GatewayRuleManager.loadRules(rules);
        log.info("Sentinel 限流规则已加载，login: {}, user-page: {}, role-assign: {}",
                loginQps, userPageQps, roleAssignQps);

        // 自定义限流返回
        GatewayCallbackManager.setBlockHandler((exchange, t) ->
                ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("{\"code\":429,\"msg\":\"请求过于频繁，请稍后再试\"}"));
    }
}
