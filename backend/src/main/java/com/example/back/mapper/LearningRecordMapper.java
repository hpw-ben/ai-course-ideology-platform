package com.example.back.mapper;

import com.example.back.dto.StudentParticipationDetail;
import com.example.back.entity.LearningRecord;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface LearningRecordMapper {
    
    // 插入学习记录
    @Insert("INSERT INTO learning_records(student_id, type, target_id, target_code, target_title, duration, enter_time) " +
            "VALUES(#{studentId}, #{type}, #{targetId}, #{targetCode}, #{targetTitle}, #{duration}, #{enterTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LearningRecord record);
    
    // 获取学生的学习轨迹
    @Select("SELECT * FROM learning_records WHERE student_id = #{studentId} ORDER BY enter_time DESC")
    List<LearningRecord> findByStudentId(Long studentId);

    @Select("SELECT * FROM learning_records WHERE id = #{id}")
    LearningRecord findById(Long id);

    @Update("UPDATE learning_records SET " +
            "duration = CASE WHEN #{duration} > IFNULL(last_heartbeat_duration, 0) THEN #{duration} ELSE duration END, " +
            "material_duration = CASE WHEN #{materialDuration} IS NULL THEN material_duration ELSE GREATEST(IFNULL(material_duration, 0), #{materialDuration}) END, " +
            "interaction_duration = CASE WHEN #{interactionDuration} IS NULL THEN interaction_duration ELSE GREATEST(IFNULL(interaction_duration, 0), #{interactionDuration}) END, " +
            "last_heartbeat_duration = GREATEST(IFNULL(last_heartbeat_duration, 0), #{duration}), " +
            "last_heartbeat_at = #{heartbeatAt} " +
            "WHERE id = #{id}")
    int updateHeartbeat(@Param("id") Long id,
                        @Param("duration") Integer duration,
                        @Param("materialDuration") Integer materialDuration,
                        @Param("interactionDuration") Integer interactionDuration,
                        @Param("heartbeatAt") LocalDateTime heartbeatAt);

    @Update("UPDATE learning_records SET " +
            "duration = GREATEST(IFNULL(duration, 0), #{duration}), " +
            "leave_time = #{leaveTime}, " +
            "material_duration = CASE WHEN #{materialDuration} IS NULL THEN material_duration ELSE GREATEST(IFNULL(material_duration, 0), #{materialDuration}) END, " +
            "interaction_duration = CASE WHEN #{interactionDuration} IS NULL THEN interaction_duration ELSE GREATEST(IFNULL(interaction_duration, 0), #{interactionDuration}) END, " +
            "last_heartbeat_duration = GREATEST(IFNULL(last_heartbeat_duration, 0), #{duration}), " +
            "last_heartbeat_at = #{leaveTime} " +
            "WHERE id = #{id}")
    int updateLeave(@Param("id") Long id,
                    @Param("duration") Integer duration,
                    @Param("materialDuration") Integer materialDuration,
                    @Param("interactionDuration") Integer interactionDuration,
                    @Param("leaveTime") LocalDateTime leaveTime);

    @Update("UPDATE learning_records SET " +
            "quiz_json = #{quizJson}, " +
            "quiz_answers_json = #{quizAnswersJson}, " +
            "quiz_total = #{quizTotal}, " +
            "quiz_correct = #{quizCorrect}, " +
            "quiz_submitted_at = #{quizSubmittedAt}, " +
            "reflection = #{reflection} " +
            "WHERE id = #{id}")
    int updateQuizResult(@Param("id") Long id,
                        @Param("quizJson") String quizJson,
                        @Param("quizAnswersJson") String quizAnswersJson,
                        @Param("quizTotal") Integer quizTotal,
                        @Param("quizCorrect") Integer quizCorrect,
                        @Param("quizSubmittedAt") LocalDateTime quizSubmittedAt,
                        @Param("reflection") String reflection);

    @Update("UPDATE learning_records SET reflection = #{reflection} WHERE id = #{id}")
    int updateReflection(@Param("id") Long id, @Param("reflection") String reflection);

    @Update("UPDATE learning_records SET report_pdf_url = #{url}, report_generated_at = #{generatedAt} WHERE id = #{id}")
    int updateReportInfo(@Param("id") Long id, @Param("url") String url, @Param("generatedAt") LocalDateTime generatedAt);

    @Update("UPDATE learning_records SET checked_in = 1, checkin_time = #{checkinTime} WHERE id = #{id}")
    int updateCheckin(@Param("id") Long id, @Param("checkinTime") LocalDateTime checkinTime);

    @Update("UPDATE learning_records SET viewpoint_choice = #{viewpointChoice}, short_note = #{shortNote} WHERE id = #{id}")
    int updateViewpointAndNote(@Param("id") Long id, @Param("viewpointChoice") String viewpointChoice, @Param("shortNote") String shortNote);

    @Select("SELECT COUNT(*) FROM learning_records WHERE student_id = #{studentId} AND type = 'TASK' AND target_id = #{taskId} AND checked_in = 1")
    int countCheckedInByStudentAndTask(@Param("studentId") Long studentId, @Param("taskId") Long taskId);

    @Select("SELECT COUNT(*) FROM learning_records WHERE student_id = #{studentId} AND type = 'TASK' AND target_id = #{taskId} AND quiz_submitted_at IS NOT NULL")
    int countQuizSubmittedByStudentAndTask(@Param("studentId") Long studentId, @Param("taskId") Long taskId);

    @Select("SELECT COUNT(*) FROM learning_records WHERE student_id = #{studentId} AND type = 'DISCUSSION' AND target_code = #{code}")
    int countStudentDiscussionRecord(@Param("studentId") Long studentId, @Param("code") String code);

    @Select("SELECT COUNT(DISTINCT target_code) FROM learning_records WHERE student_id = #{studentId} AND type = 'TASK_MATERIAL' AND target_id = #{taskId}")
    int countCompletedMaterialsByStudentAndTask(@Param("studentId") Long studentId, @Param("taskId") Long taskId);

    @Select("SELECT DISTINCT target_code FROM learning_records WHERE student_id = #{studentId} AND type = 'TASK_MATERIAL' AND target_id = #{taskId}")
    List<String> findCompletedMaterialCodesByStudentAndTask(@Param("studentId") Long studentId, @Param("taskId") Long taskId);

    @Select("SELECT COUNT(*) FROM learning_records WHERE student_id = #{studentId} AND type = 'TASK_MATERIAL' AND target_id = #{taskId} AND target_code = #{materialId}")
    int existsTaskMaterialCompletion(@Param("studentId") Long studentId, @Param("taskId") Long taskId, @Param("materialId") String materialId);

    @Insert("INSERT INTO learning_records(student_id, type, target_id, target_code, target_title, duration, enter_time) " +
            "VALUES(#{studentId}, 'TASK_MATERIAL', #{taskId}, #{materialId}, #{materialTitle}, 0, #{enterTime})")
    int insertTaskMaterialCompletion(@Param("studentId") Long studentId,
                                    @Param("taskId") Long taskId,
                                    @Param("materialId") String materialId,
                                    @Param("materialTitle") String materialTitle,
                                    @Param("enterTime") LocalDateTime enterTime);

    @Select(
            "SELECT t.id as taskId, t.code as taskCode, t.title as taskTitle, lr.id as recordId, lr.quiz_submitted_at as quizSubmittedAt, lr.checkin_time as checkinTime, lr.enter_time as lastEnterTime " +
            "FROM learning_tasks t " +
            "JOIN (" +
            "  SELECT target_id as task_id, MAX(enter_time) as max_enter " +
            "  FROM learning_records " +
            "  WHERE student_id = #{studentId} AND type = 'TASK' " +
            "  GROUP BY target_id" +
            ") x ON x.task_id = t.id " +
            "LEFT JOIN learning_records lr ON lr.student_id = #{studentId} AND lr.type = 'TASK' AND lr.target_id = t.id AND lr.enter_time = x.max_enter " +
            "ORDER BY x.max_enter DESC"
    )
    List<Map<String, Object>> listStudentTouchedTasks(@Param("studentId") Long studentId);

    @Select("SELECT COUNT(DISTINCT target_id) FROM learning_records WHERE student_id = #{studentId} AND type = 'TASK' AND checked_in = 1")
    int countStudentCheckedInTasks(Long studentId);
    
    // 统计学生参与的不同讨论数（只统计讨论，不含任务）
    @Select("SELECT COUNT(DISTINCT discussion_id) FROM discussion_comments WHERE user_id = #{studentId} AND user_type = 'STUDENT'")
    int countStudentDiscussions(Long studentId);
    
    // 统计学生参与的不同任务数
    @Select("SELECT COUNT(DISTINCT task_id) FROM task_comments WHERE user_id = #{studentId} AND user_type = 'STUDENT'")
    int countStudentTasks(Long studentId);
    
    // 统计学生总评论数
    @Select("SELECT (SELECT COUNT(*) FROM discussion_comments WHERE user_id = #{studentId} AND user_type = 'STUDENT') + " +
            "(SELECT COUNT(*) FROM task_comments WHERE user_id = #{studentId} AND user_type = 'STUDENT')")
    int countStudentTotalComments(Long studentId);

    @Select(
            "SELECT " +
                    "sb.student_id as student_id, " +
                    "s.username as student_name, " +
                    "s.real_name as student_real_name, " +
                    "s.major as student_major, " +
                    "sb.bind_time as bind_time, " +
                    "lr.id as record_id, " +
                    "lr.type as type, " +
                    "lr.target_code as target_code, " +
                    "lr.target_title as target_title, " +
                    "lr.duration as duration, " +
                    "lr.enter_time as enter_time, " +
                    "lr.leave_time as leave_time, " +
                    "lr.checked_in as checked_in, " +
                    "lr.checkin_time as checkin_time, " +
                    "lr.quiz_total as quiz_total, " +
                    "lr.quiz_correct as quiz_correct, " +
                    "lr.quiz_submitted_at as quiz_submitted_at, " +
                    "CASE WHEN #{type} = 'TASK' THEN (" +
                    "  SELECT COUNT(*) FROM task_materials tm WHERE tm.task_id = (SELECT id FROM learning_tasks WHERE code = #{code} LIMIT 1)" +
                    ") ELSE NULL END as taskTotalMaterials, " +
                    "CASE WHEN #{type} = 'TASK' THEN (" +
                    "  SELECT COUNT(DISTINCT lr3.target_code) FROM learning_records lr3" +
                    "  WHERE lr3.student_id = sb.student_id AND lr3.type = 'TASK_MATERIAL'" +
                    "    AND lr3.target_id = (SELECT id FROM learning_tasks WHERE code = #{code} LIMIT 1)" +
                    ") ELSE NULL END as taskCompletedMaterials, " +
                    "IFNULL(dcstat.comment_count, 0) as discussion_comment_count, " +
                    "IFNULL(dcstat.reply_count, 0) as discussion_reply_count, " +
                    "dcstat.last_comment_at as discussion_last_comment_at " +
                    "FROM student_bindings sb " +
                    "JOIN students s ON sb.student_id = s.id " +
                    "LEFT JOIN learning_records lr ON lr.id = (" +
                    "  SELECT lr2.id FROM learning_records lr2 " +
                    "  WHERE lr2.student_id = sb.student_id " +
                    "    AND lr2.target_code = #{code} " +
                    "    AND lr2.type = #{type} " +
                    "  ORDER BY lr2.enter_time DESC, lr2.id DESC " +
                    "  LIMIT 1" +
                    ") " +
                    "LEFT JOIN (" +
                    "  SELECT dc.user_id as user_id, " +
                    "         SUM(CASE WHEN dc.parent_id IS NULL THEN 1 ELSE 0 END) as comment_count, " +
                    "         SUM(CASE WHEN dc.parent_id IS NOT NULL THEN 1 ELSE 0 END) as reply_count, " +
                    "         MAX(dc.created_at) as last_comment_at " +
                    "  FROM discussion_comments dc " +
                    "  JOIN discussions d ON dc.discussion_id = d.id " +
                    "  WHERE d.code = #{code} AND dc.user_type = 'STUDENT' " +
                    "  GROUP BY dc.user_id" +
                    ") dcstat ON dcstat.user_id = sb.student_id " +
                    "WHERE sb.teacher_id = #{teacherId} " +
                    "ORDER BY (lr.enter_time IS NULL) ASC, lr.enter_time DESC, sb.bind_time DESC"
    )
    List<StudentParticipationDetail> findTeacherBoundStudentParticipationDetails(
            @Param("teacherId") Long teacherId,
            @Param("code") String code,
            @Param("type") String type
    );

    @Select(
            "SELECT " +
                    "sb.student_id as student_id, " +
                    "s.username as student_name, " +
                    "s.real_name as student_real_name, " +
                    "s.major as student_major, " +
                    "sb.bind_time as bind_time, " +
                    "lr.id as record_id, " +
                    "lr.type as type, " +
                    "lr.target_code as target_code, " +
                    "lr.target_title as target_title, " +
                    "lr.duration as duration, " +
                    "lr.enter_time as enter_time, " +
                    "lr.leave_time as leave_time, " +
                    "lr.checked_in as checked_in, " +
                    "lr.checkin_time as checkin_time, " +
                    "lr.quiz_total as quiz_total, " +
                    "lr.quiz_correct as quiz_correct, " +
                    "lr.quiz_submitted_at as quiz_submitted_at, " +
                    "CASE WHEN #{type} = 'TASK' THEN (" +
                    "  SELECT COUNT(*) FROM task_materials tm WHERE tm.task_id = (SELECT id FROM learning_tasks WHERE code = #{code} LIMIT 1)" +
                    ") ELSE NULL END as taskTotalMaterials, " +
                    "CASE WHEN #{type} = 'TASK' THEN (" +
                    "  SELECT COUNT(DISTINCT lr3.target_code) FROM learning_records lr3" +
                    "  WHERE lr3.student_id = sb.student_id AND lr3.type = 'TASK_MATERIAL'" +
                    "    AND lr3.target_id = (SELECT id FROM learning_tasks WHERE code = #{code} LIMIT 1)" +
                    ") ELSE NULL END as taskCompletedMaterials, " +
                    "IFNULL(dcstat.comment_count, 0) as discussion_comment_count, " +
                    "IFNULL(dcstat.reply_count, 0) as discussion_reply_count, " +
                    "dcstat.last_comment_at as discussion_last_comment_at " +
                    "FROM student_bindings sb " +
                    "JOIN students s ON sb.student_id = s.id " +
                    "LEFT JOIN learning_records lr ON lr.id = (" +
                    "  SELECT lr2.id FROM learning_records lr2 " +
                    "  WHERE lr2.student_id = sb.student_id " +
                    "    AND lr2.target_code = #{code} " +
                    "    AND lr2.type = #{type} " +
                    "  ORDER BY lr2.enter_time DESC, lr2.id DESC " +
                    "  LIMIT 1" +
                    ") " +
                    "LEFT JOIN (" +
                    "  SELECT dc.user_id as user_id, " +
                    "         SUM(CASE WHEN dc.parent_id IS NULL THEN 1 ELSE 0 END) as comment_count, " +
                    "         SUM(CASE WHEN dc.parent_id IS NOT NULL THEN 1 ELSE 0 END) as reply_count, " +
                    "         MAX(dc.created_at) as last_comment_at " +
                    "  FROM discussion_comments dc " +
                    "  JOIN discussions d ON dc.discussion_id = d.id " +
                    "  WHERE d.code = #{code} AND dc.user_type = 'STUDENT' " +
                    "  GROUP BY dc.user_id" +
                    ") dcstat ON dcstat.user_id = sb.student_id " +
                    "WHERE sb.teacher_id = #{teacherId} " +
                    "ORDER BY (lr.enter_time IS NULL) ASC, lr.enter_time DESC, sb.bind_time DESC " +
                    "LIMIT #{limit} OFFSET #{offset}"
    )
    List<StudentParticipationDetail> findTeacherBoundStudentParticipationDetailsPaged(
            @Param("teacherId") Long teacherId,
            @Param("code") String code,
            @Param("type") String type,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    @Select("SELECT COUNT(*) FROM student_bindings WHERE teacher_id = #{teacherId}")
    int countBoundStudentsByTeacher(@Param("teacherId") Long teacherId);

    @Select(
            "SELECT " +
                    "SUM(CASE WHEN (IFNULL(dcstat.comment_count, 0) + IFNULL(dcstat.reply_count, 0)) > 0 THEN 1 ELSE 0 END) as boundStudentParticipantCount, " +
                    "SUM(IFNULL(dcstat.comment_count, 0)) as boundTopLevelCommentCount, " +
                    "SUM(IFNULL(dcstat.reply_count, 0)) as boundReplyCommentCount, " +
                    "MAX(dcstat.last_comment_at) as boundLastCommentAt " +
                    "FROM student_bindings sb " +
                    "LEFT JOIN (" +
                    "  SELECT dc.user_id as user_id, " +
                    "         SUM(CASE WHEN dc.parent_id IS NULL THEN 1 ELSE 0 END) as comment_count, " +
                    "         SUM(CASE WHEN dc.parent_id IS NOT NULL THEN 1 ELSE 0 END) as reply_count, " +
                    "         MAX(dc.created_at) as last_comment_at " +
                    "  FROM discussion_comments dc " +
                    "  JOIN discussions d ON dc.discussion_id = d.id " +
                    "  WHERE d.code = #{code} AND dc.user_type = 'STUDENT' " +
                    "  GROUP BY dc.user_id" +
                    ") dcstat ON dcstat.user_id = sb.student_id " +
                    "WHERE sb.teacher_id = #{teacherId}"
    )
    Map<String, Object> getTeacherBoundDiscussionSummary(
            @Param("teacherId") Long teacherId,
            @Param("code") String code
    );

    @Select(
            "SELECT " +
                    "SUM(CASE WHEN lr.id IS NOT NULL THEN 1 ELSE 0 END) as boundStudentParticipantCount, " +
                    "SUM(CASE WHEN lr.id IS NOT NULL " +
                    "          AND lr.checked_in = 1 " +
                    "          AND lr.quiz_submitted_at IS NOT NULL " +
                    "          AND IFNULL(lr.quiz_total, 0) > 0 " +
                    "          AND ((SELECT COUNT(*) FROM task_materials tm WHERE tm.task_id = (SELECT id FROM learning_tasks WHERE code = #{code} LIMIT 1)) <= 0 " +
                    "               OR IFNULL(mp.done_materials, 0) >= (SELECT COUNT(*) FROM task_materials tm WHERE tm.task_id = (SELECT id FROM learning_tasks WHERE code = #{code} LIMIT 1))) " +
                    "     THEN 1 ELSE 0 END) as boundStudentCompletedCount, " +
                    "(SELECT COUNT(*) FROM task_materials tm WHERE tm.task_id = (SELECT id FROM learning_tasks WHERE code = #{code} LIMIT 1)) as taskTotalMaterials " +
                    "FROM student_bindings sb " +
                    "LEFT JOIN learning_records lr ON lr.id = (" +
                    "  SELECT lr2.id FROM learning_records lr2 " +
                    "  WHERE lr2.student_id = sb.student_id " +
                    "    AND lr2.target_code = #{code} " +
                    "    AND lr2.type = 'TASK' " +
                    "  ORDER BY lr2.enter_time DESC, lr2.id DESC " +
                    "  LIMIT 1" +
                    ") " +
                    "LEFT JOIN (" +
                    "  SELECT lr3.student_id as student_id, COUNT(DISTINCT lr3.target_code) as done_materials " +
                    "  FROM learning_records lr3 " +
                    "  WHERE lr3.type = 'TASK_MATERIAL' " +
                    "    AND lr3.target_id = (SELECT id FROM learning_tasks WHERE code = #{code} LIMIT 1) " +
                    "  GROUP BY lr3.student_id" +
                    ") mp ON mp.student_id = sb.student_id " +
                    "WHERE sb.teacher_id = #{teacherId}"
    )
    Map<String, Object> getTeacherBoundTaskSummary(
            @Param("teacherId") Long teacherId,
            @Param("code") String code
    );

    @Select(
            "SELECT " +
                    "lr.target_id as discussionId, " +
                    "lr.target_code as code, " +
                    "lr.target_title as title, " +
                    "MAX(lr.enter_time) as lastEnterTime, " +
                    "IFNULL(dcstat.comment_count, 0) as myCommentCount, " +
                    "IFNULL(dcstat.reply_count, 0) as myReplyCount, " +
                    "dcstat.last_comment_at as myLastCommentAt " +
                    "FROM learning_records lr " +
                    "LEFT JOIN (" +
                    "  SELECT dc.discussion_id as discussion_id, " +
                    "         SUM(CASE WHEN dc.parent_id IS NULL THEN 1 ELSE 0 END) as comment_count, " +
                    "         SUM(CASE WHEN dc.parent_id IS NOT NULL THEN 1 ELSE 0 END) as reply_count, " +
                    "         MAX(dc.created_at) as last_comment_at " +
                    "  FROM discussion_comments dc " +
                    "  WHERE dc.user_type = 'STUDENT' AND dc.user_id = #{studentId} " +
                    "  GROUP BY dc.discussion_id" +
                    ") dcstat ON dcstat.discussion_id = lr.target_id " +
                    "WHERE lr.student_id = #{studentId} AND lr.type = 'DISCUSSION' " +
                    "GROUP BY lr.target_id, lr.target_code, lr.target_title, dcstat.comment_count, dcstat.reply_count, dcstat.last_comment_at " +
                    "ORDER BY lastEnterTime DESC"
    )
    List<Map<String, Object>> listStudentDiscussionParticipation(@Param("studentId") Long studentId);

    @Select(
            "SELECT " +
                    "CAST(lr.target_code AS UNSIGNED) as materialId, " +
                    "lr.target_title as materialTitle, " +
                    "m.type as materialType, " +
                    "m.category as materialCategory, " +
                    "lr.target_id as taskId, " +
                    "t.code as taskCode, " +
                    "t.title as taskTitle, " +
                    "MAX(lr.enter_time) as lastLearnTime " +
                    "FROM learning_records lr " +
                    "LEFT JOIN materials m ON m.id = CAST(lr.target_code AS UNSIGNED) " +
                    "LEFT JOIN learning_tasks t ON t.id = lr.target_id " +
                    "WHERE lr.student_id = #{studentId} AND lr.type = 'TASK_MATERIAL' " +
                    "GROUP BY CAST(lr.target_code AS UNSIGNED), lr.target_title, m.type, m.category, lr.target_id, t.code, t.title " +
                    "ORDER BY lastLearnTime DESC"
    )
    List<Map<String, Object>> listStudentMaterialFootprints(@Param("studentId") Long studentId);
}
