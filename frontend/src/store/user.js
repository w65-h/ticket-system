import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  const permissions = ref(JSON.parse(localStorage.getItem('permissions') || '[]'))
  const roles = ref(JSON.parse(localStorage.getItem('roles') || '[]'))

  function setToken(val) {
    token.value = val
    localStorage.setItem('token', val)
  }

  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  function setPermissions(perms) {
    permissions.value = perms
    localStorage.setItem('permissions', JSON.stringify(perms))
  }

  function setRoles(r) {
    roles.value = r
    localStorage.setItem('roles', JSON.stringify(r))
  }

  function hasPermission(perm) {
    return permissions.value.includes(perm) || roles.value.includes('admin')
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    permissions.value = []
    roles.value = []
    localStorage.clear()
  }

  return { token, userInfo, permissions, roles, setToken, setUserInfo, setPermissions, setRoles, hasPermission, logout }
})