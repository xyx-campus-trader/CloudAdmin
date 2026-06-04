package com.mate.admin.api.feign;

import com.mate.admin.api.common.Result;
import com.mate.admin.api.dto.UserRegisterDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "mate-admin-uaa", path = "/auth")
public interface UaaFeignClient {

    @GetMapping("/user/{userId}/exists")
    Result<Boolean> checkUserExists(@PathVariable("userId") Long userId);

    /** 跨服务调用：通知 UAA 为新用户初始化认证资源 */
    @PostMapping("/user/sync")
    Result<Void> syncUserAuth(@RequestBody UserRegisterDTO dto);
}
