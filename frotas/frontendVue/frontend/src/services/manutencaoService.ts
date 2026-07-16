import api from './api'
import type { ManutencaoDTORequest, ManutencaoResponse, AtendimentoDTORequest } from '../types'

export const manutencaoService = {
  async cadastrar(data: ManutencaoDTORequest): Promise<void> {
    await api.post('/manutencao', data)
  },

  async listarTodas(): Promise<ManutencaoResponse[]> {
    const response = await api.get<ManutencaoResponse[]>('/manutencao')
    return response.data
  },

  async listarAbertas(): Promise<ManutencaoResponse[]> {
    const response = await api.get<ManutencaoResponse[]>('/manutencao', {
      params: { status: 'EM_ABERTO' },
    })
    return response.data
  },

  async atender(id: number, data: AtendimentoDTORequest): Promise<void> {
    await api.put(`/manutencao/${id}/atendimento`, data)
  },
}
