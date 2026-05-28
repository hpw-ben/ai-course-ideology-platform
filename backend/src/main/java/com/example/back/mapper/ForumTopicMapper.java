package com.example.back.mapper;

import com.example.back.entity.ForumTopic;
import com.example.back.entity.Material;
import com.example.back.entity.TopicComment;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ForumTopicMapper {
    
    @Select("SELECT t.*, t.news_ids_json as newsIdsJson, a.real_name as admin_name, " +
            "(SELECT COUNT(*) FROM topic_comments WHERE topic_id = t.id AND status = 'NORMAL') as comment_count " +
            "FROM forum_topics t JOIN admins a ON t.admin_id = a.id ORDER BY t.created_at DESC")
    List<ForumTopic> findAll();
    
    @Select("SELECT t.*, t.news_ids_json as newsIdsJson, a.real_name as admin_name, " +
            "(SELECT COUNT(*) FROM topic_comments WHERE topic_id = t.id AND status = 'NORMAL') as comment_count " +
            "FROM forum_topics t JOIN admins a ON t.admin_id = a.id WHERE t.status = 'ACTIVE' ORDER BY t.created_at DESC")
    List<ForumTopic> findActive();

    @Select("SELECT id, title, code, view_count FROM forum_topics WHERE status = 'ACTIVE' ORDER BY view_count DESC, created_at DESC LIMIT #{limit}")
    List<ForumTopic> findHotTopics(@Param("limit") int limit);
    
    @Select("SELECT t.*, t.news_ids_json as newsIdsJson, a.real_name as admin_name FROM forum_topics t JOIN admins a ON t.admin_id = a.id WHERE t.id = #{id}")
    ForumTopic findById(Long id);
    
    @Select("SELECT t.*, t.news_ids_json as newsIdsJson, a.real_name as admin_name FROM forum_topics t JOIN admins a ON t.admin_id = a.id WHERE t.code = #{code}")
    ForumTopic findByCode(String code);
    
    @Insert("INSERT INTO forum_topics(title, description, code, admin_id, news_ids_json, status) VALUES(#{title}, #{description}, #{code}, #{adminId}, #{newsIdsJson}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ForumTopic topic);
    
    @Update("UPDATE forum_topics SET title=#{title}, description=#{description}, news_ids_json=#{newsIdsJson}, status=#{status} WHERE id=#{id}")
    int update(ForumTopic topic);
    
    @Delete("DELETE FROM forum_topics WHERE id = #{id}")
    int delete(Long id);
    
    @Update("UPDATE forum_topics SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(Long id);
    
    // 话题-素材关联
    @Insert("INSERT INTO topic_materials(topic_id, material_id) VALUES(#{topicId}, #{materialId})")
    int insertTopicMaterial(@Param("topicId") Long topicId, @Param("materialId") Long materialId);
    
    @Delete("DELETE FROM topic_materials WHERE topic_id = #{topicId}")
    int deleteTopicMaterials(Long topicId);
    
    @Select("SELECT m.id, m.title, m.type, m.description, m.teacher_id, m.status, m.created_at " +
            "FROM materials m INNER JOIN topic_materials tm ON m.id = tm.material_id WHERE tm.topic_id = #{topicId}")
    List<Material> findMaterialsByTopicId(Long topicId);
    
    // 评论相关
    @Insert("INSERT INTO topic_comments(topic_id, user_id, user_type, content, image_url, parent_id, reply_to_user_id, reply_to_user_type, is_pinned, status) " +
            "VALUES(#{topicId}, #{userId}, #{userType}, #{content}, #{imageUrl}, #{parentId}, #{replyToUserId}, #{replyToUserType}, #{isPinned}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertComment(TopicComment comment);
    
    @Select("SELECT * FROM topic_comments WHERE topic_id = #{topicId} AND parent_id IS NULL AND status = 'NORMAL' ORDER BY is_pinned DESC, created_at DESC")
    List<TopicComment> findCommentsByTopicId(Long topicId);
    
    @Select("SELECT * FROM topic_comments WHERE parent_id = #{parentId} AND status = 'NORMAL' ORDER BY created_at ASC")
    List<TopicComment> findRepliesByParentId(Long parentId);
    
    @Select("SELECT * FROM topic_comments WHERE id = #{id}")
    TopicComment findCommentById(Long id);

    @Select("SELECT * FROM topic_comments WHERE id = #{id} FOR UPDATE")
    TopicComment findCommentByIdForUpdate(Long id);

    @Update("UPDATE topic_comments SET like_count = #{likeCount}, like_users_json = #{likeUsersJson} WHERE id = #{commentId}")
    int updateLikeInfo(@Param("commentId") Long commentId,
                       @Param("likeCount") Integer likeCount,
                       @Param("likeUsersJson") String likeUsersJson);
    
    @Update("UPDATE topic_comments SET status = 'DELETED', delete_reason = #{reason} WHERE id = #{id}")
    int deleteComment(@Param("id") Long id, @Param("reason") String reason);
    
    @Update("UPDATE topic_comments SET is_pinned = #{isPinned} WHERE id = #{id}")
    int updateCommentPinned(@Param("id") Long id, @Param("isPinned") Boolean isPinned);
    
    @Select("SELECT COUNT(*) FROM topic_comments WHERE topic_id = #{topicId} AND is_pinned = 1 AND parent_id IS NULL AND status = 'NORMAL'")
    int countPinnedComments(Long topicId);
}
