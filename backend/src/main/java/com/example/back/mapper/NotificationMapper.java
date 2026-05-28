package com.example.back.mapper;

import com.example.back.entity.UserNotification;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface NotificationMapper {
    
    @Select("SELECT * FROM user_notifications WHERE user_id = #{userId} AND user_type = #{userType} ORDER BY created_at DESC")
    List<UserNotification> findByUser(@Param("userId") Long userId, @Param("userType") String userType);
    
    @Select("SELECT * FROM user_notifications WHERE user_id = #{userId} AND user_type = #{userType} AND is_read = 0 ORDER BY created_at DESC")
    List<UserNotification> findUnreadByUser(@Param("userId") Long userId, @Param("userType") String userType);
    
    @Select("SELECT COUNT(*) FROM user_notifications WHERE user_id = #{userId} AND user_type = #{userType} AND is_read = 0")
    int countUnread(@Param("userId") Long userId, @Param("userType") String userType);
    
    @Insert("INSERT INTO user_notifications(user_id, user_type, title, content, type) VALUES(#{userId}, #{userType}, #{title}, #{content}, #{type})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserNotification notification);
    
    @Update("UPDATE user_notifications SET is_read = 1 WHERE id = #{id}")
    int markAsRead(Long id);
    
    @Update("UPDATE user_notifications SET is_read = 1 WHERE user_id = #{userId} AND user_type = #{userType}")
    int markAllAsRead(@Param("userId") Long userId, @Param("userType") String userType);
    
    @Delete("DELETE FROM user_notifications WHERE id = #{id}")
    int delete(Long id);
}
