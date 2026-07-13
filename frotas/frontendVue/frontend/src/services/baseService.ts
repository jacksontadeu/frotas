import api from './api'
import type { BaseDTORequest, BaseResponse } from '../types'

export const baseService = {
  async cadastrar(data: BaseDTORequest): Promise<void> {
    await api.post('/base', data)
  },

  async listarTodas(): Promise<BaseResponse[]> {
    const response = await api.get<BaseResponse[]>('/base')
    return response.data
  },

  async excluir(id: number): Promise<void> {
    await api.delete(`/base/${id}`)
  },
}
