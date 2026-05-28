package com.example.back.mapper;

import com.example.back.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    
    // 学生表操作
    @Insert("INSERT INTO students(username, password, real_name, major, avatar, student_no, phone) VALUES(#{username}, #{password}, #{realName}, #{major}, #{avatar}, #{studentNo}, #{phone})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertStudent(User user);

    @Select("SELECT *, 'STUDENT' as role FROM students WHERE username = #{username}")
    User findStudentByUsername(String username);

    @Select("SELECT *, 'STUDENT' as role FROM students WHERE username = #{username} AND password = #{password}")
    User findStudentByUsernameAndPassword(@Param("username") String username, @Param("password") String password);

    @Select("SELECT *, 'STUDENT' as role FROM students WHERE id = #{id}")
    User findStudentById(Long id);

    @Select("SELECT *, 'STUDENT' as role FROM students WHERE student_no = #{studentNo}")
    User findStudentByStudentNo(@Param("studentNo") String studentNo);

    @Update("UPDATE students SET username = #{username} WHERE id = #{id}")
    int updateStudentUsername(@Param("id") Long id, @Param("username") String username);

    @Update("UPDATE students SET password = #{newPassword} WHERE id = #{id} AND password = #{oldPassword}")
    int updateStudentPassword(@Param("id") Long id, @Param("oldPassword") String oldPassword, @Param("newPassword") String newPassword);

    @Update("UPDATE students SET password = #{newPassword} WHERE student_no = #{studentNo}")
    int updateStudentPasswordByStudentNo(@Param("studentNo") String studentNo, @Param("newPassword") String newPassword);

    @Update("UPDATE students SET avatar = #{avatar} WHERE id = #{id}")
    int updateStudentAvatar(@Param("id") Long id, @Param("avatar") String avatar);

    @Update("UPDATE students SET major = #{major} WHERE id = #{id}")
    int updateStudentMajor(@Param("id") Long id, @Param("major") String major);

    @Update("UPDATE students SET phone = #{phone} WHERE id = #{id}")
    int updateStudentPhone(@Param("id") Long id, @Param("phone") String phone);

    // 教师表操作
    @Insert("INSERT INTO teachers(username, password, real_name, major, avatar, staff_no, phone) VALUES(#{username}, #{password}, #{realName}, #{major}, #{avatar}, #{staffNo}, #{phone})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertTeacher(User user);

    @Select("SELECT *, 'TEACHER' as role FROM teachers WHERE username = #{username}")
    User findTeacherByUsername(String username);

    @Select("SELECT *, 'TEACHER' as role FROM teachers WHERE username = #{username} AND password = #{password}")
    User findTeacherByUsernameAndPassword(@Param("username") String username, @Param("password") String password);

    @Select("SELECT *, 'TEACHER' as role FROM teachers WHERE id = #{id}")
    User findTeacherById(Long id);

    @Select("SELECT *, 'TEACHER' as role FROM teachers WHERE staff_no = #{staffNo}")
    User findTeacherByStaffNo(@Param("staffNo") String staffNo);

    @Update("UPDATE teachers SET username = #{username} WHERE id = #{id}")
    int updateTeacherUsername(@Param("id") Long id, @Param("username") String username);

    @Update("UPDATE teachers SET password = #{newPassword} WHERE id = #{id} AND password = #{oldPassword}")
    int updateTeacherPassword(@Param("id") Long id, @Param("oldPassword") String oldPassword, @Param("newPassword") String newPassword);

    @Update("UPDATE teachers SET password = #{newPassword} WHERE staff_no = #{staffNo}")
    int updateTeacherPasswordByStaffNo(@Param("staffNo") String staffNo, @Param("newPassword") String newPassword);

    @Update("UPDATE teachers SET avatar = #{avatar} WHERE id = #{id}")
    int updateTeacherAvatar(@Param("id") Long id, @Param("avatar") String avatar);

    @Update("UPDATE teachers SET phone = #{phone} WHERE id = #{id}")
    int updateTeacherPhone(@Param("id") Long id, @Param("phone") String phone);
}
