package com.example.back.service;

import com.example.back.entity.BindCode;
import com.example.back.entity.StudentBinding;
import com.example.back.entity.User;
import com.example.back.mapper.BindMapper;
import com.example.back.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class BindService {
    
    @Autowired
    private BindMapper bindMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    // 生成或获取教师绑定码
    public String getOrCreateBindCode(Long teacherId) {
        BindCode existing = bindMapper.findByTeacherId(teacherId);
        if (existing != null) {
            return existing.getCode();
        }
        // 生成新绑定码：BD + 6位随机字符
        String code = "BD" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        BindCode bindCode = new BindCode();
        bindCode.setCode(code);
        bindCode.setTeacherId(teacherId);
        bindMapper.insertBindCode(bindCode);
        return code;
    }
    
    // 重新生成绑定码
    @Transactional
    public String regenerateBindCode(Long teacherId) {
        bindMapper.deleteByTeacherId(teacherId);
        String code = "BD" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        BindCode bindCode = new BindCode();
        bindCode.setCode(code);
        bindCode.setTeacherId(teacherId);
        bindMapper.insertBindCode(bindCode);
        return code;
    }

    // 学生绑定教师
    @Transactional
    public User bindStudent(Long studentId, String code) throws Exception {
        BindCode bindCode = bindMapper.findByCode(code);
        if (bindCode == null) {
            throw new Exception("绑定码不存在");
        }
        
        // 检查是否已绑定
        StudentBinding existing = bindMapper.findBinding(studentId, bindCode.getTeacherId());
        if (existing != null) {
            throw new Exception("您已绑定该教师");
        }
        
        // 获取教师信息
        User teacher = userMapper.findTeacherById(bindCode.getTeacherId());
        if (teacher == null) {
            throw new Exception("教师不存在");
        }
        
        // 创建绑定关系
        StudentBinding binding = new StudentBinding();
        binding.setStudentId(studentId);
        binding.setTeacherId(bindCode.getTeacherId());
        bindMapper.insertBinding(binding);

        // 返回更新后的学生信息
        return userMapper.findStudentById(studentId);
    }
    
    // 获取教师绑定的学生列表
    public List<StudentBinding> getBindingsByTeacher(Long teacherId) {
        return bindMapper.findBindingsByTeacher(teacherId);
    }
    
    // 获取绑定学生数量
    public int getBindingCount(Long teacherId) {
        return bindMapper.countByTeacher(teacherId);
    }
    
    // 获取学生绑定的教师列表
    public List<StudentBinding> getBindingsByStudent(Long studentId) {
        return bindMapper.findBindingsByStudent(studentId);
    }

    // 教师解绑学生
    @Transactional
    public void unbindStudent(Long teacherId, Long studentId) {
        if (teacherId == null) throw new IllegalArgumentException("teacherId不能为空");
        if (studentId == null) throw new IllegalArgumentException("studentId不能为空");
        StudentBinding existing = bindMapper.findBinding(studentId, teacherId);
        if (existing == null) {
            throw new RuntimeException("绑定关系不存在");
        }
        bindMapper.deleteBinding(studentId, teacherId);
    }
}
