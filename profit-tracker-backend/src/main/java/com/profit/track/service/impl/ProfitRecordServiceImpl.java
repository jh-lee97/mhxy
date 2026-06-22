package com.profit.track.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.profit.track.dto.ChartResponse;
import com.profit.track.dto.ProfitRecordRequest;
import com.profit.track.dto.ProfitRecordResponse;
import com.profit.track.dto.StatResponse;
import com.profit.track.entity.ProfitRecord;
import com.profit.track.mapper.ProfitRecordMapper;
import com.profit.track.mapper.SysUserMapper;
import com.profit.track.service.ProfitRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfitRecordServiceImpl extends ServiceImpl<ProfitRecordMapper, ProfitRecord> implements ProfitRecordService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysUserMapper sysUserMapper;

    @Override
    public List<ProfitRecordResponse> listRecords(Long userId) {
        // 权限检查已在 Controller 的 @PreAuthorize 中处理
        // 管理员查看所有，普通用户只看自己的
        // 这里通过判断用户角色来决定查询范围
        boolean isAdmin = isAdmin(userId);
        
        List<ProfitRecord> records;
        if (isAdmin) {
            records = list();
        } else {
            records = lambdaQuery()
                    .eq(ProfitRecord::getUserId, userId)
                    .orderByDesc(ProfitRecord::getDate)
                    .orderByDesc(ProfitRecord::getCreatedAt)
                    .list();
        }
        return records.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public ProfitRecordResponse addRecord(ProfitRecordRequest request) {
        ProfitRecord record = new ProfitRecord();
        BeanUtils.copyProperties(request, record);
        String now = LocalDateTime.now().format(ISO_FMT);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        save(record);
        return toResponse(record);
    }

    @Override
    public ProfitRecordResponse updateRecord(ProfitRecordRequest request, Long userId) {
        ProfitRecord existing = getById(request.getId());
        if (existing == null) {
            throw new RuntimeException("记录不存在");
        }
        // 检查权限：管理员或记录所有者
        if (!isAdmin(userId) && !existing.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改他人的记录");
        }
        BeanUtils.copyProperties(request, existing, "id", "createdAt");
        existing.setUpdatedAt(LocalDateTime.now().format(ISO_FMT));
        updateById(existing);
        return toResponse(existing);
    }

    @Override
    public void deleteRecord(Long id, Long userId) {
        ProfitRecord existing = getById(id);
        if (existing == null) {
            throw new RuntimeException("记录不存在");
        }
        // 检查权限：管理员或记录所有者
        if (!isAdmin(userId) && !existing.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除他人的记录");
        }
        removeById(id);
    }

    @Override
    public StatResponse getStats(Long userId) {
        boolean isAdmin = isAdmin(userId);
        List<ProfitRecord> allRecords;
        
        if (isAdmin) {
            allRecords = list();
        } else {
            allRecords = lambdaQuery()
                    .eq(ProfitRecord::getUserId, userId)
                    .list();
        }

        String today = LocalDate.now().format(DATE_FMT);
        LocalDate weekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY);

        BigDecimal todayIncome = BigDecimal.ZERO;
        BigDecimal todayCost = BigDecimal.ZERO;
        BigDecimal weekIncome = BigDecimal.ZERO;
        BigDecimal weekCost = BigDecimal.ZERO;
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (ProfitRecord r : allRecords) {
            BigDecimal inc = r.getIncome() != null ? r.getIncome() : BigDecimal.ZERO;
            BigDecimal cbg = r.getCbgIncome() != null ? r.getCbgIncome() : BigDecimal.ZERO;
            BigDecimal prop = r.getPropIncome() != null ? r.getPropIncome() : BigDecimal.ZERO;
            BigDecimal cost = r.getCost() != null ? r.getCost() : BigDecimal.ZERO;
            BigDecimal dayIncome = inc.add(cbg).add(prop);

            totalIncome = totalIncome.add(dayIncome);
            totalCost = totalCost.add(cost);

            if (r.getDate().equals(today)) {
                todayIncome = todayIncome.add(dayIncome);
                todayCost = todayCost.add(cost);
            }

            if (r.getDate() != null && (r.getDate().compareTo(weekStart.toString()) >= 0)) {
                weekIncome = weekIncome.add(dayIncome);
                weekCost = weekCost.add(cost);
            }
        }

        StatResponse resp = new StatResponse();
        resp.setTodayIncome(todayIncome);
        resp.setTodayCost(todayCost);
        resp.setTodayProfit(todayIncome.subtract(todayCost));
        resp.setWeekIncome(weekIncome);
        resp.setWeekCost(weekCost);
        resp.setWeekProfit(weekIncome.subtract(weekCost));
        resp.setTotalIncome(totalIncome);
        resp.setTotalCost(totalCost);
        resp.setTotalProfit(totalIncome.subtract(totalCost));
        resp.setTotalRecords((long) allRecords.size());
        return resp;
    }

    @Override
    public List<ChartResponse> getChartRecords(Long userId) {
        boolean isAdmin = isAdmin(userId);
        List<ProfitRecord> allRecords;
        
        if (isAdmin) {
            allRecords = list();
        } else {
            allRecords = lambdaQuery()
                    .eq(ProfitRecord::getUserId, userId)
                    .list();
        }

        List<ChartResponse> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            String dateStr = d.format(DATE_FMT);

            BigDecimal income = BigDecimal.ZERO;
            BigDecimal cost = BigDecimal.ZERO;

            for (ProfitRecord r : allRecords) {
                if (dateStr.equals(r.getDate())) {
                    income = income.add(r.getIncome() != null ? r.getIncome() : BigDecimal.ZERO)
                            .add(r.getCbgIncome() != null ? r.getCbgIncome() : BigDecimal.ZERO)
                            .add(r.getPropIncome() != null ? r.getPropIncome() : BigDecimal.ZERO);
                    cost = cost.add(r.getCost() != null ? r.getCost() : BigDecimal.ZERO);
                }
            }

            ChartResponse resp = new ChartResponse();
            resp.setDate(dateStr);
            resp.setIncome(income);
            resp.setCost(cost);
            resp.setProfit(income.subtract(cost));
            result.add(resp);
        }
        return result;
    }

    private ProfitRecordResponse toResponse(ProfitRecord record) {
        ProfitRecordResponse resp = new ProfitRecordResponse();
        BeanUtils.copyProperties(record, resp);
        return resp;
    }

    /** 判断用户是否为管理员 */
    private boolean isAdmin(Long userId) {
        // 从 SecurityContext 获取权限列表
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        
        Set<String> roles = auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .collect(Collectors.toSet());
        
        return roles.contains("ROLE_ADMIN");
    }
}
