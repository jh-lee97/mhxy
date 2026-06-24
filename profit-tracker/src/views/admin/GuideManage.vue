<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getGuides, createGuide, updateGuide, deleteGuide } from '../../api/guide.js'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'

const loading = ref(false)
const guideList = ref([])
const dialogVisible = ref(false)
const editingGuide = ref(null)
const formData = ref({
  category: '日常任务',
  title: '',
  summary: '',
  content: '',
  sortOrder: 0,
  status: 1
})

const categories = ['日常任务', '副本', '活动', '装备打造']

// 富文本编辑器内容
const content = ref('')

// 弹窗打开时同步内容
watch(dialogVisible, (val) => {
  if (val) {
    content.value = formData.value.content || ''
  }
})

async function loadData() {
  loading.value = true
  try {
    const res = await getGuides(null)
    if (res.data.code === 200) {
      guideList.value = res.data.data || []
    }
  } catch (e) {
    console.error('加载攻略失败:', e)
    ElMessage.error('加载攻略失败')
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  editingGuide.value = null
  formData.value = { category: '日常任务', title: '', summary: '', content: '', sortOrder: 0, status: 1 }
  dialogVisible.value = true
}

function openEditDialog(row) {
  editingGuide.value = row
  formData.value = { ...row }
  dialogVisible.value = true
}

function handleSubmit() {
  if (!formData.value.title) {
    ElMessage.warning('请填写标题')
    return
  }
  // Quill 的内容通过 v-model 绑定在 content 上
  formData.value.content = content.value

  const promise = editingGuide.value
    ? updateGuide(editingGuide.value.id, formData.value)
    : createGuide(formData.value)

  promise.then(res => {
    if (res.data.code === 200) {
      ElMessage.success(editingGuide.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadData()
    }
  }).catch(() => ElMessage.error('操作失败'))
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除攻略 "${row.title}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteGuide(row.id).then(res => {
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        loadData()
      }
    }).catch(() => ElMessage.error('删除失败'))
  }).catch(() => {})
}

onMounted(loadData)
</script>

<template>
  <div class="guide-manage">
    <el-card>
      <div class="toolbar">
        <el-button type="primary" @click="openCreateDialog">新增攻略</el-button>
      </div>

      <el-table :data="guideList" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="category" label="分类" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="summary" label="摘要" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="warning" @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingGuide ? '编辑攻略' : '新增攻略'"
      width="1000px"
    >
      <el-form :model="formData" label-width="80px">
        <el-form-item label="分类" required>
          <el-select v-model="formData.category" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" required>
          <el-input v-model="formData.title" placeholder="请输入攻略标题" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="formData.summary" type="textarea" :rows="2" placeholder="请输入摘要" />
        </el-form-item>
        <el-form-item label="正文">
          <div class="editor-wrapper">
            <QuillEditor
              v-model="content"
              :style="{ height: '400px' }"
              placeholder="请输入攻略正文内容..."
              toolbar="full"
            />
          </div>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="999" />
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
  margin-bottom: 16px;
}

.editor-wrapper {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  width: 100%;
}
</style>
