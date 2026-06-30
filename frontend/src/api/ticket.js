import request from '../utils/request'

export function getTicketList(params) {
  return request({
    url: '/ticket/list',
    method: 'get',
    params
  })
}

export function getTicketDetail(id) {
  return request({
    url: `/ticket/${id}`,
    method: 'get'
  })
}

export function createTicket(data) {
  return request({
    url: '/ticket',
    method: 'post',
    data
  })
}

export function updateTicketStatus(data) {
  return request({
    url: '/ticket/status',
    method: 'put',
    data
  })
}