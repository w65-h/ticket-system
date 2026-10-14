<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ data.totalTickets }}</div>
          <div class="stat-label">工单总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card pending">
          <div class="stat-value">{{ data.pendingTickets }}</div>
          <div class="stat-label">待处理</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card processing">
          <div class="stat-value">{{ data.processingTickets }}</div>
          <div class="stat-label">处理中</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card completed">
          <div class="stat-value">{{ data.completedTickets }}</div>
          <div class="stat-label">已完成</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span>工单状态分布</span></template>
          <div ref="statusChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span>部门工单统计</span></template>
          <div ref="deptChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header><span>近30天工单趋势</span></template>
          <div ref="trendChartRef" class="chart trend-chart"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts/core'
import { PieChart, BarChart, LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { getDashboard } from '../../api/dashboard'

echarts.use([PieChart, BarChart, LineChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent, CanvasRenderer])

const data = ref({
  totalTickets: 0, pendingTickets: 0, processingTickets: 0,
  completedTickets: 0, rejectedTickets: 0, avgProcessHours: 0,
  statusStats: [], deptStats: [], dayTrend: []
})

const statusChartRef = ref(null)
const deptChartRef = ref(null)
const trendChartRef = ref(null)
let statusChart = null, deptChart = null, trendChart = null

const fetchData = async () => {
  const res = await getDashboard()
  data.value = res.data
  await nextTick()
  renderCharts()
}

const renderCharts = () => {
  renderStatusChart()
  renderDeptChart()
  renderTrendChart()
}

const renderStatusChart = () => {
  if (!statusChartRef.value) return
  statusChart = echarts.init(statusChartRef.value)
  statusChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie', radius: ['45%', '70%'], center: ['50%', '45%'],
      label: { show: true, formatter: '{b}\n{d}%' },
      data: (data.value.statusStats || []).map(s => ({
        name: s.status, value: s.count
      })),
      itemStyle: {
        borderRadius: 4, borderColor: '#fff', borderWidth: 2
      }
    }]
  })
}

const renderDeptChart = () => {
  if (!deptChartRef.value) return
  deptChart = echarts.init(deptChartRef.value)
  const stats = data.value.deptStats || []
  deptChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['工单总数', '已完成', '平均耗时(h)'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: stats.map(d => d.deptName), axisLabel: { rotate: 30 } },
    yAxis: [
      { type: 'value', name: '数量' },
      { type: 'value', name: '小时' }
    ],
    series: [
      { name: '工单总数', type: 'bar', data: stats.map(d => d.totalCount), itemStyle: { color: '#409EFF' } },
      { name: '已完成', type: 'bar', data: stats.map(d => d.completedCount), itemStyle: { color: '#67C23A' } },
      { name: '平均耗时(h)', type: 'line', yAxisIndex: 1, data: stats.map(d => d.avgHours), itemStyle: { color: '#E6A23C' } }
    ]
  })
}

const renderTrendChart = () => {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  const trend = data.value.dayTrend || []
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['新建工单', '完成工单'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: trend.map(d => d.date), axisLabel: { rotate: 45, fontSize: 10 } },
    yAxis: { type: 'value' },
    series: [
      { name: '新建工单', type: 'line', smooth: true, data: trend.map(d => d.createCount),
        itemStyle: { color: '#409EFF' }, areaStyle: { color: 'rgba(64,158,255,0.1)' } },
      { name: '完成工单', type: 'line', smooth: true, data: trend.map(d => d.completeCount),
        itemStyle: { color: '#67C23A' }, areaStyle: { color: 'rgba(103,194,58,0.1)' } }
    ]
  })
}

const handleResize = () => {
  statusChart?.resize()
  deptChart?.resize()
  trendChart?.resize()
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  statusChart?.dispose()
  deptChart?.dispose()
  trendChart?.dispose()
})
</script>

<style scoped>
.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  cursor: pointer;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.pending .stat-value { color: #E6A23C; }
.processing .stat-value { color: #409EFF; }
.completed .stat-value { color: #67C23A; }

.chart-row {
  margin-bottom: 20px;
}

.chart {
  height: 320px;
}

.trend-chart {
  height: 300px;
}
</style>