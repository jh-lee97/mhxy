package com.profit.track.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.profit.track.dto.GameGuideRequest;
import com.profit.track.dto.GameGuideResponse;
import com.profit.track.entity.GameGuide;

import java.util.List;

public interface GameGuideService extends IService<GameGuide> {

    /** 获取攻略列表（可按分类筛选） */
    List<GameGuideResponse> listGuides(String category);

    /** 获取单个攻略详情 */
    GameGuideResponse getGuideById(Long id);

    /** 创建攻略 */
    GameGuideResponse createGuide(GameGuideRequest request);

    /** 更新攻略 */
    GameGuideResponse updateGuide(Long id, GameGuideRequest request);

    /** 删除攻略 */
    void deleteGuide(Long id);
}
