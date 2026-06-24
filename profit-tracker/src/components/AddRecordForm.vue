<script setup>
import { ref, reactive, computed, watch } from 'vue'

const emit = defineEmits(['add'])

const showDialog = ref(false)

// 控制 body 滚动锁定
watch(showDialog, (val) => {
  document.body.style.overflow = val ? 'hidden' : ''
})

const POINT_CARD_PRICE = 1.3

const defaultForm = {
  date: new Date().toISOString().split('T')[0],
  mode: '副本',
  activity: '',
  income: 0,
  propIncome: 0,
  onlineHours: 0,
  charCount: 5,
  pointCardPrice: POINT_CARD_PRICE,
  remark: ''
}

const form = reactive({ ...defaultForm })

const modes = ['副本', '捉鬼', '刷宝图', '日常任务', '活动', '跑商', '其他']

function resetForm() {
  Object.assign(form, { ...defaultForm, date: new Date().toISOString().split('T')[0] })
}

function openDialog() {
  resetForm()
  showDialog.value = true
}

function closeDialog() {
  showDialog.value = false
}

function handleSubmit() {
  if (!form.date || (form.income <= 0 && form.propIncome <= 0)) return
  const cost = calcCost()
  const totalIncome = parseFloat(form.income) + parseFloat(form.propIncome)
  emit('add', {
    ...form,
    income: totalIncome,
    propIncome: 0,
    cost: Math.round(cost * 100) / 100,
  })
  closeDialog()
}

defineExpose({ openDialog, closeDialog })

const totalIncome = computed(() => {
  return (parseFloat(form.income) || 0) + (parseFloat(form.propIncome) || 0)
})

const totalCost = computed(() => {
  return calcCost()
})

function calcCost() {
  return parseFloat(form.onlineHours) * 6 * parseFloat(form.charCount) * (parseFloat(form.pointCardPrice) || POINT_CARD_PRICE)
}
</script>

<template>
  <button class="add-btn" @click="openDialog">+ 新增记录</button>

  <teleport to="body">
    <div v-if="showDialog" class="modal-overlay" @click.self="showDialog = false">
      <div class="modal">
        <div class="modal-header">
          <h3>新增收益记录</h3>
          <button class="close-btn" @click="showDialog = false">×</button>
        </div>

        <div class="modal-body">
          <div class="form-row">
            <label>日期 <span class="required">*</span></label>
            <input v-model="form.date" type="date" required />
          </div>

          <div class="form-row">
            <label>玩法模式</label>
            <select v-model="form.mode">
              <option v-for="m in modes" :key="m" :value="m">{{ m }}</option>
            </select>
          </div>

          <div class="form-row">
            <label>活动名称</label>
            <input v-model="form.activity" placeholder="如：副本名、任务名" />
          </div>

          <div class="section-title">收入明细（单位：万）</div>
          <div class="form-row">
            <label>梦幻币收入 (万)</label>
            <input v-model.number="form.income" type="number" step="0.01" min="0" placeholder="如：1.5" />
          </div>
          <div class="form-row">
            <label>物品收入 (万)</label>
            <input v-model.number="form.propIncome" type="number" step="0.01" min="0" placeholder="如：0.5" />
          </div>
          <div class="form-row total-row">
            <span>合计收入</span>
            <span class="total-value income">{{ totalIncome.toFixed(2) }} 万</span>
          </div>

          <div class="section-title">成本明细</div>
          <div class="form-row">
            <label>单个号在线小时</label>
            <input v-model.number="form.onlineHours" type="number" step="0.5" min="0" placeholder="如：4" />
          </div>
          <div class="form-row">
            <label>号的个数</label>
            <input v-model.number="form.charCount" type="number" step="1" min="1" max="10" />
          </div>
          <div class="form-row">
            <label>点卡价格 (万/小时)</label>
            <input v-model.number="form.pointCardPrice" type="number" step="0.01" min="0" placeholder="默认1.3" />
          </div>
          <div class="form-row total-row">
            <span>自动计算成本</span>
            <span class="total-value cost">
              {{ form.onlineHours }} × 6 × {{ form.charCount }} × {{ form.pointCardPrice || POINT_CARD_PRICE }} = {{ totalCost.toFixed(2) }}
            </span>
          </div>

          <div class="form-row total-row">
            <span>净利润</span>
            <span :class="['total-value', totalIncome - totalCost >= 0 ? 'positive' : 'negative']">
              {{ (totalIncome - totalCost).toFixed(2) }} 万
            </span>
          </div>

          <div class="form-row">
            <label>备注</label>
            <textarea v-model="form.remark" rows="2" placeholder="可选备注"></textarea>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn cancel" @click="showDialog = false">取消</button>
          <button class="btn submit" @click="handleSubmit">保存</button>
        </div>
      </div>
    </div>
  </teleport>
</template>

<style scoped>
.add-btn {
  background: #409eff;
  color: #fff;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}
.add-btn:hover { background: #66b1ff; }
.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.modal {
  background: #fff; border-radius: 12px;
  width: 520px; max-width: 90vw; max-height: 90vh;
  overflow-y: auto; box-shadow: 0 12px 36px rgba(0, 0, 0, 0.2);
}
.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20px 24px; border-bottom: 1px solid #ebeef5;
}
.modal-header h3 { margin: 0; font-size: 18px; }
.close-btn {
  background: none; border: none; font-size: 20px;
  cursor: pointer; color: #909399;
}
.modal-body {
  padding: 24px; display: flex; flex-direction: column; gap: 16px;
}
.form-row { display: flex; flex-direction: column; gap: 6px; }
.form-row label { font-size: 13px; color: #606266; }
.required { color: #f56c6c; }
.form-row input, .form-row select, .form-row textarea {
  padding: 8px 12px; border: 1px solid #dcdfe6;
  border-radius: 6px; font-size: 14px; outline: none;
  transition: border-color 0.2s;
}
.form-row input:focus, .form-row select:focus, .form-row textarea:focus {
  border-color: #409eff;
}
.section-title {
  font-size: 14px; font-weight: 600; color: #303133;
  padding-top: 8px; border-top: 1px dashed #ebeef5; margin-top: 4px;
}
.total-row {
  flex-direction: row; justify-content: space-between; align-items: center;
  padding: 12px 16px; background: #f5f7fa; border-radius: 8px;
}
.total-value { font-size: 18px; font-weight: 700; }
.total-value.income { color: #67c23a; }
.total-value.cost { color: #e6a23c; }
.total-value.positive { color: #67c23a; }
.total-value.negative { color: #f56c6c; }
.modal-footer {
  display: flex; justify-content: flex-end; gap: 12px;
  padding: 16px 24px; border-top: 1px solid #ebeef5;
}
.btn {
  padding: 8px 20px; border-radius: 6px;
  border: 1px solid #dcdfe6; cursor: pointer; font-size: 14px;
  transition: all 0.2s;
}
.btn.cancel { background: #fff; color: #606266; }
.btn.submit { background: #409eff; color: #fff; border-color: #409eff; }
.btn.submit:hover { background: #66b1ff; }
</style>
