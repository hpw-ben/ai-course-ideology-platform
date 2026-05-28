package com.example.back.controller;

import com.example.back.dto.Result;
import com.example.back.entity.StudentBinding;
import com.example.back.entity.User;
import com.example.back.service.BindService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bind")
public class BindController {

    @Autowired
    private BindService bindService;

    // 获取或生成绑定码
    @GetMapping("/code/{teacherId}")
    public Result<String> getBindCode(@PathVariable Long teacherId) {
        try {
            String code = bindService.getOrCreateBindCode(teacherId);
            return Result.success(code);
        } catch (Exception e) {
            return Result.error("获取绑定码失败: " + e.getMessage());
        }
    }

    // 重新生成绑定码
    @PostMapping("/code/regenerate/{teacherId}")
    public Result<String> regenerateBindCode(@PathVariable Long teacherId) {
        try {
            String code = bindService.regenerateBindCode(teacherId);
            return Result.success(code);
        } catch (Exception e) {
            return Result.error("生成绑定码失败: " + e.getMessage());
        }
    }

    // 学生绑定教师
    @PostMapping("/bindStudent")
    public Result<User> bindStudent(@RequestBody Map<String, Object> body) {
        try {
            Long studentId = Long.valueOf(body.get("studentId").toString());
            String code = body.get("code").toString();
            User user = bindService.bindStudent(studentId, code);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 获取教师绑定的学生列表
    @GetMapping("/students/{teacherId}")
    public Result<List<StudentBinding>> getBindingStudents(@PathVariable Long teacherId) {
        try {
            List<StudentBinding> list = bindService.getBindingsByTeacher(teacherId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("获取学生列表失败: " + e.getMessage());
        }
    }

    // 获取绑定学生数量
    @GetMapping("/count/{teacherId}")
    public Result<Integer> getBindingCount(@PathVariable Long teacherId) {
        try {
            int count = bindService.getBindingCount(teacherId);
            return Result.success(count);
        } catch (Exception e) {
            return Result.error("获取数量失败");
        }
    }
    
    // 获取学生绑定的教师列表
    @GetMapping("/teachers/{studentId}")
    public Result<List<StudentBinding>> getBindingTeachers(@PathVariable Long studentId) {
        try {
            List<StudentBinding> list = bindService.getBindingsByStudent(studentId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("获取教师列表失败: " + e.getMessage());
        }
    }

    // 教师解绑学生
    @PostMapping("/unbind")
    public Result<String> unbind(@RequestBody Map<String, Object> body) {
        try {
            Long teacherId = body.get("teacherId") == null ? null : Long.valueOf(body.get("teacherId").toString());
            Long studentId = body.get("studentId") == null ? null : Long.valueOf(body.get("studentId").toString());
            bindService.unbindStudent(teacherId, studentId);
            return Result.success("解绑成功");
        } catch (Exception e) {
            return Result.error("解绑失败: " + e.getMessage());
        }
    }
}
