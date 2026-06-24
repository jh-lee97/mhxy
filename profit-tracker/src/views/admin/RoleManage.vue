<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoles, createRole, updateRole, deleteRole, getRolePermissions, assignRolePermissions } from '../../api/admin.js'
import { getPermissions } from '../../api/admin.js'
import { useAuthStore } from '../../stores/authStore.js'

const loading = ref(false)
const roleList = ref([])
const permissionList = ref([])

// 新增/编辑弹窗
const dialogVisible = ref(false)
const editingRole = ref(null)
const formData = ref({
  roleName: '',
  roleCode: '',
  roleLevel: 1,
  description: '',
  status: 1
})

// 分配权限弹窗
const permissionDialogVisible = ref(false)
const currentRoleId = ref(null)
const currentRoleName = ref('')
const checkedPermissionIds = ref([])
const permissionTree = ref([])

function loadData() {
  loading.value = true
  getRoles().then(res => {
    loading.value = false
    if (res.data.code === 200) {
      roleList.value = res.data.data || []
    }
  }).catch(() => {
    loading.value = false
    ElMessage.error('加载角色列表失败')
  })
}

function openCreateDialog() {
  editingRole.value = null
  formData.value = { roleName: '', roleCode: '', roleLevel: 1, description: '', status: 1 }
  dialogVisible.value = true
}

function openEditDialog(row) {
  editingRole.value = row
  formData.value = { ...row }
  dialogVisible.value = true
}

function handleSubmit() {
  if (!formData.value.roleName || !formData.value.roleCode) {
    ElMessage.warning('请填写必填项')
    return
  }
  const promise = editingRole.value
    ? updateRole(editingRole.value.id, formData.value)
    : createRole(formData.value)
  
  promise.then(res => {
    if (res.data.code === 200) {
      ElMessage.success(editingRole.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadData()
    }
  }).catch(() => ElMessage.error('操作失败'))
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除角色 "${row.roleName}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteRole(row.id).then(res => {
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        loadData()
      }
    }).catch(() => ElMessage.error('删除失败'))
  }).catch(() => {})
}

function openPermissionDialog(row) {
  currentRoleId.value = row.id
  currentRoleName.value = row.roleName
  getRolePermissions(row.id).then(res => {
    if (res.data.code === 200) {
      checkedPermissionIds.value = (res.data.data || []).map(p => p.id)
    }
  })
  getPermissions().then(res => {
    if (res.data.code === 200) {
      permissionTree.value = buildTree(res.data.data || [])
    }
  })
  permissionDialogVisible.value = true
}

function buildTree(list, parentId = 0) {
  return list.filter(item => item.parentId === parentId).map(item => ({
    ...item,
    children: buildTree(list, item.id)
  }))
}

function handlePermissionCheckChange(checkedKeys) {
  checkedPermissionIds.value = checkedKeys
}

function handleAssignPermissions() {
  assignRolePermissions(currentRoleId.value, checkedPermissionIds.value).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('分配权限成功')
      permissionDialogVisible.value = false
    }
  }).catch(() => ElMessage.error('分配权限失败'))
}

onMounted(loadData)
</script>

<template>
  <div class="role-manage">
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="openCreateDialog">新增角色</el-button>
      </div>

      <el-table :data="roleList" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="roleLevel" label="等级" width="100" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openPermissionDialog(row)">分配权限</el-button>
            <el-button size="small" type="warning" @click="openEditDialog(row)">编辑</el-button>
            <el-button
              v-if="row.roleCode !== 'ADMIN'"
              size="small"
              type="danger"
              @click="handleDelete(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editingRole ? '编辑角色' : '新增角色'" width="500px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="角色名称" required>
          <el-input v-model="formData.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" required>
          <el-input v-model="formData.roleCode" placeholder="如: ADMIN, MANAGER" />
        </el-form-item>
        <el-form-item label="角色等级">
          <el-input-number v-model="formData.roleLevel" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限对话框 -->
    <el-dialog v-model="permissionDialogVisible" :title="`分配权限 - ${currentRoleName}`" width="600px">
      <el-tree
        ref="treeRef"
        :data="permissionTree"
        show-checkbox
        node-key="id"
        :props="{ label: 'name', children: 'children' }"
        @check="handlePermissionCheckChange"
      />
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignPermissions">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}
</style>
