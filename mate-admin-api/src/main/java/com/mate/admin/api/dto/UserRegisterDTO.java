package com.mate.admin.api.dto;

import lombok.Data;

/**
 * 跨服务用户注册 DTO —— System 调用 UAA 同步认证信息
 */
@Data
public class UserRegisterDTO {
    private Long userId;
    private String username;
}
