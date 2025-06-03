package com.example.ogani.service;

import com.example.ogani.entity.User;
import com.example.ogani.model.request.ChangePasswordRequest;
import com.example.ogani.model.request.CreateUserRequest;
import com.example.ogani.model.request.UpdateProfileRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserService {
    
    void register(CreateUserRequest request);
    User createUser(CreateUserRequest request);
    User getUserByUsername(String username);
    User getUserById(long id);
    List<User> getList();

    Page<User> getListWithPagination(Pageable pageable, String search);

    User updateUser(UpdateProfileRequest request);

    void changePassword(ChangePasswordRequest request);

    void deleteUser(long id);

    void toggleUserStatus(long id);

    @Transactional
    User updateUserById(Long id, UpdateProfileRequest request);
}
