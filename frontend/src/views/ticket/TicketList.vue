<template>
  <div class="ticket-list">
    <el-card shadow="never">
      <div class="search-bar">
        <el-form :inline="true" :model="query" size="default">
          <el-form-item label="工单编号">
            <el-input v-model="query.ticketNo" placeholder="请输入" clearable style="width: 160px" />
          </el-form-item>
          <el-form-item label="标题">
            <el-input v-model="query.title" placeholder="请输入" clearable style="width: 160px" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
              <el-option label="待处理" :value="1" />
              <el-option label="处理中" :value="2" />
              <el-option label="待验收" :value="3" />
              <el-option label="已完成" :value="4" />
              <el-option label="已驳回" :value="5" />
            </el-select>
          </el-form-item>
          <el-form-item label="优先级">
            <el-select v-model="query.priority" placeholder="全部" clearable style="width: 120px">
              <el-option label="低" :value="1" />
              <el-option label="中" :value="2" />
              <el-option label="高" :value="3" />
              <el-option label="紧急" :value="4" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <el-card shadow="never" style="margin-top: 16px">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="ticketNo" label="工单编号" width="180" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="90">
          <template #default="{ row }">
            <el-tag :type="priorityTagType(row.priority)" size="small">{{ priorityText(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="创建人" width="100" />
        <el-table-column prop="assigneeName" label="处理人" width="100" />
        <el-table-column prop="deptName" label="所属部门" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleDetail(row)">详情</el-button>
            <template v-if="row.status === 1 && userStore.hasPermission('ticket:process')">
              <el-button type="success" link size="small" @click="handleStatus(row, 2)">接单</el-button>
            </template>
            <template v-if="row.status === 2 && userStore.hasPermission('ticket:process')">
              <el-button type="warning" link size="small" @click="handleStatus(row, 3)">提交验收</el-button>
            </template>
            <template v-if="row.status === 3 && userStore.hasPermission('ticket:process')">
              <el-button type="success" link size="small" @click="handleStatus(row, 4)">验收通过</el-button>
              <el-button type="danger" link size="small" @click="handleStatus(row, 5)">驳回</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchData"
          @current-change="fetchData"
          background
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="工单详情" width="600px">
      <template v-if="currentTicket">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="工单编号">{{ currentTicket.ticketNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(currentTicket.status)">{{ statusText(currentTicket.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="priorityTagType(currentTicket.priority)">{{ priorityText(currentTicket.priority) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建人">{{ currentTicket.creatorName }}</el-descriptions-item>
          <el-descriptions-item label="处理人">{{ currentTicket.assigneeName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="所属部门">{{ currentTicket.deptName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentTicket.createTime }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ currentTicket.finishTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="标题" :span="2">{{ currentTicket.title }}</el-descriptions-item>
          <el-descriptions-item label="内容" :span="2">
            <div style="white-space: pre-wrap">{{ currentTicket.content }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTicketList, getTicketDetail, updateTicketStatus } from '../../api/ticket'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailVisible = ref(false)
const currentTicket = ref(null)

const query = reactive({
  ticketNo: '', title: '', status: null, priority: null, page: 1, size: 10
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getTicketList(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.page = 1
  fetchData()
}

const handleReset = () => {
  query.ticketNo = ''
  query.title = ''
  query.status = null
  query.priority = null
  query.page = 1
  fetchData()
}

const handleDetail = async (row) => {
  const res = await getTicketDetail(row.id)
  currentTicket.value = res.data
  detailVisible.value = true
}

const handleStatus = (row, targetStatus) => {
  const statusMap = { 2: '接单', 3: '提交验收', 4: '验收通过', 5: '驳回' }
  ElMessageBox.confirm(`确定对该工单执行【${statusMap[targetStatus]}】操作？`, '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    await updateTicketStatus({ ticketId: row.id, targetStatus })
    ElMessage.success('操作成功')
    fetchData()
  }).catch(() => {})
}

const statusText = (s) => ({ 1: '待处理', 2: '处理中', 3: '待验收', 4: '已完成', 5: '已驳回' }[s] || '未知')
const statusTagType = (s) => ({ 1: 'warning', 2: 'primary', 3: '', 4: 'success', 5: 'danger' }[s] || 'info')
const priorityText = (p) => ({ 1: '低', 2: '中', 3: '高', 4: '紧急' }[p] || '未知')
const priorityTagType = (p) => ({ 1: 'info', 2: 'warning', 3: 'danger', 4: 'danger' }[p] || 'info')

onMounted(() => fetchData())
</script>

<style scoped>
.search-bar {
  padding: 4px 0;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>