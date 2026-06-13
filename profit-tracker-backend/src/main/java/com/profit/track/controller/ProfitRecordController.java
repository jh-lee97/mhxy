package com.profit.track.controller;

import com.profit.track.dto.*;
import com.profit.track.service.ProfitRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class ProfitRecordController {

    private final ProfitRecordService profitRecordService;

    /** 查询记录列表 */
    @GetMapping
    public Result<List<ProfitRecordResponse>> list(@RequestParam Long userId) {
        try {
            List<ProfitRecordResponse> records = profitRecordService.listRecords(userId);
            return Result.ok(records);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 新增记录 */
    @PostMapping
    public Result<ProfitRecordResponse> add(@RequestBody ProfitRecordRequest request) {
        try {
            ProfitRecordResponse record = profitRecordService.addRecord(request);
            return Result.ok(record);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 更新记录 */
    @PutMapping
    public Result<ProfitRecordResponse> update(@RequestBody ProfitRecordRequest request) {
        try {
            ProfitRecordResponse record = profitRecordService.updateRecord(request);
            return Result.ok(record);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 删除记录 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, @RequestParam Long userId) {
        try {
            profitRecordService.deleteRecord(id, userId);
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 统计信息 */
    @GetMapping("/stats")
    public Result<StatResponse> stats(@RequestParam Long userId) {
        try {
            StatResponse stats = profitRecordService.getStats(userId);
            return Result.ok(stats);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 图表数据（近7日） */
    @GetMapping("/chart")
    public Result<List<ChartResponse>> chart(@RequestParam Long userId) {
        try {
            List<ChartResponse> chartData = profitRecordService.getChartRecords(userId);
            return Result.ok(chartData);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}