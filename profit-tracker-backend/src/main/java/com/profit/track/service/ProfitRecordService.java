package com.profit.track.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.profit.track.dto.ChartResponse;
import com.profit.track.dto.ProfitRecordRequest;
import com.profit.track.dto.ProfitRecordResponse;
import com.profit.track.dto.StatResponse;
import com.profit.track.entity.ProfitRecord;
import java.util.List;

public interface ProfitRecordService extends IService<ProfitRecord> {

    /** 分页查询记录 */
    List<ProfitRecordResponse> listRecords(Long userId);

    /** 新增记录 */
    ProfitRecordResponse addRecord(ProfitRecordRequest request);

    /** 更新记录 */
    ProfitRecordResponse updateRecord(ProfitRecordRequest request);

    /** 删除记录 */
    void deleteRecord(Long id, Long userId);

    /** 获取统计信息 */
    StatResponse getStats(Long userId);

    /** 获取图表数据（近7日） */
    List<ChartResponse> getChartRecords(Long userId);
}