import request from '../utils/request'

export function getDeptTree() {
  return request({
    url: '/dept/tree',
    method: 'get'
  })
}

export function getDeptList() {
  return request({
    url: '/dept',
    method: 'get'
  })
}

export function saveDept(data) {
  return request({
    url: '/dept',
    method: 'post',
    data
  })
}

export function deleteDept(id) {
  return request({
    url: `/dept/${id}`,
    method: 'delete'
  })
}