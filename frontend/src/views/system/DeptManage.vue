<template>
  <div class="dept-manage">
    <el-card shadow="never">
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd(0)">新增部门</el-button>
      </div>

      <el-table :data="deptList" v-loading="loading" row-key="id" stripe
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" style="width: 100%; margin-top: 16px">
        <el-table-column prop="deptName" label="部门名称" min-width="200" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="leader" label="负责人" width="120" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleAdd(row.id)">添加子部门</el-button>
            <el-button type="warning" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除该部门？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="上级部门" v-if="form.parentId !== undefined">
          <el-input :value="parentName" disabled />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="form.leader" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getDeptTree, saveDept, deleteDept } from '../../api/dept'

const loading = ref(false)
const saving = ref(false)
const deptList = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const parentName = ref('顶级部门')

const form = reactive({
  id: null, deptName: '', parentId: 0, sortOrder: 0, leader: '', phone: '', status: 1
})

const dialogTitle = computed(() => isEdit.value ? '编辑部门' : '新增部门')

const rules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getDeptTree()
    deptList.value = res.data || []
  } finally {
    loading.value = false
  }
}

const findDeptName = (list, id) => {
  for (const d of list) {
    if (d.id === id) return d.deptName
    if (d.children) {
      const found = findDeptName(d.children, id)
      if (found) return found
    }
  }
  return '顶级部门'
}

const handleAdd = (parentId) => {
  isEdit.value = false
  form.id = null
  form.deptName = ''
  form.parentId = parentId
  form.sortOrder = 0
  form.leader = ''
  form.phone = ''
  form.status = 1
  parentName.value = parentId === 0 ? '顶级部门' : findDeptName(deptList.value, parentId)
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.id = row.id
  form.deptName = row.deptName
  form.parentId = row.parentId
  form.sortOrder = row.sortOrder
  form.leader = row.leader
  form.phone = row.phone
  form.status = row.status
  parentName.value = findDeptName(deptList.value, row.parentId)
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await saveDept({ ...form })
    ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await deleteDept(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

onMounted(() => fetchData())
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
}
</style>