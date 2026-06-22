package com.profit.track.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_permission")
public class SysPermission implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父级权限ID，0表示顶级 */
    private Long parentId;

    /** 权限名称 */
    private String name;

    /** 权限标识(如: record:add) */
    private String code;

    /** 类型：1-菜单 2-按钮 3-接口 */
    private Integer type;

    /** 路由/接口路径 */
    private String path;

    /** HTTP方法(GET/POST等) */
    private String method;

    /** 排序 */
    private Integer sortOrder;

    /** 图标 */
    private String icon;

    /** 状态：0-禁用 1-启用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
