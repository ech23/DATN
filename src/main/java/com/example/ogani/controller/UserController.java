package com.example.ogani.controller;

import com.example.ogani.entity.Blog;
import com.example.ogani.exception.BadRequestException;
import com.example.ogani.exception.NotFoundException;
import com.example.ogani.model.request.CreateUserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.ogani.entity.User;
import com.example.ogani.model.request.ChangePasswordRequest;
import com.example.ogani.model.request.UpdateProfileRequest;
import com.example.ogani.model.response.MessageResponse;
import com.example.ogani.service.UserService;

import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@RestController
@RequestMapping("/api/user")
//@CrossOrigin(origins = "*",maxAge = 3600)
public class UserController {

    @Autowired
    private UserService userService;
    

    @GetMapping("/")
    @Operation(summary="Lấy ra user bằng username")
    public ResponseEntity<User> getUser(@RequestParam("username") String username){
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") long id){
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    @GetMapping("/all")
    @Operation(summary="Lấy tất cả danh sách user với phân trang")
    public ResponseEntity<Page<User>> getList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<User> users = userService.getListWithPagination(pageable, search);
        return ResponseEntity.ok(users);
    }
    @DeleteMapping("/delete/{id}")
    @Operation(summary="Xóa user theo ID")
    public ResponseEntity<?> deleteUser(@PathVariable long id){
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new MessageResponse("User deleted successfully"));
        } catch (NotFoundException e) {
            return ResponseEntity.status(404).body(new MessageResponse(e.getMessage()));
        } catch (BadRequestException e) {
            return ResponseEntity.status(400).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new MessageResponse("Internal server error: " + e.getMessage()));
        }
    }
    @PutMapping("/update")
    @Operation(summary="Cập nhật thông tin user")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request){
        try {
            userService.updateUser(request);
            return ResponseEntity.ok(new MessageResponse("User profile updated successfully"));
        }catch (Exception e){
            return ResponseEntity.status(500).body(new MessageResponse(e.getMessage()));
        }

    }
    @PutMapping("/update/{id}")
    @Operation(summary="cập nhât thông tin user theo id")
    public ResponseEntity<User> updateUser(@PathVariable long id, @RequestBody UpdateProfileRequest request){
        User user = userService.updateUserById(id, request);
        return ResponseEntity.ok(user);
    }
     @PutMapping("/password")
     @Operation(summary="Thay đổi mật khẩu")
     public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request){
         userService.changePassword(request);
         return ResponseEntity.ok(new MessageResponse("Change Password Success!"));
     }

    @PutMapping("/{id}/status")
    @Operation(summary="Khóa/Mở khóa tài khoản user")
    public ResponseEntity<?> toggleUserStatus(@PathVariable long id) {
        try {
            userService.toggleUserStatus(id);
            return ResponseEntity.ok(new MessageResponse("User status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new MessageResponse("Error updating user status: " + e.getMessage()));
        }
    }
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request){
        try{
            userService.createUser(request);
            return ResponseEntity.ok(new MessageResponse("User created successfully"));
        }catch (Exception e){
            return ResponseEntity.status(500).body(new MessageResponse("Error creating user: " + e.getMessage()));
        }
    }
}
