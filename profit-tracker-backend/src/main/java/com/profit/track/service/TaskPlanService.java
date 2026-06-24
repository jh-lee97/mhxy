package com.profit.track.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.profit.track.dto.DailyTaskStats;
import com.profit.track.dto.TaskCompletionRequest;
import com.profit.track.dto.TaskCompletionResponse;
import com.profit.track.dto.TaskPlanRequest;
import com.profit.track.dto.TaskPlanResponse;
import com.profit.track.entity.TaskPlan;
import java.util.List;

public interface TaskPlanService extends IService<TaskPlan> {

    /**
     * 获取用户的活跃计划（最新的一个进行中的计划）
     */
    TaskPlanResponse getActivePlan(Long userId);

    /**
     * 获取指定计划详情（含每日任务和统计）
     */
    TaskPlanResponse getPlanDetail(Long planId, Long userId);

    /**
     * 创建新计划
     */
    TaskPlanResponse createPlan(TaskPlanRequest request, Long userId);

    /**
     * 更新计划
     */
    TaskPlanResponse updatePlan(TaskPlanRequest request, Long userId);

    /**
     * 删除计划
     */
    void deletePlan(Long planId, Long userId);

    /**
     * 停用/激活计划
     */
    void togglePlanStatus(Long planId, Long userId);

    /**
     * 获取本周所有计划（按周数排序）
     */
    List<TaskPlanResponse> getWeeklyPlans(Long userId);

    /**
     * 完成/更新某天的任务
     */
    TaskCompletionResponse completeTask(TaskCompletionRequest request, Long userId);

    /**
     * 批量完成某天所有任务
     */
    List<TaskCompletionResponse> batchCompleteDay(Long planId, String date, Integer status, String remark, Long userId);

    /**
     * 获取某天的任务完成情况
     */
    List<TaskCompletionResponse> getDayCompletions(Long planId, String date, Long userId);

    /**
     * 获取每日任务统计
     */
    List<DailyTaskStats> getDailyStats(Long planId, Long userId);

    /**
     * 获取今日任务概览
     */
    DailyTaskStats getTodayStats(Long planId, Long userId);
}
