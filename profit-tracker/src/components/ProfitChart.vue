<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import ECharts from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import {
  TitleComponent, TooltipComponent, GridComponent, LegendComponent,
} from 'echarts/components'
import { getChartRecords } from '../api/record.js'

use([CanvasRenderer, LineChart, TitleComponent, TooltipComponent, GridComponent, LegendComponent])

const chartData = ref([])

const option = computed(() => {
  const dates = chartData.value.map(d => d.date)
  const incomes = chartData.value.map(d => d.income / 10000)
  const costs = chartData.value.map(d => d.cost / 10000)
  const profits = chartData.value.map(d => d.profit / 10000)

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        const item = params[0]
        let html = `<b>${item.name}</b><br/>`
        for (const p of params) {
          html += `${p.marker} ${p.seriesName}: ${(p.value * 10000).toFixed(2)}<br/>`
        }
        return html
      },
    },
    legend: { data: ['收入', '成本', '净利润'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '10%', top: '10%', containLabel: true },
    xAxis: { type: 'category', data: dates, axisLabel: { rotate: 30 } },
    yAxis: {
      type: 'value',
      name: '金额(万)',
      axisLabel: {
        formatter: (v) => (v * 10000).toFixed(0),
      },
    },
    series: [
      { name: '收入', type: 'line', data: incomes, smooth: true, itemStyle: { color: '#67c23a' }, areaStyle: { color: 'rgba(103, 194, 58, 0.1)' } },
      { name: '成本', type: 'line', data: costs, smooth: true, itemStyle: { color: '#e6a23c' }, areaStyle: { color: 'rgba(230, 162, 60, 0.1)' } },
      { name: '净利润', type: 'bar', data: profits, itemStyle: { color: (params) => params.value >= 0 ? '#409eff' : '#f56c6c' } },
    ],
  }
})

async function updateChart() {
  const res = await getChartRecords()
  chartData.value = res?.data?.code === 200 ? res.data.data : []
}

onMounted(updateChart)
window.addEventListener('resize', updateChart)
onUnmounted(() => window.removeEventListener('resize', updateChart))
</script>

<template>
  <div class="chart">
    <h3>近7日收益趋势</h3>
    <div class="chart-wrapper">
      <ECharts :option="option" autoresize />
    </div>
  </div>
</template>

<style scoped>
.chart { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08); }
.chart h3 { margin: 0 0 16px; font-size: 16px; color: #303133; }
.chart-wrapper { height: 320px; }
</style>
