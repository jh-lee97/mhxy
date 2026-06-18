package com.profit.track.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.profit.track.dto.RoleRequest;
import com.profit.track.dto.RoleResponse;
import com.profit.track.entity.SysRole;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {

    /** 获取所有角色列表 */
    List<RoleResponse> listRoles();

    /** 创建角色 */
    RoleResponse createRole(RoleRequest request);

    /** 更新角色 */
    RoleResponse updateRole(RoleRequest request);

    /** 删除角色 */
    void deleteRole(Long id);
}
