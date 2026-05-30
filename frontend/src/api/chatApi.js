import axiosClient from './axiosClient'

export const sendMessage = (payload) =>
  axiosClient.post('/chat', payload).then((res) => res.data)

export const getSessions = () =>
  axiosClient.get('/chat/sessions').then((res) => res.data)

export const getMessages = (sessionId) =>
  axiosClient.get(`/chat/sessions/${sessionId}`).then((res) => res.data)
