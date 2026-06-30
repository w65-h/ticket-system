<template>
  <div class="ticket-create">
    <el-card shadow="never">
      <template #header><span>创建工单</span></template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 700px">
        <el-form-item label="工单标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入工单标题" />
        </el-form-item>
        <el-form-item label="工单内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请详细描述工单内容" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-radio-group v-model="form.priority">
            <el-radio :value="1">低</el-radio>
            <el-radio :value="2">中</el-radio>
            <el-radio :value="3">高</el-radio>
            <el-radio :value="4">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="指派处理人">
          <el-select v-model="form.assigneeId" placeholder="请选择处理人" clearable filterable style="width: 100%">
            <el-option v-for="u in users" :key="u.id" :label="u.realName" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属部门">
          <el-tree-select
            v-model="form.deptId"
            :data="deptTree"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            placeholder="请选择部门"
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker v-model="form.deadline" type="datetime" placeholder="请选择" style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">提交工单</el-button>
          <el-button @click="$router.push('/ticket')">返回列表</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createTicket } from '../../api/ticket'
import { getDeptTree } from '../../api/dept'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const deptTree = ref([])
const users = ref([])

const form = reactive({
  title: '', content: '', priority: 2, assigneeId: null, deptId: null, deadline: ''
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }]
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await createTicket(form)
    ElMessage.success('工单创建成功')
    router.push('/ticket')
  } catch (e) {
    ElMessage.error(e.message || '创建失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  const res = await getDeptTree()
  deptTree.value = res.data || []
})
</script>