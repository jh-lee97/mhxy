package com.profit.track.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.profit.track.dto.GameGuideRequest;
import com.profit.track.dto.GameGuideResponse;
import com.profit.track.entity.GameGuide;
import com.profit.track.mapper.GameGuideMapper;
import com.profit.track.service.GameGuideService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameGuideServiceImpl extends ServiceImpl<GameGuideMapper, GameGuide> implements GameGuideService {

    @Override
    public List<GameGuideResponse> listGuides(String category) {
        LambdaQueryWrapper<GameGuide> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GameGuide::getStatus, 1);
        if (category != null && !category.trim().isEmpty()) {
            wrapper.eq(GameGuide::getCategory, category);
        }
        wrapper.orderByAsc(GameGuide::getSortOrder);
        List<GameGuide> guides = list(wrapper);
        return guides.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public GameGuideResponse getGuideById(Long id) {
        GameGuide guide = getById(id);
        if (guide == null) {
            throw new RuntimeException("攻略不存在");
        }
        return toResponse(guide);
    }

    @Override
    public GameGuideResponse createGuide(GameGuideRequest request) {
        GameGuide guide = new GameGuide();
        BeanUtils.copyProperties(request, guide);
        guide.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        guide.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        LocalDateTime now = LocalDateTime.now();
        guide.setCreatedAt(now);
        guide.setUpdatedAt(now);
        save(guide);
        return toResponse(guide);
    }

    @Override
    public GameGuideResponse updateGuide(Long id, GameGuideRequest request) {
        GameGuide guide = getById(id);
        if (guide == null) {
            throw new RuntimeException("攻略不存在");
        }
        BeanUtils.copyProperties(request, guide, "id", "createdAt");
        guide.setUpdatedAt(LocalDateTime.now());
        updateById(guide);
        return toResponse(guide);
    }

    @Override
    public void deleteGuide(Long id) {
        removeById(id);
    }

    private GameGuideResponse toResponse(GameGuide guide) {
        GameGuideResponse response = new GameGuideResponse();
        response.setId(guide.getId());
        response.setCategory(guide.getCategory());
        response.setTitle(guide.getTitle());
        response.setSummary(guide.getSummary());
        response.setContent(guide.getContent());
        response.setSortOrder(guide.getSortOrder());
        response.setStatus(guide.getStatus());
        response.setCreatedAt(guide.getCreatedAt());
        response.setUpdatedAt(guide.getUpdatedAt());
        return response;
    }
}
