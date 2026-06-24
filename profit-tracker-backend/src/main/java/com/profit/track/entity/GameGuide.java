package com.profit.track.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("game_guide")
public class GameGuide implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类 */
    private String category;

    /** 攻略标题 */
    private String title;

    /** 摘要 */
    private String summary;

    /** 正文内容 */
    private String content;

    /** 排序权重 */
    private Integer sortOrder;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
