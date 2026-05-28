package com.example.back.mapper;

import com.example.back.entity.NewsComment;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface NewsCommentMapper {
    
    // 查询新闻的顶级评论（不包括回复）
    @Select("SELECT * FROM news_comments WHERE news_id = #{newsId} AND (parent_id IS NULL OR parent_id = 0) ORDER BY created_at DESC")
    List<NewsComment> findByNewsId(Long newsId);
    
    // 查询某条评论的所有回复
    @Select("SELECT * FROM news_comments WHERE parent_id = #{parentId} AND parent_id > 0 ORDER BY created_at ASC")
    List<NewsComment> findRepliesByParentId(Long parentId);

    @Insert("INSERT INTO news_comments(news_id, user_id, user_type, content, image_url, parent_id, reply_to_user_id, reply_to_user_type) " +
            "VALUES(#{newsId}, #{userId}, #{userType}, #{content}, #{imageUrl}, #{parentId}, #{replyToUserId}, #{replyToUserType})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(NewsComment comment);

    @Insert("INSERT INTO news_comments(news_id, user_id, user_type, content, parent_id, reply_to_user_id, reply_to_user_type) " +
            "VALUES(#{newsId}, #{userId}, #{userType}, #{content}, #{parentId}, #{replyToUserId}, #{replyToUserType})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLegacy(NewsComment comment);

    // 点赞相关
    @Select("SELECT * FROM news_comments WHERE id = #{id} FOR UPDATE")
    NewsComment findCommentByIdForUpdate(Long id);

    @Update("UPDATE news_comments SET like_count = #{likeCount}, like_users_json = #{likeUsersJson} WHERE id = #{commentId}")
    int updateLikeInfo(@Param("commentId") Long commentId,
                       @Param("likeCount") Integer likeCount,
                       @Param("likeUsersJson") String likeUsersJson);
    
    @Delete("DELETE FROM news_comments WHERE id = #{id}")
    int delete(Long id);
    
    @Select("SELECT COUNT(*) FROM news_comments WHERE news_id = #{newsId}")
    int countByNewsId(Long newsId);
}
