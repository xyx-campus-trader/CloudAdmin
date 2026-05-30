package com.mate.admin.uaa.service;

public interface AuthService {
    String login(String username, String password);
    void logout(Long userId);
    boolean userExists(Long userId);
}
