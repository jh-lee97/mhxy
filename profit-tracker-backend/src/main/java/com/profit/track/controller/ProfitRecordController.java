package com.profit.track.controller;

import com.profit.track.dto.*;
import com.profit.track.service.ProfitRecordService;
import com.profit.track.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
@Tag(name = "收益记录管理", description = "收益记录的增删改查及统计分析")
public class ProfitRecordController {

    private final ProfitRecordService profitRecordService;
    private final SysUserService sysUserService;

    /** 查询记录列表 */
    @GetMapping
    @Operation(summary = "查询记录列表", description = "根据当前登录用户查询所有收益记录，按日期降序")
    public Result<List<ProfitRecordResponse>> list(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            Integer roleLevel = getRoleLevelFromRequest(request);
            List<ProfitRecordResponse> records = profitRecordService.listRecords(userId, roleLevel);
            return Result.ok(records);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 新增记录 */
    @PostMapping
    @Operation(summary = "新增记录", description = "新增一条收益记录")
    public Result<ProfitRecordResponse> add(@RequestBody ProfitRecordRequest request, HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            request.setUserId(userId);
            ProfitRecordResponse record = profitRecordService.addRecord(request);
            return Result.ok(record);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 更新记录 */
    @PutMapping
    @Operation(summary = "更新记录", description = "更新一条收益记录")
    public Result<ProfitRecordResponse> update(@RequestBody ProfitRecordRequest request, HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            Integer roleLevel = getRoleLevelFromRequest(httpRequest);
            request.setUserId(userId);
            ProfitRecordResponse record = profitRecordService.updateRecord(request, userId, roleLevel);
            return Result.ok(record);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 删除记录 */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除记录", description = "根据 ID 删除一条收益记录")
    public Result<Void> delete(
            @Parameter(description = "记录ID", required = true) @PathVariable Long id,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            Integer roleLevel = getRoleLevelFromRequest(request);
            profitRecordService.deleteRecord(id, userId, roleLevel);
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 统计信息 */
    @GetMapping("/stats")
    @Operation(summary = "统计信息", description = "获取今日/本周/累计的收入、成本、利润统计")
    public Result<StatResponse> stats(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            Integer roleLevel = getRoleLevelFromRequest(request);
            StatResponse stats = profitRecordService.getStats(userId, roleLevel);
            return Result.ok(stats);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 图表数据（近7日） */
    @GetMapping("/chart")
    @Operation(summary = "图表数据", description = "获取近7日的收入、成本、净利润趋势数据")
    public Result<List<ChartResponse>> chart(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            Integer roleLevel = getRoleLevelFromRequest(request);
            List<ChartResponse> chartData = profitRecordService.getChartRecords(userId, roleLevel);
            return Result.ok(chartData);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 从请求中获取 userId */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            throw new RuntimeException("未登录，请先登录");
        }
        return (Long) userIdObj;
    }

    /** 从请求中获取角色等级 */
    private Integer getRoleLevelFromRequest(HttpServletRequest request) {
        Object roleLevelObj = request.getAttribute("roleLevel");
        if (roleLevelObj == null) {
            throw new RuntimeException("未获取到角色信息");
        }
        return (Integer) roleLevelObj;
    }
}