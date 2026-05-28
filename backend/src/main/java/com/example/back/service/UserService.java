package com.example.back.service;

import com.example.back.dto.LoginRequest;
import com.example.back.dto.RegisterRequest;
import com.example.back.entity.User;
import com.example.back.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    private void validatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        if (password.length() < 8) {
            throw new RuntimeException("密码长度不能少于8位");
        }
        if (password.length() > 20) {
            throw new RuntimeException("密码长度不能超过20位");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new RuntimeException("密码必须包含大写字母");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new RuntimeException("密码必须包含小写字母");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new RuntimeException("密码必须包含数字");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new RuntimeException("密码必须包含特殊符号");
        }
    }

    public void register(RegisterRequest request) {
        String role = request.getRole();

        if (role == null || (!"STUDENT".equals(role) && !"TEACHER".equals(role))) {
            throw new RuntimeException("角色错误");
        }

        validatePasswordStrength(request.getPassword());

        String phone = request.getPhone() != null ? request.getPhone().trim() : "";
        if (!phone.matches("\\d{11}")) {
            throw new RuntimeException("手机号必须为11位数字");
        }

        if ("STUDENT".equals(role)) {
            String studentNo = request.getStudentNo() != null ? request.getStudentNo().trim() : "";
            if (!studentNo.matches("\\d{10}")) {
                throw new RuntimeException("学号必须为10位数字");
            }
        } else {
            String staffNo = request.getStaffNo() != null ? request.getStaffNo().trim() : "";
            if (!staffNo.matches("\\d{10}")) {
                throw new RuntimeException("职工号必须为10位数字");
            }
        }
        
        // 根据角色检查用户名是否存在
        if ("STUDENT".equals(role)) {
            if (userMapper.findStudentByUsername(request.getUsername()) != null) {
                throw new RuntimeException("用户名已存在");
            }
        } else {
            if (userMapper.findTeacherByUsername(request.getUsername()) != null) {
                throw new RuntimeException("用户名已存在");
            }
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setRealName(request.getRealName());
        user.setRole(role);
        user.setMajor(request.getMajor());
        user.setAvatar(request.getAvatar());
        user.setPhone(phone);
        user.setStudentNo(request.getStudentNo() != null ? request.getStudentNo().trim() : null);
        user.setStaffNo(request.getStaffNo() != null ? request.getStaffNo().trim() : null);
        
        // 根据角色插入不同的表
        if ("STUDENT".equals(role)) {
            userMapper.insertStudent(user);
        } else {
            userMapper.insertTeacher(user);
        }
    }

    public User login(LoginRequest request) {
        String role = request.getRole();
        User user;
        
        // 根据角色从不同表查询
        if ("STUDENT".equals(role)) {
            user = userMapper.findStudentByUsernameAndPassword(request.getUsername(), request.getPassword());
        } else {
            user = userMapper.findTeacherByUsernameAndPassword(request.getUsername(), request.getPassword());
        }
        
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        return user;
    }

    public void updateUsername(Long userId, String newUsername, String role) {
        // 根据角色检查用户名是否被占用
        if ("STUDENT".equals(role)) {
            User existing = userMapper.findStudentByUsername(newUsername);
            if (existing != null && !existing.getId().equals(userId)) {
                throw new RuntimeException("用户名已被占用");
            }
            userMapper.updateStudentUsername(userId, newUsername);
        } else {
            User existing = userMapper.findTeacherByUsername(newUsername);
            if (existing != null && !existing.getId().equals(userId)) {
                throw new RuntimeException("用户名已被占用");
            }
            userMapper.updateTeacherUsername(userId, newUsername);
        }
    }

    public void updatePassword(Long userId, String oldPassword, String newPassword, String role) {
        int rows;
        if ("STUDENT".equals(role)) {
            rows = userMapper.updateStudentPassword(userId, oldPassword, newPassword);
        } else {
            rows = userMapper.updateTeacherPassword(userId, oldPassword, newPassword);
        }
        if (rows == 0) {
            throw new RuntimeException("原密码错误");
        }
    }

    public void forgotPasswordByStudentNo(String studentNo, String newPassword) {
        String sn = studentNo != null ? studentNo.trim() : "";
        if (!sn.matches("\\d{10}")) {
            throw new RuntimeException("学号必须为10位数字");
        }
        String np = newPassword != null ? newPassword.trim() : "";
        if (np.length() < 6) {
            throw new RuntimeException("密码长度不能少于6位");
        }

        User student = userMapper.findStudentByStudentNo(sn);
        if (student == null) {
            throw new RuntimeException("学号不存在");
        }

        int rows = userMapper.updateStudentPasswordByStudentNo(sn, np);
        if (rows == 0) {
            throw new RuntimeException("重置失败");
        }
    }

    public void forgotPasswordByStaffNo(String staffNo, String newPassword) {
        String sn = staffNo != null ? staffNo.trim() : "";
        if (!sn.matches("\\d{10}")) {
            throw new RuntimeException("工号必须为10位数字");
        }
        String np = newPassword != null ? newPassword.trim() : "";
        if (np.length() < 6) {
            throw new RuntimeException("密码长度不能少于6位");
        }

        User teacher = userMapper.findTeacherByStaffNo(sn);
        if (teacher == null) {
            throw new RuntimeException("工号不存在");
        }

        int rows = userMapper.updateTeacherPasswordByStaffNo(sn, np);
        if (rows == 0) {
            throw new RuntimeException("重置失败");
        }
    }

    public void updateAvatar(Long userId, String avatar, String role) {
        if ("STUDENT".equals(role)) {
            userMapper.updateStudentAvatar(userId, avatar);
        } else {
            userMapper.updateTeacherAvatar(userId, avatar);
        }
    }

    public void updatePhone(Long userId, String phone, String role) {
        String p = phone != null ? phone.trim() : "";
        if (!p.matches("\\d{11}")) {
            throw new RuntimeException("手机号必须为11位数字");
        }

        if ("STUDENT".equals(role)) {
            userMapper.updateStudentPhone(userId, p);
        } else if ("TEACHER".equals(role)) {
            userMapper.updateTeacherPhone(userId, p);
        } else {
            throw new RuntimeException("角色错误");
        }
    }
}