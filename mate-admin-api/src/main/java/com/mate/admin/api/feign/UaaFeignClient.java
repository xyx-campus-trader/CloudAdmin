package com.mate.admin.api.feign;

import com.mate.admin.api.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "mate-admin-uaa", path = "/auth")
public interface UaaFeignClient {

    @GetMapping("/user/{userId}/exists")
    Result<Boolean> checkUserExists(@PathVariable("userId") Long userId);
}
