import request from '@/utils/request'

export function createHealthRecord(data) {
  return request({
    url: '/health/records',
    method: 'post',
    data
  })
}

export function updateHealthRecord(id, data) {
  return request({
    url: `/health/records/${id}`,
    method: 'put',
    data
  })
}

export function deleteHealthRecord(id) {
  return request({
    url: `/health/records/${id}`,
    method: 'delete'
  })
}

export function getHealthRecord(id) {
  return request({
    url: `/health/records/${id}`,
    method: 'get'
  })
}

export function getHealthRecordList(params) {
  return request({
    url: '/health/records',
    method: 'get',
    params
  })
}

export function getHealthTrend(petId) {
  return request({
    url: `/health/trend/${petId}`,
    method: 'get'
  })
}

export function getHealthStatistics() {
  return request({
    url: '/health/statistics',
    method: 'get'
  })
}

export function getAllHealthRecords(params) {
  return request({
    url: '/health/records',
    method: 'get',
    params
  })
}

export function getMyHealthRecords(params) {
  return request({
    url: '/health/my',
    method: 'get',
    params
  })
}
