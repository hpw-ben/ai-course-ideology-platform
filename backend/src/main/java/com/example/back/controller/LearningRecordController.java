package com.example.back.controller;

import com.example.back.dto.CheckinTaskRequest;
import com.example.back.dto.HeartbeatRequest;
import com.example.back.dto.LeaveRequest;
import com.example.back.dto.MarkTaskMaterialCompletedRequest;
import com.example.back.dto.RecordEnterRequest;
import com.example.back.dto.Result;
import com.example.back.dto.SaveTaskViewpointAndNoteRequest;
import com.example.back.dto.SubmitTaskQuizRequest;
import com.example.back.entity.LearningRecord;
import com.example.back.service.LearningRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learning-record")
public class LearningRecordController {

    @Autowired
    private LearningRecordService recordService;

    @GetMapping("/teacher/bound-participation")
    public Result<Map<String, Object>> getTeacherBoundStudentParticipationDetails(
            @RequestParam Long teacherId,
            @RequestParam String code,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) {
        Map<String, Object> r = recordService.getTeacherBoundStudentParticipationDetails(teacherId, code, type, page, pageSize);
        return Result.success(r);
    }

    // 记录进入学习（学习轨迹）
    @PostMapping("/enter")
    public Result<LearningRecord> recordEnter(@Valid @RequestBody RecordEnterRequest req) {
        LearningRecord record = recordService.recordEnter(
                req.getStudentId(),
                req.getType(),
                req.getTargetId(),
                req.getTargetCode(),
                req.getTargetTitle()
        );
        return Result.success(record);
    }

    @GetMapping("/detail/{id}")
    public Result<LearningRecord> getById(@PathVariable Long id) {
        LearningRecord r = recordService.getById(id);
        if (r == null) return Result.error("学习记录不存在");
        return Result.success(r);
    }

    @GetMapping("/task/checkin-status")
    public Result<Map<String, Object>> getTaskCheckinStatus(@RequestParam Long studentId, @RequestParam Long taskId) {
        return Result.success(recordService.getTaskCheckinStatus(studentId, taskId));
    }

    @PostMapping("/task/checkin")
    public Result<Void> checkinTask(@Valid @RequestBody CheckinTaskRequest req) {
        recordService.checkinTask(
                req.getRecordId(),
                req.getTaskId(),
                req.getDuration(),
                req.getMaterialDuration(),
                req.getInteractionDuration()
        );
        return Result.success();
    }

    // 记录离开学习（暂不使用）
    @PostMapping("/leave")
    public Result<Void> recordLeave(@Valid @RequestBody LeaveRequest req) {
        recordService.leave(req.getRecordId(), req.getDuration(), req.getMaterialDuration(), req.getInteractionDuration());
        return Result.success();
    }

    @PostMapping("/heartbeat")
    public Result<Void> heartbeat(@Valid @RequestBody HeartbeatRequest req) {
        recordService.heartbeat(req.getRecordId(), req.getDuration(), req.getMaterialDuration(), req.getInteractionDuration());
        return Result.success();
    }

    @PostMapping("/task/submit-quiz")
    public Result<Map<String, Object>> submitTaskQuiz(@Valid @RequestBody SubmitTaskQuizRequest req) {
        Map<String, Object> r = recordService.submitTaskQuiz(
                req.getRecordId(),
                req.getTaskId(),
                req.getTaskCode(),
                req.getAnswers(),
                req.getReflection()
        );
        return Result.success(r);
    }

    @PostMapping("/task/material/complete")
    public Result<Map<String, Object>> markTaskMaterialCompleted(@Valid @RequestBody MarkTaskMaterialCompletedRequest req) {
        Map<String, Object> r = recordService.markTaskMaterialCompleted(req.getStudentId(), req.getTaskId(), req.getMaterialId());
        return Result.success(r);
    }

    @GetMapping("/task/progress")
    public Result<Map<String, Object>> getTaskProgress(@RequestParam Long studentId, @RequestParam Long taskId) {
        return Result.success(recordService.getTaskProgress(studentId, taskId));
    }

    @GetMapping("/task/completions")
    public Result<List<Map<String, Object>>> listStudentTaskCompletions(@RequestParam Long studentId,
                                                                       @RequestParam(required = false) String status) {
        return Result.success(recordService.listStudentTaskCompletions(studentId, status));
    }

    @PostMapping("/task/viewpoint-note")
    public Result<LearningRecord> saveTaskViewpointAndNote(@Valid @RequestBody SaveTaskViewpointAndNoteRequest req) {
        LearningRecord r = recordService.saveTaskViewpointAndNote(
                req.getRecordId(),
                req.getStudentId(),
                req.getViewpointChoice(),
                req.getShortNote()
        );
        return Result.success(r);
    }

    @GetMapping("/report/{recordId}/pdf")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long recordId) {
        try {
            byte[] pdf = recordService.generateReportPdf(recordId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment().filename("learning-report-" + recordId + ".pdf").build());
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("text", "plain", StandardCharsets.UTF_8));
            String msg = e.getMessage() == null ? "生成失败" : e.getMessage();
            return new ResponseEntity<>(msg.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.BAD_REQUEST);
        }
    }

    // 获取学生学习轨迹
    @GetMapping("/student/{studentId}")
    public Result<List<LearningRecord>> getByStudent(@PathVariable Long studentId) {
        List<LearningRecord> records = recordService.getByStudent(studentId);
        return Result.success(records);
    }

    // 获取学生学习统计（从评论表统计）
    @GetMapping("/stats/{studentId}")
    public Result<Map<String, Object>> getStats(@PathVariable Long studentId) {
        Map<String, Object> stats = recordService.getStudentStats(studentId);
        return Result.success(stats);
    }

    @GetMapping("/student/{studentId}/discussion-participation")
    public Result<List<Map<String, Object>>> listStudentDiscussionParticipation(@PathVariable Long studentId) {
        return Result.success(recordService.listStudentDiscussionParticipation(studentId));
    }

    @GetMapping("/student/{studentId}/material-footprints")
    public Result<List<Map<String, Object>>> listStudentMaterialFootprints(@PathVariable Long studentId) {
        return Result.success(recordService.listStudentMaterialFootprints(studentId));
    }
}
