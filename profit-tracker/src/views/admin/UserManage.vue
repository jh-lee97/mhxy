<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUsers, updateUserStatus, assignUserRole, resetUserPassword, deleteUser } from '../../api/admin.js'
import { getRoles } from '../../api/admin.js'
import { useAuthStore } from '../../stores/authStore.js'

const store = useAuthStore()
const loading = ref(false)
const userList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const roleList = ref([])
const searchLoading = ref(false)

// 弹窗状态
const statusDialog = ref(false)
const passwordDialog = ref(false)
const roleDialog = ref(false)
const currentUserId = ref(null)
const currentStatus = ref(1)
const newPassword = ref('')
const newRoleId = ref(null)

async function loadData() {
  loading.value = true
  try {
    const res = await getUsers({
      page: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value
    })
    if (res.data.code === 200) {
      userList.value = res.data.data.records || []
      total.value = res.data.data.total || 0
    }
  } catch (e) {
    console.error('加载用户列表失败:', e)
  } finally {
    loading.value = false
  }
}

async function loadRoles() {
  try {
    const res = await getRoles()
    if (res.data.code === 200) {
      roleList.value = res.data.data || []
    }
  } catch (e) {
    console.error('加载角色列表失败:', e)
  }
}

function handleSearch() {
  currentPage.value = 1
  loadData()
}

function handlePageChange(page) {
  currentPage.value = page
  loadData()
}

function handleSizeChange(size) {
  pageSize.value = size
  currentPage.value = 1
  loadData()
}

function handleToggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  ElMessageBox.confirm(`确定要${action}用户 "${row.username}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await updateUserStatus(row.id, newStatus)
      row.status = newStatus
      ElMessage.success(`${action}成功`)
      loadData()
    } catch (e) {
      ElMessage.error(e.response?.data?.msg || '操作失败')
    }
  }).catch(() => {})
}

function openRoleDialog(row) {
  currentUserId.value = row.id
  newRoleId.value = row.roleId || null
  roleDialog.value = true
}

function handleAssignRole() {
  if (!newRoleId.value) {
    ElMessage.warning('请选择角色')
    return
  }
  assignUserRole(currentUserId.value, newRoleId.value).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('分配角色成功')
      roleDialog.value = false
      loadData()
    }
  }).catch(() => ElMessage.error('分配角色失败'))
}

function openPasswordDialog(row) {
  currentUserId.value = row.id
  newPassword.value = ''
  passwordDialog.value = true
}

function handleResetPassword() {
  if (!newPassword.value || newPassword.value.length < 6) {
    ElMessage.warning('密码长度至少6位')
    return
  }
  resetUserPassword(currentUserId.value, newPassword.value).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('重置密码成功')
      passwordDialog.value = false
    }
  }).catch(() => ElMessage.error('重置密码失败'))
}

function handleDeleteUser(row) {
  ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？此操作不可恢复！`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'error'
  }).then(async () => {
    try {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (e) {
      ElMessage.error(e.response?.data?.msg || '删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadData()
  loadRoles()
})
</script>

<template>
  <div class="user-manage">
    <el-card>
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索用户名/昵称/手机号"
          clearable
          style="width: 300px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>

      <!-- 用户列表 -->
      <el-table :data="userList" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="角色" width="150">
          <template #default="{ row }">
            <el-tag>{{ row.roleName || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openRoleDialog(row)">分配角色</el-button>
            <el-button size="small" type="warning" @click="openPasswordDialog(row)">重置密码</el-button>
            <el-button
              size="small"
              :type="row.status === 1 ? 'danger' : 'success'"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button
              v-if="row.roleCode !== 'ADMIN'"
              size="small"
              type="danger"
              @click="handleDeleteUser(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 分配角色对话框 -->
    <el-dialog v-model="roleDialog" title="分配角色" width="400px">
      <el-select v-model="newRoleId" placeholder="选择角色" style="width: 100%">
        <el-option
          v-for="role in roleList"
          :key="role.id"
          :label="role.roleName"
          :value="role.id"
        />
      </el-select>
      <template #footer>
        <el-button @click="roleDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAssignRole">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog v-model="passwordDialog" title="重置密码" width="400px">
      <el-input v-model="newPassword" placeholder="新密码（至少6位）" show-password />
      <template #footer>
        <el-button @click="passwordDialog = false">取消</el-button>
        <el-button type="primary" @click="handleResetPassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.search-bar {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
