package com.example.back.mapper;

import com.example.back.entity.BindCode;
import com.example.back.entity.StudentBinding;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface BindMapper {
    
    @Select("SELECT * FROM bind_codes WHERE teacher_id = #{teacherId} ORDER BY created_at DESC LIMIT 1")
    BindCode findByTeacherId(Long teacherId);
    
    @Select("SELECT * FROM bind_codes WHERE code = #{code}")
    BindCode findByCode(String code);
    
    @Insert("INSERT INTO bind_codes(code, teacher_id) VALUES(#{code}, #{teacherId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertBindCode(BindCode bindCode);
    
    @Delete("DELETE FROM bind_codes WHERE teacher_id = #{teacherId}")
    int deleteByTeacherId(Long teacherId);
    
    @Select("SELECT * FROM student_bindings WHERE student_id = #{studentId} AND teacher_id = #{teacherId}")
    StudentBinding findBinding(@Param("studentId") Long studentId, @Param("teacherId") Long teacherId);
    
    @Insert("INSERT INTO student_bindings(student_id, teacher_id) VALUES(#{studentId}, #{teacherId})")
    int insertBinding(StudentBinding binding);

    @Delete("DELETE FROM student_bindings WHERE student_id = #{studentId} AND teacher_id = #{teacherId}")
    int deleteBinding(@Param("studentId") Long studentId, @Param("teacherId") Long teacherId);
    
    @Select("SELECT sb.*, s.username as student_name, s.real_name as student_real_name, s.major as student_major " +
            "FROM student_bindings sb JOIN students s ON sb.student_id = s.id WHERE sb.teacher_id = #{teacherId} ORDER BY sb.bind_time DESC")
    List<StudentBinding> findBindingsByTeacher(Long teacherId);
    
    @Select("SELECT COUNT(*) FROM student_bindings WHERE teacher_id = #{teacherId}")
    int countByTeacher(Long teacherId);
    
    @Select("SELECT sb.*, t.username as teacher_name, t.real_name as teacher_real_name, t.major as teacher_major " +
            "FROM student_bindings sb JOIN teachers t ON sb.teacher_id = t.id WHERE sb.student_id = #{studentId} ORDER BY sb.bind_time DESC")
    List<StudentBinding> findBindingsByStudent(Long studentId);
}
