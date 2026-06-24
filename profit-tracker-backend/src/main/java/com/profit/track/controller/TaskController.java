package com.profit.track.controller;

import com.profit.track.dto.*;
import com.profit.track.service.TaskPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "每日任务管理", description = "每周任务计划、每日清单和完成记录管理")
public class TaskController {

    private final TaskPlanService taskPlanService;

    /** 获取活跃计划（今日任务） */
    @GetMapping("/active")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "获取活跃计划", description = "获取当前用户的活跃任务计划（含每日任务）")
    public Result<TaskPlanResponse> getActivePlan(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        TaskPlanResponse plan = taskPlanService.getActivePlan(userId);
        if (plan == null) {
            return Result.ok(null);
        }
        return Result.ok(plan);
    }

    /** 获取指定计划详情 */
    @GetMapping("/detail/{planId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "获取计划详情", description = "获取指定计划详情（含每日任务和统计）")
    public Result<TaskPlanResponse> getPlanDetail(
            @PathVariable Long planId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        TaskPlanResponse plan = taskPlanService.getPlanDetail(planId, userId);
        return Result.ok(plan);
    }

    /** 获取本周所有计划 */
    @GetMapping("/weekly")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "获取所有计划", description = "获取用户的所有周任务计划")
    public Result<List<TaskPlanResponse>> getWeeklyPlans(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<TaskPlanResponse> plans = taskPlanService.getWeeklyPlans(userId);
        return Result.ok(plans);
    }

    /** 创建新计划 */
    @PostMapping("/plan")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "创建任务计划", description = "创建一个新的周任务计划，包含每日任务")
    public Result<TaskPlanResponse> createPlan(@RequestBody TaskPlanRequest request, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        TaskPlanResponse plan = taskPlanService.createPlan(request, userId);
        return Result.ok(plan);
    }

    /** 更新计划 */
    @PutMapping("/plan")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "更新任务计划", description = "更新计划信息和每日任务")
    public Result<TaskPlanResponse> updatePlan(@RequestBody TaskPlanRequest request, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        TaskPlanResponse plan = taskPlanService.updatePlan(request, userId);
        return Result.ok(plan);
    }

    /** 删除计划 */
    @DeleteMapping("/plan/{planId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "删除任务计划", description = "删除一个周任务计划及其所有任务")
    public Result<Void> deletePlan(@PathVariable Long planId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        taskPlanService.deletePlan(planId, userId);
        return Result.ok();
    }

    /** 切换计划状态（激活/停用） */
    @PatchMapping("/plan/{planId}/toggle")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "切换计划状态", description = "激活或停用某个计划")
    public Result<Void> togglePlanStatus(@PathVariable Long planId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        taskPlanService.togglePlanStatus(planId, userId);
        return Result.ok();
    }

    /** 完成单个任务 */
    @PostMapping("/completion")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "完成任务", description = "标记某个任务为完成/跳过")
    public Result<TaskCompletionResponse> completeTask(@RequestBody TaskCompletionRequest request, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        TaskCompletionResponse completion = taskPlanService.completeTask(request, userId);
        return Result.ok(completion);
    }

    /** 批量完成某天所有任务 */
    @PostMapping("/batch-complete")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "批量完成任务", description = "批量标记某天所有任务为完成/跳过")
    public Result<List<TaskCompletionResponse>> batchComplete(
            @RequestParam Long planId,
            @RequestParam String date,
            @RequestParam(defaultValue = "1") Integer status,
            @RequestParam(required = false, defaultValue = "") String remark,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        List<TaskCompletionResponse> results = taskPlanService.batchCompleteDay(planId, date, status, remark, userId);
        return Result.ok(results);
    }

    /** 获取某天任务完成情况 */
    @GetMapping("/day/{date}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "获取某天任务", description = "获取指定计划某天的任务完成情况")
    public Result<List<TaskCompletionResponse>> getDayCompletions(
            @RequestParam Long planId,
            @PathVariable String date,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<TaskCompletionResponse> completions = taskPlanService.getDayCompletions(planId, date, userId);
        return Result.ok(completions);
    }

    /** 获取每日统计 */
    @GetMapping("/stats/{planId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "获取每日统计", description = "获取计划内每天的统计信息")
    public Result<List<DailyTaskStats>> getDailyStats(
            @PathVariable Long planId, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<DailyTaskStats> stats = taskPlanService.getDailyStats(planId, userId);
        return Result.ok(stats);
    }

    /** 获取今日统计 */
    @GetMapping("/stats/today")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "获取今日统计", description = "获取活跃计划今日的任务统计")
    public Result<DailyTaskStats> getTodayStats(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        TaskPlanResponse active = taskPlanService.getActivePlan(userId);
        if (active == null || active.getId() == null) {
            return Result.ok(null);
        }
        DailyTaskStats stats = taskPlanService.getTodayStats(active.getId(), userId);
        return Result.ok(stats);
    }

    /** 从请求中获取 userId */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            throw new RuntimeException("未登录，请先登录");
        }
        return (Long) userIdObj;
    }
}
