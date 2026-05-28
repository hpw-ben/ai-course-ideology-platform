package com.example.back.mapper;

import com.example.back.entity.Admin;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AdminMapper {
    
    @Select("SELECT *, 'ADMIN' as role FROM admins WHERE username = #{username} AND password = #{password}")
    Admin findByUsernameAndPassword(@Param("username") String username, @Param("password") String password);
    
    @Select("SELECT *, 'ADMIN' as role FROM admins WHERE id = #{id}")
    Admin findById(Long id);
    
    @Select("SELECT *, 'ADMIN' as role FROM admins WHERE username = #{username}")
    Admin findByUsername(String username);
    
    @Update("UPDATE admins SET password = #{newPassword} WHERE id = #{id} AND password = #{oldPassword}")
    int updatePassword(@Param("id") Long id, @Param("oldPassword") String oldPassword, @Param("newPassword") String newPassword);
    
    @Update("UPDATE admins SET avatar = #{avatar} WHERE id = #{id}")
    int updateAvatar(@Param("id") Long id, @Param("avatar") String avatar);

    @Select("SELECT COUNT(*) FROM students")
    int countStudents();

    @Select("SELECT COUNT(*) FROM teachers")
    int countTeachers();

    @Select("SELECT COUNT(*) FROM materials")
    int countMaterials();

    @Select("SELECT COUNT(*) FROM learning_tasks")
    int countLearningTasks();

    @Select("SELECT COUNT(*) FROM discussions")
    int countDiscussions();

    @Select("SELECT COUNT(*) FROM news")
    int countNews();

    @Select("SELECT COUNT(*) FROM forum_topics")
    int countForumTopics();

    @Select("SELECT DATABASE()")
    String currentDatabase();

    @Select("SELECT @@hostname")
    String currentHostname();

    @Select("SELECT @@port")
    Integer currentPort();

    @Select("SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'discussions' AND COLUMN_NAME = 'view_count'")
    Integer hasDiscussionViewCountColumn();

    @Select("SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'materials' AND COLUMN_NAME = 'view_count'")
    Integer hasMaterialViewCountColumn();

    @Select("SELECT COALESCE(SUM(view_count), 0) FROM news")
    Long sumNewsViewCount();

    @Select("SELECT COALESCE(SUM(view_count), 0) FROM discussions")
    Long sumDiscussionViewCount();

    @Select("SELECT COALESCE(SUM(view_count), 0) FROM forum_topics")
    Long sumForumTopicViewCount();
}
