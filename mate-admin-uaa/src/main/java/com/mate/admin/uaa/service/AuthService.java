package com.mate.admin.uaa.service;

public interface AuthService {
    String login(String username, String password);
    void logout(Long userId);
    boolean userExists(Long userId);

    /** UAA 为新用户初始化认证资源（由 System 服务通过 Feign 触发） */
    void syncUserAuth(Long userId, String username);
}
