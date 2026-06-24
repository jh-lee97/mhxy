<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPermissions, createPermission, updatePermission, deletePermission } from '../../api/admin.js'

const loading = ref(false)
const permissionList = ref([])
const dialogVisible = ref(false)
const editingPerm = ref(null)
const filterType = ref(null)

const formData = ref({
  parentId: 0,
  name: '',
  code: '',
  type: 1,
  path: '',
  method: '',
  sortOrder: 0,
  icon: '',
  status: 1
})

const typeOptions = [
  { label: '菜单', value: 1 },
  { label: '按钮', value: 2 },
  { label: '接口', value: 3 }
]

const typeColors = {
  1: '',
  2: 'warning',
  3: 'danger'
}

const filteredList = computed(() => {
  if (!filterType.value) return permissionList.value
  return permissionList.value.filter(p => p.type === filterType.value)
})

function loadData() {
  loading.value = true
  getPermissions().then(res => {
    loading.value = false
    if (res.data.code === 200) {
      permissionList.value = buildTree(res.data.data || [])
    }
  }).catch(() => {
    loading.value = false
    ElMessage.error('加载权限列表失败')
  })
}

function buildTree(list, parentId = 0) {
  return list
    .filter(item => item.parentId === parentId)
    .map(item => ({
      ...item,
      children: buildTree(list, item.id)
    }))
    .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
}

function openCreateDialog(parentId = 0) {
  editingPerm.value = null
  formData.value = {
    parentId,
    name: '',
    code: '',
    type: 2,
    path: '',
    method: '',
    sortOrder: 0,
    icon: '',
    status: 1
  }
  dialogVisible.value = true
}

function openEditDialog(row) {
  editingPerm.value = row
  formData.value = { ...row }
  dialogVisible.value = true
}

function handleSubmit() {
  if (!formData.value.name || !formData.value.code) {
    ElMessage.warning('请填写必填项')
    return
  }
  const promise = editingPerm.value
    ? updatePermission(editingPerm.value.id, formData.value)
    : createPermission(formData.value)

  promise.then(res => {
    if (res.data.code === 200) {
      ElMessage.success(editingPerm.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadData()
    }
  }).catch(() => ElMessage.error('操作失败'))
}

function handleDelete(row) {
  // 检查是否有子权限
  const hasChildren = permissionList.value.some(p => p.parentId === row.id)
  if (hasChildren) {
    ElMessage.warning('请先删除子权限')
    return
  }
  
  ElMessageBox.confirm(`确定要删除权限 "${row.name}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deletePermission(row.id).then(res => {
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        loadData()
      }
    }).catch(() => ElMessage.error('删除失败'))
  }).catch(() => {})
}

function getTypeLabel(type) {
  const opt = typeOptions.find(o => o.value === type)
  return opt ? opt.label : '-'
}

// 获取父级权限名称
function getParentName(parentId) {
  if (!parentId || parentId === 0) return '顶级'
  // 扁平列表中查找
  const flatList = flattenPermissions(permissionList.value)
  const parent = flatList.find(p => p.id === parentId)
  return parent ? parent.name : '未知'
}

function flattenPermissions(tree) {
  let result = []
  tree.forEach(node => {
    result.push(node)
    if (node.children && node.children.length > 0) {
      result = result.concat(flattenPermissions(node.children))
    }
  })
  return result
}

onMounted(loadData)
</script>

<template>
  <div class="permission-manage">
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="openCreateDialog(0)">新增顶级权限</el-button>
        <el-divider direction="vertical" />
        <el-radio-group v-model="filterType" size="small">
          <el-radio-button :label="null">全部</el-radio-button>
          <el-radio-button :label="1">菜单</el-radio-button>
          <el-radio-button :label="2">按钮</el-radio-button>
          <el-radio-button :label="3">接口</el-radio-button>
        </el-radio-group>
      </div>

      <el-table :data="filteredList" border stripe row-key="id" default-expand-all>
        <el-table-column prop="name" label="名称" min-width="150">
          <template #default="{ row }">
            <span v-if="row.parentId === 0 || !row.parentId" style="font-weight: bold;">
              {{ row.name }}
            </span>
            <span v-else style="padding-left: 20px; color: #606266;">
              └─ {{ row.name }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="code" label="权限标识" width="200" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="typeColors[row.type]" size="small">{{ getTypeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="父级" width="120">
          <template #default="{ row }">
            {{ getParentName(row.parentId) }}
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路径" width="200" />
        <el-table-column prop="method" label="方法" width="100" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="图标" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.icon" size="small">{{ row.icon }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="success" @click="openCreateDialog(row.id)">新增子权限</el-button>
            <el-button size="small" type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button
              v-if="!permissionList.some(p => p.parentId === row.id)"
              size="small"
              type="danger"
              @click="handleDelete(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editingPerm ? '编辑权限' : '新增权限'" width="550px">
      <el-form :model="formData" label-width="100px">
        <el-form-item label="上级权限">
          <el-select v-model="formData.parentId" style="width: 100%" clearable>
            <el-option label="顶级权限" :value="0" />
            <el-option
              v-for="item in flattenPermissions(permissionList)"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="权限名称" required>
          <el-input v-model="formData.name" placeholder="如: 收益记录" />
        </el-form-item>
        <el-form-item label="权限标识" required>
          <el-input v-model="formData.code" placeholder="如: record" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="formData.type" style="width: 100%">
            <el-option v-for="opt in typeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="路径">
          <el-input v-model="formData.path" placeholder="/records 或 /api/records" />
        </el-form-item>
        <el-form-item label="HTTP方法">
          <el-select v-model="formData.method" placeholder="选择方法" clearable style="width: 100%">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="formData.icon" placeholder="Element Plus 图标名，如: MoneyBag" />
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
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
</style>
