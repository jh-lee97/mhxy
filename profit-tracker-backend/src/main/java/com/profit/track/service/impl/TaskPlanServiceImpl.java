package com.profit.track.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.profit.track.dto.DailyTaskStats;
import com.profit.track.dto.TaskCompletionRequest;
import com.profit.track.dto.TaskCompletionResponse;
import com.profit.track.dto.TaskPlanRequest;
import com.profit.track.dto.TaskPlanResponse;
import com.profit.track.entity.TaskChecklist;
import com.profit.track.entity.TaskCompletion;
import com.profit.track.entity.TaskPlan;
import com.profit.track.mapper.TaskChecklistMapper;
import com.profit.track.mapper.TaskCompletionMapper;
import com.profit.track.mapper.TaskPlanMapper;
import com.profit.track.service.TaskPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskPlanServiceImpl extends ServiceImpl<TaskPlanMapper, TaskPlan> implements TaskPlanService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final TaskChecklistMapper checklistMapper;
    private final TaskCompletionMapper completionMapper;

    @Override
    public TaskPlanResponse getActivePlan(Long userId) {
        TaskPlan plan = lambdaQuery()
                .eq(TaskPlan::getUserId, userId)
                .eq(TaskPlan::getStatus, 1)
                .orderByDesc(TaskPlan::getStartDate)
                .last("LIMIT 1")
                .one();
        if (plan == null) {
            // 尝试找到最近的一周计划（即使是停用的）
            plan = lambdaQuery()
                    .eq(TaskPlan::getUserId, userId)
                    .orderByDesc(TaskPlan::getStartDate)
                    .last("LIMIT 1")
                    .one();
        }
        if (plan == null) return null;
        return buildPlanResponse(plan, userId);
    }

    @Override
    public TaskPlanResponse getPlanDetail(Long planId, Long userId) {
        TaskPlan plan = getById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new RuntimeException("计划不存在或无权访问");
        }
        return buildPlanResponse(plan, userId);
    }

    @Override
    @Transactional
    public TaskPlanResponse createPlan(TaskPlanRequest request, Long userId) {
        TaskPlan plan = new TaskPlan();
        plan.setUserId(userId);
        plan.setPlanName(request.getPlanName());
        plan.setYear(request.getYear());
        plan.setWeekNumber(request.getWeekNumber());
        plan.setStartDate(request.getStartDate());
        plan.setEndDate(request.getEndDate());
        plan.setStatus(1);
        save(plan);

        // 保存每日任务
        if (request.getDayTasks() != null) {
            for (TaskPlanRequest.DayTask dayTask : request.getDayTasks()) {
                TaskChecklist checklist = new TaskChecklist();
                checklist.setPlanId(plan.getId());
                checklist.setDayOfWeek(dayTask.getDayOfWeek());
                checklist.setTaskName(dayTask.getTaskName());
                checklist.setCategory(dayTask.getCategory());
                checklist.setEstimatedIncome(dayTask.getEstimatedIncome());
                checklist.setEstimatedTime(dayTask.getEstimatedTime());
                checklist.setDescription(dayTask.getDescription());
                checklist.setSortOrder(dayTask.getSortOrder() != null ? dayTask.getSortOrder() : 0);
                checklist.setMandatory(dayTask.getMandatory() != null ? dayTask.getMandatory() : 1);
                checklistMapper.insert(checklist);
            }
        }

        return getPlanDetail(plan.getId(), userId);
    }

    @Override
    @Transactional
    public TaskPlanResponse updatePlan(TaskPlanRequest request, Long userId) {
        TaskPlan plan = getById(request.getId());
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new RuntimeException("计划不存在或无权访问");
        }

        plan.setPlanName(request.getPlanName());
        plan.setYear(request.getYear());
        plan.setWeekNumber(request.getWeekNumber());
        plan.setStartDate(request.getStartDate());
        plan.setEndDate(request.getEndDate());
        plan.setStatus(request.getStatus());
        updateById(plan);

        // 删除旧任务，重新插入
        LambdaQueryWrapper<TaskChecklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskChecklist::getPlanId, plan.getId());
        checklistMapper.delete(wrapper);

        if (request.getDayTasks() != null) {
            for (TaskPlanRequest.DayTask dayTask : request.getDayTasks()) {
                TaskChecklist checklist = new TaskChecklist();
                checklist.setPlanId(plan.getId());
                checklist.setDayOfWeek(dayTask.getDayOfWeek());
                checklist.setTaskName(dayTask.getTaskName());
                checklist.setCategory(dayTask.getCategory());
                checklist.setEstimatedIncome(dayTask.getEstimatedIncome());
                checklist.setEstimatedTime(dayTask.getEstimatedTime());
                checklist.setDescription(dayTask.getDescription());
                checklist.setSortOrder(dayTask.getSortOrder() != null ? dayTask.getSortOrder() : 0);
                checklist.setMandatory(dayTask.getMandatory() != null ? dayTask.getMandatory() : 1);
                checklistMapper.insert(checklist);
            }
        }

        return getPlanDetail(plan.getId(), userId);
    }

    @Override
    @Transactional
    public void deletePlan(Long planId, Long userId) {
        TaskPlan plan = getById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new RuntimeException("计划不存在或无权访问");
        }
        // 删除关联的任务和完成记录
        LambdaQueryWrapper<TaskChecklist> cw = new LambdaQueryWrapper<>();
        cw.eq(TaskChecklist::getPlanId, planId);
        checklistMapper.delete(cw);

        LambdaQueryWrapper<TaskCompletion> pw = new LambdaQueryWrapper<>();
        pw.eq(TaskCompletion::getPlanId, planId);
        completionMapper.delete(pw);

        removeById(planId);
    }

    @Override
    public void togglePlanStatus(Long planId, Long userId) {
        TaskPlan plan = getById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new RuntimeException("计划不存在或无权访问");
        }
        plan.setStatus(plan.getStatus() == 1 ? 0 : 1);
        updateById(plan);
    }

    @Override
    public List<TaskPlanResponse> getWeeklyPlans(Long userId) {
        List<TaskPlan> plans = lambdaQuery()
                .eq(TaskPlan::getUserId, userId)
                .orderByDesc(TaskPlan::getStartDate)
                .list();
        return plans.stream()
                .map(p -> buildPlanResponse(p, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaskCompletionResponse completeTask(TaskCompletionRequest request, Long userId) {
        TaskChecklist checklist = checklistMapper.selectById(request.getChecklistId());
        if (checklist == null) {
            throw new RuntimeException("任务不存在");
        }

        TaskPlan plan = getById(checklist.getPlanId());
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new RuntimeException("计划不存在或无权访问");
        }

        // 查找或创建完成记录
        LambdaQueryWrapper<TaskCompletion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskCompletion::getChecklistId, checklist.getId())
                .eq(TaskCompletion::getCompleteDate, request.getCompleteDate());
        TaskCompletion existing = completionMapper.selectOne(wrapper);

        TaskCompletion completion;
        if (existing != null) {
            completion = existing;
            completion.setStatus(request.getStatus());
            completion.setActualIncome(request.getActualIncome());
            completion.setActualTime(request.getActualTime());
            completion.setRemark(request.getRemark());
            if (request.getStatus() == 1 && completion.getCompletedAt() == null) {
                completion.setCompletedAt(java.time.LocalDateTime.now());
            }
            completionMapper.updateById(completion);
        } else {
            completion = new TaskCompletion();
            completion.setChecklistId(checklist.getId());
            completion.setPlanId(checklist.getPlanId());
            completion.setUserId(userId);
            completion.setCompleteDate(request.getCompleteDate());
            completion.setActualIncome(request.getActualIncome());
            completion.setActualTime(request.getActualTime());
            completion.setStatus(request.getStatus());
            completion.setRemark(request.getRemark());
            if (request.getStatus() == 1) {
                completion.setCompletedAt(java.time.LocalDateTime.now());
            }
            completionMapper.insert(completion);
        }

        return toCompletionResponse(completion, checklist);
    }

    @Override
    @Transactional
    public List<TaskCompletionResponse> batchCompleteDay(Long planId, String date, Integer status, String remark, Long userId) {
        TaskPlan plan = getById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new RuntimeException("计划不存在或无权访问");
        }

        // 计算该日期是星期几
        LocalDate ld = LocalDate.parse(date, DATE_FMT);
        int dayOfWeek = ld.getDayOfWeek().getValue(); // 1=Mon, 7=Sun

        // 获取该天的所有任务
        List<TaskChecklist> dayTasks = checklistMapper.selectList(
                new LambdaQueryWrapper<TaskChecklist>()
                        .eq(TaskChecklist::getPlanId, planId)
                        .eq(TaskChecklist::getDayOfWeek, dayOfWeek)
                        .orderByAsc(TaskChecklist::getSortOrder)
        );

        List<TaskCompletionResponse> results = new ArrayList<>();
        for (TaskChecklist checklist : dayTasks) {
            TaskCompletionRequest req = new TaskCompletionRequest();
            req.setChecklistId(checklist.getId());
            req.setCompleteDate(date);
            req.setStatus(status);
            req.setRemark(remark);
            results.add(completeTask(req, userId));
        }
        return results;
    }

    @Override
    public List<TaskCompletionResponse> getDayCompletions(Long planId, String date, Long userId) {
        TaskPlan plan = getById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new RuntimeException("计划不存在或无权访问");
        }

        LocalDate ld = LocalDate.parse(date, DATE_FMT);
        int dayOfWeek = ld.getDayOfWeek().getValue();

        List<TaskChecklist> dayTasks = checklistMapper.selectList(
                new LambdaQueryWrapper<TaskChecklist>()
                        .eq(TaskChecklist::getPlanId, planId)
                        .eq(TaskChecklist::getDayOfWeek, dayOfWeek)
                        .orderByAsc(TaskChecklist::getSortOrder)
        );

        List<TaskCompletionResponse> results = new ArrayList<>();
        for (TaskChecklist checklist : dayTasks) {
            LambdaQueryWrapper<TaskCompletion> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TaskCompletion::getChecklistId, checklist.getId())
                    .eq(TaskCompletion::getCompleteDate, date);
            TaskCompletion completion = completionMapper.selectOne(wrapper);

            if (completion != null) {
                results.add(toCompletionResponse(completion, checklist));
            } else {
                // 返回空记录（未开始）
                TaskCompletion empty = new TaskCompletion();
                empty.setChecklistId(checklist.getId());
                empty.setStatus(0);
                results.add(toCompletionResponse(empty, checklist));
            }
        }
        return results;
    }

    @Override
    public List<DailyTaskStats> getDailyStats(Long planId, Long userId) {
        TaskPlan plan = getById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new RuntimeException("计划不存在或无权访问");
        }

        List<DailyTaskStats> stats = new ArrayList<>();
        LocalDate start = LocalDate.parse(plan.getStartDate(), DATE_FMT);
        LocalDate end = LocalDate.parse(plan.getEndDate(), DATE_FMT);

        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            String dateStr = d.format(DATE_FMT);
            int dayOfWeek = d.getDayOfWeek().getValue();
            DailyTaskStats dayStats = new DailyTaskStats();
            dayStats.setDate(dateStr);
            dayStats.setDayOfWeek(dayOfWeek);
            stats.add(dayStats);
        }

        // 填充统计数据
        for (DailyTaskStats dayStats : stats) {
            fillDailyStats(dayStats, planId, dayStats.getDate());
        }

        return stats;
    }

    @Override
    public DailyTaskStats getTodayStats(Long planId, Long userId) {
        String today = LocalDate.now().format(DATE_FMT);
        DailyTaskStats stats = new DailyTaskStats();
        stats.setDate(today);
        stats.setDayOfWeek(LocalDate.now().getDayOfWeek().getValue());
        fillDailyStats(stats, planId, today);
        return stats;
    }

    /** 填充单日统计数据 */
    private void fillDailyStats(DailyTaskStats dayStats, Long planId, String date) {
        LocalDate ld = LocalDate.parse(date, DATE_FMT);
        int dayOfWeek = ld.getDayOfWeek().getValue();

        // 获取当天所有任务
        List<TaskChecklist> dayTasks = checklistMapper.selectList(
                new LambdaQueryWrapper<TaskChecklist>()
                        .eq(TaskChecklist::getPlanId, planId)
                        .eq(TaskChecklist::getDayOfWeek, dayOfWeek)
                        .orderByAsc(TaskChecklist::getSortOrder)
        );

        dayStats.setTotalTasks(dayTasks.size());

        int completed = 0;
        int skipped = 0;
        BigDecimal totalEstimatedIncome = BigDecimal.ZERO;
        BigDecimal totalActualIncome = BigDecimal.ZERO;
        BigDecimal totalEstimatedTime = BigDecimal.ZERO;
        BigDecimal totalActualTime = BigDecimal.ZERO;

        for (TaskChecklist checklist : dayTasks) {
            if (checklist.getEstimatedIncome() != null) {
                totalEstimatedIncome = totalEstimatedIncome.add(checklist.getEstimatedIncome());
            }
            if (checklist.getEstimatedTime() != null) {
                totalEstimatedTime = totalEstimatedTime.add(checklist.getEstimatedTime());
            }

            // 查找完成记录
            LambdaQueryWrapper<TaskCompletion> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TaskCompletion::getChecklistId, checklist.getId())
                    .eq(TaskCompletion::getCompleteDate, date);
            TaskCompletion completion = completionMapper.selectOne(wrapper);

            if (completion != null) {
                if (completion.getStatus() == 1) {
                    completed++;
                    if (completion.getActualIncome() != null) {
                        totalActualIncome = totalActualIncome.add(completion.getActualIncome());
                    }
                    if (completion.getActualTime() != null) {
                        totalActualTime = totalActualTime.add(completion.getActualTime());
                    }
                } else if (completion.getStatus() == 2) {
                    skipped++;
                }
            }
        }

        dayStats.setCompletedTasks(completed);
        dayStats.setSkippedTasks(skipped);
        dayStats.setEstimatedIncome(totalEstimatedIncome);
        dayStats.setActualIncome(totalActualIncome);
        dayStats.setEstimatedTime(totalEstimatedTime);
        dayStats.setActualTime(totalActualTime);

        if (dayStats.getTotalTasks() > 0) {
            dayStats.setCompletionRate((double) completed / dayStats.getTotalTasks() * 100);
        }
    }

    /** 构建计划响应 */
    private TaskPlanResponse buildPlanResponse(TaskPlan plan, Long userId) {
        TaskPlanResponse resp = new TaskPlanResponse();
        resp.setId(plan.getId());
        resp.setUserId(plan.getUserId());
        resp.setPlanName(plan.getPlanName());
        resp.setYear(plan.getYear());
        resp.setWeekNumber(plan.getWeekNumber());
        resp.setStartDate(plan.getStartDate());
        resp.setEndDate(plan.getEndDate());
        resp.setStatus(plan.getStatus());
        resp.setCreatedAt(plan.getCreatedAt());
        resp.setUpdatedAt(plan.getUpdatedAt());

        // 获取每日任务
        List<TaskChecklist> allTasks = checklistMapper.selectList(
                new LambdaQueryWrapper<TaskChecklist>()
                        .eq(TaskChecklist::getPlanId, plan.getId())
                        .orderByAsc(TaskChecklist::getDayOfWeek)
                        .orderByAsc(TaskChecklist::getSortOrder)
        );

        // 按星期几分组统计
        Map<Integer, List<TaskChecklist>> grouped = allTasks.stream()
                .collect(Collectors.groupingBy(TaskChecklist::getDayOfWeek));

        int todayDayOfWeek = LocalDate.now().getDayOfWeek().getValue();
        String todayStr = LocalDate.now().format(DATE_FMT);

        // 构建每日响应（包含当天该星期的所有任务）
        List<TaskPlanResponse.DayTaskResponse> dayTaskResponses = new ArrayList<>();
        for (int day = 1; day <= 7; day++) {
            List<TaskChecklist> tasks = grouped.getOrDefault(day, Collections.emptyList());
            if (tasks.isEmpty()) continue;

            TaskPlanResponse.DayTaskResponse dayResp = new TaskPlanResponse.DayTaskResponse();
            dayResp.setDayOfWeek(day);
            dayResp.setTotalCount(tasks.size());

            int completedCount = 0;
            for (TaskChecklist tc : tasks) {
                // 如果今天是这个星期几，查今天的完成记录
                if (day == todayDayOfWeek) {
                    LambdaQueryWrapper<TaskCompletion> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(TaskCompletion::getChecklistId, tc.getId())
                            .eq(TaskCompletion::getCompleteDate, todayStr)
                            .eq(TaskCompletion::getStatus, 1);
                    if (completionMapper.selectCount(wrapper) > 0) {
                        completedCount++;
                    }
                }
            }
            dayResp.setCompletedCount(completedCount);
            dayResp.setCompletionRate(tasks.isEmpty() ? 0.0 : (double) completedCount / tasks.size() * 100);
            dayTaskResponses.add(dayResp);
        }

        // 构建所有任务的扁平列表（供前端逐个操作）
        List<TaskPlanResponse.DayTaskResponse> allFlatTasks = new ArrayList<>();
        for (TaskChecklist tc : allTasks) {
            TaskPlanResponse.DayTaskResponse taskResp = new TaskPlanResponse.DayTaskResponse();
            taskResp.setId(tc.getId());
            taskResp.setDayOfWeek(tc.getDayOfWeek());
            taskResp.setTaskName(tc.getTaskName());
            taskResp.setCategory(tc.getCategory());
            taskResp.setEstimatedIncome(tc.getEstimatedIncome());
            taskResp.setEstimatedTime(tc.getEstimatedTime());
            taskResp.setDescription(tc.getDescription());
            taskResp.setSortOrder(tc.getSortOrder());
            taskResp.setMandatory(tc.getMandatory());

            // 统计完成数
            if (tc.getDayOfWeek() == todayDayOfWeek) {
                LambdaQueryWrapper<TaskCompletion> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(TaskCompletion::getChecklistId, tc.getId())
                        .eq(TaskCompletion::getCompleteDate, todayStr)
                        .eq(TaskCompletion::getStatus, 1);
                if (completionMapper.selectCount(wrapper) > 0) {
                    taskResp.setCompletedCount(1);
                }
            }
            taskResp.setTotalCount(1);
            taskResp.setCompletionRate(taskResp.getCompletedCount() != null && taskResp.getCompletedCount() > 0 ? 100.0 : 0.0);

            allFlatTasks.add(taskResp);
        }

        // 设置两个列表：dayTasks 按天分组（用于管理页面），allTasks 扁平列表（用于工作台）
        resp.setDayTasks(dayTaskResponses);
        return resp;
    }

    /** 转换为完成记录响应 */
    private TaskCompletionResponse toCompletionResponse(TaskCompletion completion, TaskChecklist checklist) {
        TaskCompletionResponse resp = new TaskCompletionResponse();
        resp.setId(completion.getId());
        resp.setChecklistId(completion.getChecklistId());
        resp.setTaskName(checklist != null ? checklist.getTaskName() : "");
        resp.setCategory(checklist != null ? checklist.getCategory() : "");
        resp.setDayOfWeek(checklist != null ? checklist.getDayOfWeek() : 0);
        resp.setCompleteDate(completion.getCompleteDate());
        resp.setActualIncome(completion.getActualIncome());
        resp.setActualTime(completion.getActualTime());
        resp.setStatus(completion.getStatus());
        resp.setCompletedAt(completion.getCompletedAt());
        resp.setRemark(completion.getRemark());
        resp.setCreatedAt(completion.getCreatedAt());
        return resp;
    }
}
