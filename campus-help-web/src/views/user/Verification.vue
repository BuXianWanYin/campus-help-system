<template>
  <div class="verification-container">
    <div class="verification-card">
      <h1 class="page-title">实名认证</h1>
      
      <!-- 已认证状态 -->
      <div v-if="userInfo.isVerified === 1" class="verified-status">
        <el-result icon="success" title="认证成功" sub-title="您已完成实名认证">
          <template #extra>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="真实姓名">{{ userInfo.realName }}</el-descriptions-item>
              <el-descriptions-item label="学号">{{ userInfo.studentId }}</el-descriptions-item>
              <el-descriptions-item label="身份证号">
                {{ maskIdCard(userInfo.idCard) }}
              </el-descriptions-item>
              <el-descriptions-item label="认证状态">
                <el-tag type="success">已认证</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="接单权限">
                <el-tag v-if="userInfo.canAcceptTask === 1" type="success">可接单</el-tag>
                <el-tag v-else type="info">不可接单</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </template>
        </el-result>
      </div>
      
      <!-- 未认证状态 -->
      <div v-else>
        <el-alert
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;"
        >
          <template #title>
            <div>
              <p>实名认证说明：</p>
              <ul style="margin: 8px 0 0 20px; padding: 0;">
                <li>提交认证信息后，管理员将在1-3个工作日内完成审核</li>
                <li>认证通过后，您将自动获得接单权限，可以接取跑腿任务</li>
                <li>认证通过后，您的信用积分将增加10分</li>
                <li>请确保填写的信息真实有效，虚假信息将导致认证失败</li>
              </ul>
            </div>
          </template>
        </el-alert>
        
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="120px"
          style="max-width: 600px;"
        >
          <el-form-item label="真实姓名" prop="realName">
            <el-input
              v-model="form.realName"
              placeholder="请输入真实姓名"
              maxlength="50"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="身份证号" prop="idCard">
            <el-input
              v-model="form.idCard"
              placeholder="请输入18位身份证号"
              maxlength="18"
              show-word-limit
            />
            <div class="form-tip">请输入18位身份证号码</div>
          </el-form-item>
          
          <el-form-item label="学号" prop="studentId">
            <el-input
              v-model="form.studentId"
              placeholder="请输入学号"
              maxlength="50"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleSubmit">
              提交认证
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const userInfo = ref({})

const form = reactive({
  realName: '',
  idCard: '',
  studentId: ''
})

// 身份证号验证规则
const validateIdCard = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入身份证号'))
  } else if (!/^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[0-9Xx]$/.test(value)) {
    callback(new Error('身份证号格式不正确'))
  } else {
    callback()
  }
}

const rules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { max: 50, message: '真实姓名长度不能超过50个字符', trigger: 'blur' }
  ],
  idCard: [
    { required: true, validator: validateIdCard, trigger: 'blur' }
  ],
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { max: 50, message: '学号长度不能超过50个字符', trigger: 'blur' }
  ]
}

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const response = await userApi.getCurrentUser()
    if (response.code === 200) {
      userInfo.value = response.data
      // 如果已填写认证信息但未认证，填充表单
      if (userInfo.value.realName && userInfo.value.isVerified !== 1) {
        form.realName = userInfo.value.realName || ''
        form.idCard = userInfo.value.idCard || ''
        form.studentId = userInfo.value.studentId || ''
      }
    }
  } catch (error) {
    ElMessage.error(error.message || '获取用户信息失败')
  }
}

// 提交认证
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const response = await userApi.submitVerification(form)
        if (response.code === 200) {
          ElMessage.success('认证信息提交成功，请等待管理员审核')
          // 更新用户信息
          userStore.setUserInfo(response.data)
          userInfo.value = response.data
          // 重置表单
          handleReset()
        }
      } catch (error) {
        ElMessage.error(error.message || '提交失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 重置表单
const handleReset = () => {
  form.realName = ''
  form.idCard = ''
  form.studentId = ''
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 身份证号脱敏显示
const maskIdCard = (idCard) => {
  if (!idCard) return ''
  if (idCard.length !== 18) return idCard
  return idCard.substring(0, 6) + '********' + idCard.substring(14)
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.verification-container {
  max-width: 800px;
  margin: 0 auto;
  padding: var(--content-padding);
}

.verification-card {
  background-color: var(--color-bg-white);
  border-radius: var(--radius-md);
  padding: var(--spacing-3xl);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-3xl) 0;
}

.verified-status {
  padding: 20px 0;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
  line-height: 1.5;
}

/* 优化表单样式 */
:deep(.el-form) {
  padding: 20px 0;
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-input__wrapper) {
  border-radius: var(--radius-sm);
  box-shadow: 0 0 0 1px var(--color-border) inset;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

:deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

:deep(.el-alert) {
  border-radius: var(--radius-md);
  margin-bottom: var(--spacing-2xl);
}

:deep(.el-result__title) {
  color: var(--color-text-primary);
  font-size: 20px;
}

:deep(.el-result__subtitle) {
  color: var(--color-text-regular);
  font-size: 14px;
}

:deep(.el-descriptions) {
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .verification-container {
    padding: 16px;
  }
  
  .verification-card {
    padding: 24px;
  }
  
  .page-title {
    font-size: 24px;
    margin-bottom: 24px;
  }
}
</style>

