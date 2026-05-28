package com.example.back.mapper;

import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface TaskCompletionMapper {

    @Insert("INSERT INTO task_material_progress(student_id, task_id, material_id, completed_at) " +
            "VALUES(#{studentId}, #{taskId}, #{materialId}, #{completedAt}) " +
            "ON DUPLICATE KEY UPDATE completed_at = VALUES(completed_at)")
    int upsertMaterialProgress(@Param("studentId") Long studentId,
                              @Param("taskId") Long taskId,
                              @Param("materialId") Long materialId,
                              @Param("completedAt") LocalDateTime completedAt);

    @Select("SELECT material_id FROM task_material_progress WHERE student_id = #{studentId} AND task_id = #{taskId}")
    List<Long> findCompletedMaterialIds(@Param("studentId") Long studentId, @Param("taskId") Long taskId);

    @Select("SELECT COUNT(*) FROM task_material_progress WHERE student_id = #{studentId} AND task_id = #{taskId}")
    int countCompletedMaterials(@Param("studentId") Long studentId, @Param("taskId") Long taskId);

    @Insert("INSERT INTO student_task_completion(student_id, task_id, status, completed_at) " +
            "VALUES(#{studentId}, #{taskId}, #{status}, #{completedAt}) " +
            "ON DUPLICATE KEY UPDATE status = VALUES(status), completed_at = VALUES(completed_at)")
    int upsertStudentTaskCompletion(@Param("studentId") Long studentId,
                                   @Param("taskId") Long taskId,
                                   @Param("status") String status,
                                   @Param("completedAt") LocalDateTime completedAt);

    @Select("SELECT status FROM student_task_completion WHERE student_id = #{studentId} AND task_id = #{taskId}")
    String findStudentTaskStatus(@Param("studentId") Long studentId, @Param("taskId") Long taskId);

    @Select("SELECT stc.task_id as taskId, t.code as taskCode, t.title as taskTitle, stc.status as status, stc.completed_at as completedAt " +
            "FROM student_task_completion stc " +
            "LEFT JOIN learning_tasks t ON t.id = stc.task_id " +
            "WHERE stc.student_id = #{studentId} AND stc.status = #{status} " +
            "ORDER BY stc.updated_at DESC")
    List<Map<String, Object>> listStudentTasksByStatus(@Param("studentId") Long studentId, @Param("status") String status);
}
