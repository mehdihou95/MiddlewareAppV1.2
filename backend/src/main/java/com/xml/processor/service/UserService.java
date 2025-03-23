package com.xml.processor.service;

import com.xml.processor.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserService {
    User createUser(User user);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    Optional<User> getUserById(Long id);
    Optional<User> getUserByUsername(String username);
    Optional<User> getUserByEmail(String email);
    Page<User> getAllUsers(Pageable pageable);
    Page<User> searchUsers(String searchTerm, Pageable pageable);
    Page<User> getUsersByStatus(boolean enabled, Pageable pageable);
    Page<User> getLockedUsers(Pageable pageable);
    void changePassword(Long userId, String oldPassword, String newPassword);
    void resetPassword(String email);
    void unlockAccount(Long userId);
    void incrementFailedLoginAttempts(String username);
    void resetFailedLoginAttempts(String username);
    boolean isAccountLocked(String username);
} 