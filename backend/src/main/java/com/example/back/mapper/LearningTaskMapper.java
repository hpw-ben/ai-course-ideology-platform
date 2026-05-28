package com.example.back.mapper;

import com.example.back.entity.LearningTask;
import com.example.back.entity.Material;
import com.example.back.entity.News;
import com.example.back.entity.TaskComment;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface LearningTaskMapper {
    
    @Insert("INSERT INTO learning_tasks(title, description, quiz_json, viewpoint_options_json, checkin_required_seconds, code, teacher_id, material_id, status, end_time) " +
            "VALUES(#{title}, #{description}, #{quizJson}, #{viewpointOptionsJson}, #{checkinRequiredSeconds}, #{code}, #{teacherId}, #{materialId}, #{status}, #{endTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LearningTask task);
    
    @Select(
            "SELECT t.*, " +
                    "(SELECT COUNT(*) FROM student_bindings sb WHERE sb.teacher_id = t.teacher_id) as boundStudentCount, " +
                    "(SELECT COUNT(DISTINCT lr.student_id) FROM learning_records lr WHERE lr.type = 'TASK' AND lr.target_code COLLATE utf8mb4_unicode_ci = t.code COLLATE utf8mb4_unicode_ci) as participantCount, " +
                    "(SELECT COUNT(DISTINCT lr.student_id) " +
                    "   FROM learning_records lr " +
                    "   WHERE lr.type = 'TASK' AND lr.target_code COLLATE utf8mb4_unicode_ci = t.code COLLATE utf8mb4_unicode_ci " +
                    "     AND lr.checked_in = 1 " +
                    "     AND lr.quiz_submitted_at IS NOT NULL " +
                    "     AND ( (SELECT COUNT(*) FROM task_materials tm WHERE tm.task_id = t.id) = 0 " +
                    "           OR (SELECT COUNT(DISTINCT lr2.target_code) FROM learning_records lr2 " +
                    "                WHERE lr2.type = 'TASK_MATERIAL' AND lr2.target_id = t.id AND lr2.student_id = lr.student_id) " +
                    "              >= (SELECT COUNT(*) FROM task_materials tm2 WHERE tm2.task_id = t.id) " +
                    "         ) " +
                    ") as completedCount " +
                    "FROM learning_tasks t WHERE t.teacher_id = #{teacherId} ORDER BY t.created_at DESC"
    )
    List<LearningTask> findByTeacherId(Long teacherId);

    @Select("SELECT * FROM learning_tasks WHERE teacher_id = #{teacherId} ORDER BY created_at DESC")
    List<LearningTask> findByTeacherIdSimple(Long teacherId);
    
    @Select("SELECT * FROM learning_tasks WHERE id = #{id}")
    LearningTask findById(Long id);
    
    @Select("SELECT * FROM learning_tasks WHERE code = #{code}")
    LearningTask findByCode(String code);
    
    @Delete("DELETE FROM learning_tasks WHERE id = #{id}")
    int delete(Long id);
    
    @Select("SELECT COUNT(*) FROM learning_tasks WHERE teacher_id = #{teacherId}")
    int countByTeacher(Long teacherId);
    
    // 评论相关
    @Insert("INSERT INTO task_comments(task_id, user_id, user_type, content, image_url, parent_id, reply_to_user_id, reply_to_user_type, is_pinned) " +
            "VALUES(#{taskId}, #{userId}, #{userType}, #{content}, #{imageUrl}, #{parentId}, #{replyToUserId}, #{replyToUserType}, #{isPinned})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertComment(TaskComment comment);

    @Insert("INSERT INTO task_comments(task_id, user_id, user_type, content, parent_id, reply_to_user_id, reply_to_user_type, is_pinned) " +
            "VALUES(#{taskId}, #{userId}, #{userType}, #{content}, #{parentId}, #{replyToUserId}, #{replyToUserType}, #{isPinned})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertCommentLegacy(TaskComment comment);
    
    @Select("SELECT * FROM task_comments WHERE task_id = #{taskId} AND parent_id IS NULL ORDER BY is_pinned DESC, created_at DESC")
    List<TaskComment> findCommentsByTaskId(Long taskId);
    
    @Select("SELECT * FROM task_comments WHERE parent_id = #{parentId} ORDER BY created_at ASC")
    List<TaskComment> findRepliesByParentId(Long parentId);
    
    @Select("SELECT * FROM task_comments WHERE id = #{id}")
    TaskComment findCommentById(Long id);

    // 点赞相关（不建新表：把点赞用户列表存入 like_users_json）
    @Select("SELECT * FROM task_comments WHERE id = #{id} FOR UPDATE")
    TaskComment findCommentByIdForUpdate(Long id);

    @Update("UPDATE task_comments SET like_count = #{likeCount}, like_users_json = #{likeUsersJson} WHERE id = #{commentId}")
    int updateLikeInfo(@Param("commentId") Long commentId,
                       @Param("likeCount") Integer likeCount,
                       @Param("likeUsersJson") String likeUsersJson);
    
    @Delete("DELETE FROM task_comments WHERE id = #{id}")
    int deleteComment(Long id);
    
    @Delete("DELETE FROM task_comments WHERE parent_id = #{parentId}")
    int deleteRepliesByParentId(Long parentId);
    
    @Update("UPDATE task_comments SET is_pinned = #{isPinned} WHERE id = #{id}")
    int updateCommentPinned(@Param("id") Long id, @Param("isPinned") Boolean isPinned);
    
    @Select("SELECT COUNT(*) FROM task_comments WHERE task_id = #{taskId} AND is_pinned = true AND parent_id IS NULL")
    int countPinnedComments(Long taskId);
    
    @Select("SELECT COUNT(*) FROM task_comments WHERE task_id = #{taskId}")
    int countCommentsByTaskId(Long taskId);
    
    // 任务-素材关联操作
    @Insert("INSERT INTO task_materials(task_id, material_id) VALUES(#{taskId}, #{materialId})")
    int insertTaskMaterial(@Param("taskId") Long taskId, @Param("materialId") Long materialId);
    
    @Delete("DELETE FROM task_materials WHERE task_id = #{taskId}")
    int deleteTaskMaterials(Long taskId);
    
    // 不返回file_url，避免大数据量传输导致MySQL packet过大
    @Select("SELECT m.id, m.title, m.type, m.content, m.description, m.teacher_id, m.status, m.created_at FROM materials m INNER JOIN task_materials tm ON m.id = tm.material_id WHERE tm.task_id = #{taskId}")
    List<Material> findMaterialsByTaskId(Long taskId);

    // 任务-新闻关联操作
    @Insert("INSERT INTO task_news(task_id, news_id) VALUES(#{taskId}, #{newsId})")
    int insertTaskNews(@Param("taskId") Long taskId, @Param("newsId") Long newsId);

    @Delete("DELETE FROM task_news WHERE task_id = #{taskId}")
    int deleteTaskNews(Long taskId);

    @Select("SELECT n.id, n.title, n.summary, n.cover_image as coverImage, n.author, n.status, n.view_count as viewCount, n.created_at as createdAt, n.updated_at as updatedAt " +
            "FROM news n INNER JOIN task_news tn ON n.id = tn.news_id WHERE tn.task_id = #{taskId} ORDER BY tn.id ASC")
    List<News> findNewsByTaskId(Long taskId);
}
