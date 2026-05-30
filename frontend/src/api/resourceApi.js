import axiosClient from './axiosClient'

// only the filters with a real value are sent so the backend treats the rest as "no filter"
export const listResources = (filters = {}) => {
  const params = {}
  if (filters.category) params.category = filters.category
  if (filters.subject) params.subject = filters.subject
  return axiosClient.get('/resources', { params }).then((res) => res.data)
}

export const createResource = (payload) =>
  axiosClient.post('/resources', payload).then((res) => res.data)

export const updateResource = (id, payload) =>
  axiosClient.put(`/resources/${id}`, payload).then((res) => res.data)

export const deleteResource = (id) =>
  axiosClient.delete(`/resources/${id}`).then((res) => res.data)
