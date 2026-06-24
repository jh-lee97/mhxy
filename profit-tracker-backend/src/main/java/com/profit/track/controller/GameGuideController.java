package com.profit.track.controller;

import com.profit.track.dto.*;
import com.profit.track.entity.GameGuide;
import com.profit.track.service.GameGuideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "游戏攻略", description = "攻略查询和管理接口")
public class GameGuideController {

    private final GameGuideService gameGuideService;

    // ==================== 公开接口 ====================

    /** 获取攻略列表（可按分类筛选） */
    @GetMapping("/guides")
    @Operation(summary = "获取攻略列表", description = "支持按分类筛选，仅返回启用状态的攻略")
    public Result<List<GameGuideResponse>> listGuides(@RequestParam(required = false) String category) {
        return Result.ok(gameGuideService.listGuides(category));
    }

    /** 获取单个攻略详情 */
    @GetMapping("/guides/{id}")
    @Operation(summary = "获取攻略详情")
    public Result<GameGuideResponse> getGuide(@PathVariable Long id) {
        return Result.ok(gameGuideService.getGuideById(id));
    }

    // ==================== 管理接口 ====================

    /** 创建攻略 */
    @PostMapping("/admin/guides")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建攻略")
    public Result<GameGuideResponse> createGuide(@Valid @RequestBody GameGuideRequest request) {
        return Result.ok(gameGuideService.createGuide(request));
    }

    /** 更新攻略 */
    @PutMapping("/admin/guides/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新攻略")
    public Result<GameGuideResponse> updateGuide(
            @PathVariable Long id,
            @Valid @RequestBody GameGuideRequest request) {
        return Result.ok(gameGuideService.updateGuide(id, request));
    }

    /** 删除攻略 */
    @DeleteMapping("/admin/guides/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除攻略")
    public Result<Void> deleteGuide(@PathVariable Long id) {
        gameGuideService.deleteGuide(id);
        return Result.ok();
    }
}
