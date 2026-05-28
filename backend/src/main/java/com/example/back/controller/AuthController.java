package com.example.back.controller;

import com.example.back.dto.LoginRequest;
import com.example.back.dto.RegisterRequest;
import com.example.back.dto.ForgotPasswordRequest;
import com.example.back.dto.ForgotPasswordTeacherRequest;
import com.example.back.dto.UpdatePasswordRequest;
import com.example.back.dto.UpdateUsernameRequest;
import com.example.back.dto.UpdateAvatarRequest;
import com.example.back.dto.UpdatePhoneRequest;
import com.example.back.dto.Result;

import com.example.back.entity.User;
import com.example.back.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterRequest request) {
        try {
            userService.register(request);
            return Result.success("注册成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<User> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.login(request);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/updateUsername")
    public Result<String> updateUsername(@RequestBody UpdateUsernameRequest request) {
        try {
            userService.updateUsername(request.getUserId(), request.getNewUsername(), request.getRole());
            return Result.success("昵称修改成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/updatePassword")
    public Result<String> updatePassword(@RequestBody UpdatePasswordRequest request) {
        try {
            userService.updatePassword(request.getUserId(), request.getOldPassword(), request.getNewPassword(), request.getRole());
            return Result.success("密码重置成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/forgotPassword")
    public Result<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            userService.forgotPasswordByStudentNo(request.getStudentNo(), request.getNewPassword());
            return Result.success("密码重置成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/forgotPasswordTeacher")
    public Result<String> forgotPasswordTeacher(@RequestBody ForgotPasswordTeacherRequest request) {
        try {
            userService.forgotPasswordByStaffNo(request.getStaffNo(), request.getNewPassword());
            return Result.success("密码重置成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/updateAvatar")
    public Result<String> updateAvatar(@RequestBody UpdateAvatarRequest request) {
        try {
            userService.updateAvatar(request.getUserId(), request.getAvatar(), request.getRole());
            return Result.success("头像修改成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/updatePhone")
    public Result<String> updatePhone(@RequestBody UpdatePhoneRequest request) {
        try {
            userService.updatePhone(request.getUserId(), request.getPhone(), request.getRole());
            return Result.success("手机号修改成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}