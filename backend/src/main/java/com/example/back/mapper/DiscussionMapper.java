package com.example.back.mapper;

import com.example.back.entity.Discussion;
import com.example.back.entity.DiscussionComment;
import com.example.back.entity.Material;
import com.example.back.entity.News;
import org.apache.ibatis.annotations.*;
import java.util.Map;
import java.util.List;

@Mapper
public interface DiscussionMapper {
    
    @Insert("INSERT INTO discussions(title, description, code, teacher_id, material_id, status, start_time, end_time) " +
            "VALUES(#{title}, #{description}, #{code}, #{teacherId}, #{materialId}, #{status}, #{startTime}, #{endTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Discussion discussion);
    
    @Select("SELECT d.*, t.real_name as teacher_name, " +
            "(SELECT COUNT(*) FROM discussion_comments WHERE discussion_id = d.id) as comment_count " +
            ", (SELECT COUNT(*) FROM student_bindings sb WHERE sb.teacher_id = d.teacher_id) as boundStudentCount " +
            ", (SELECT COUNT(DISTINCT dc.user_id) FROM discussion_comments dc " +
            "      WHERE dc.discussion_id = d.id AND dc.user_type = 'STUDENT' " +
            "        AND dc.user_id IN (SELECT sb2.student_id FROM student_bindings sb2 WHERE sb2.teacher_id = d.teacher_id) " +
            "  ) as boundStudentParticipantCount " +
            ", (SELECT COUNT(*) FROM discussion_comments dc " +
            "      WHERE dc.discussion_id = d.id AND dc.user_type = 'STUDENT' " +
            "        AND dc.user_id IN (SELECT sb3.student_id FROM student_bindings sb3 WHERE sb3.teacher_id = d.teacher_id) " +
            "  ) as boundStudentCommentCount " +
            "FROM discussions d JOIN teachers t ON d.teacher_id = t.id WHERE d.teacher_id = #{teacherId} ORDER BY d.created_at DESC")
    List<Discussion> findByTeacherId(Long teacherId);
    
    @Select("SELECT d.*, t.real_name as teacher_name FROM discussions d JOIN teachers t ON d.teacher_id = t.id WHERE d.id = #{id}")
    Discussion findById(Long id);
    
    @Select("SELECT d.*, t.real_name as teacher_name FROM discussions d JOIN teachers t ON d.teacher_id = t.id WHERE d.code = #{code}")
    Discussion findByCode(String code);

    @Update("UPDATE discussions SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(Long id);

    @Select("SELECT d.id, d.title, d.code, d.view_count FROM discussions d ORDER BY d.view_count DESC, d.created_at DESC LIMIT #{limit}")
    List<Discussion> findHotDiscussions(@Param("limit") int limit);
    
    @Update("UPDATE discussions SET title=#{title}, description=#{description}, material_id=#{materialId}, " +
            "status=#{status}, start_time=#{startTime}, end_time=#{endTime} WHERE id=#{id}")
    int update(Discussion discussion);
    
    @Update("UPDATE discussions SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    @Delete("DELETE FROM discussions WHERE id = #{id}")
    int delete(Long id);
    
    @Select("SELECT COUNT(*) FROM discussions WHERE teacher_id = #{teacherId}")
    int countByTeacher(Long teacherId);
    
    // 讨论-素材关联操作
    @Insert("INSERT INTO discussion_materials(discussion_id, material_id) VALUES(#{discussionId}, #{materialId})")
    int insertDiscussionMaterial(@Param("discussionId") Long discussionId, @Param("materialId") Long materialId);
    
    @Delete("DELETE FROM discussion_materials WHERE discussion_id = #{discussionId}")
    int deleteDiscussionMaterials(Long discussionId);
    
    // 不返回file_url，避免大数据量传输导致MySQL packet过大
    @Select("SELECT m.id, m.title, m.type, m.content, m.description, m.teacher_id, m.status, m.created_at FROM materials m INNER JOIN discussion_materials dm ON m.id = dm.material_id WHERE dm.discussion_id = #{discussionId}")
    List<Material> findMaterialsByDiscussionId(Long discussionId);

    // 讨论-新闻关联操作
    @Insert("INSERT INTO discussion_news(discussion_id, news_id) VALUES(#{discussionId}, #{newsId})")
    int insertDiscussionNews(@Param("discussionId") Long discussionId, @Param("newsId") Long newsId);

    @Delete("DELETE FROM discussion_news WHERE discussion_id = #{discussionId}")
    int deleteDiscussionNews(Long discussionId);

    @Select("SELECT n.id, n.title, n.summary, n.cover_image as coverImage, n.author, n.status, n.view_count as viewCount, n.created_at as createdAt, n.updated_at as updatedAt " +
            "FROM news n INNER JOIN discussion_news dn ON n.id = dn.news_id WHERE dn.discussion_id = #{discussionId} ORDER BY dn.id ASC")
    List<News> findNewsByDiscussionId(Long discussionId);
    
    // 评论相关
    @Insert("INSERT INTO discussion_comments(discussion_id, user_id, user_type, content, image_url, parent_id, reply_to_user_id, reply_to_user_type, status) " +
            "VALUES(#{discussionId}, #{userId}, #{userType}, #{content}, #{imageUrl}, #{parentId}, #{replyToUserId}, #{replyToUserType}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertComment(DiscussionComment comment);
    
    @Select("SELECT * FROM discussion_comments WHERE discussion_id = #{discussionId} AND parent_id IS NULL ORDER BY is_pinned DESC, created_at DESC")
    List<DiscussionComment> findCommentsByDiscussion(Long discussionId);
    
    @Select("SELECT * FROM discussion_comments WHERE parent_id = #{parentId} ORDER BY created_at ASC")
    List<DiscussionComment> findRepliesByParent(Long parentId);

    @Select("SELECT * FROM discussion_comments WHERE discussion_id = #{discussionId} AND status = 'APPROVED' ORDER BY created_at ASC, id ASC")
    List<DiscussionComment> findApprovedCommentsFlatByDiscussion(@Param("discussionId") Long discussionId);
    
    @Update("UPDATE discussion_comments SET is_pinned = #{isPinned} WHERE id = #{id}")
    int updatePinStatus(@Param("id") Long id, @Param("isPinned") Boolean isPinned);
    
    @Select("SELECT COUNT(*) FROM discussion_comments WHERE discussion_id = #{discussionId} AND is_pinned = 1 AND parent_id IS NULL")
    int countPinnedComments(Long discussionId);
    
    @Select("SELECT COUNT(*) FROM discussion_comments WHERE discussion_id = #{discussionId} AND status = 'PENDING'")
    int countPendingComments(Long discussionId);
    
    @Select("SELECT COUNT(*) FROM discussion_comments c JOIN discussions d ON c.discussion_id = d.id " +
            "WHERE d.teacher_id = #{teacherId} AND c.status = 'PENDING'")
    int countPendingCommentsByTeacher(Long teacherId);
    
    @Select("SELECT username, real_name, avatar FROM students WHERE id = #{id}")
    @Results({
        @Result(property = "userName", column = "username"),
        @Result(property = "userRealName", column = "real_name"),
        @Result(property = "userAvatar", column = "avatar")
    })
    DiscussionComment findStudentInfo(Long id);
    
    @Select("SELECT username, real_name, avatar FROM teachers WHERE id = #{id}")
    @Results({
        @Result(property = "userName", column = "username"),
        @Result(property = "userRealName", column = "real_name"),
        @Result(property = "userAvatar", column = "avatar")
    })
    DiscussionComment findTeacherInfo(Long id);
    
    @Update("UPDATE discussion_comments SET status = #{status} WHERE id = #{id}")
    int updateCommentStatus(@Param("id") Long id, @Param("status") String status);
    
    @Delete("DELETE FROM discussion_comments WHERE id = #{id}")
    int deleteComment(Long id);
    
    @Select("SELECT * FROM discussion_comments WHERE id = #{id}")
    DiscussionComment findCommentById(Long id);

    // 点赞相关（不建新表：把点赞用户列表存入 like_users_json）
    @Select("SELECT * FROM discussion_comments WHERE id = #{id} FOR UPDATE")
    DiscussionComment findCommentByIdForUpdate(Long id);

    @Update("UPDATE discussion_comments SET like_count = #{likeCount}, like_users_json = #{likeUsersJson} WHERE id = #{commentId}")
    int updateLikeInfo(@Param("commentId") Long commentId,
                       @Param("likeCount") Integer likeCount,
                       @Param("likeUsersJson") String likeUsersJson);

    @Select(
            "SELECT " +
                    "COUNT(*) as totalCommentCount, " +
                    "SUM(CASE WHEN dc.parent_id IS NULL THEN 1 ELSE 0 END) as topLevelCommentCount, " +
                    "SUM(CASE WHEN dc.parent_id IS NOT NULL THEN 1 ELSE 0 END) as replyCommentCount, " +
                    "SUM(CASE WHEN dc.user_type = 'STUDENT' THEN 1 ELSE 0 END) as studentCommentCount, " +
                    "SUM(CASE WHEN dc.user_type = 'TEACHER' THEN 1 ELSE 0 END) as teacherCommentCount, " +
                    "COUNT(DISTINCT CASE WHEN dc.user_type = 'STUDENT' THEN dc.user_id END) as studentParticipantCount " +
                    "FROM discussion_comments dc " +
                    "JOIN discussions d ON dc.discussion_id = d.id " +
                    "WHERE d.code = #{code}"
    )
    Map<String, Object> getDiscussionCommentStatsByCode(@Param("code") String code);
}
