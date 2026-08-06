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
    const response = await api.get<ManutencaoResponse[]>('/manutencao/status', {
      params: { status: 'EM_ABERTO' },
    })
    return response.data
  },

  async atender(id: number, data: AtendimentoDTORequest): Promise<void> {
    await api.patch(`/atendimento/${id}`, data)
  },

  async gerarRelatorioDetalhado(manutencaoIds: number[]): Promise<Blob> {
    const response = await api.post('/relatorios/manutencoes/detalhada', manutencaoIds, {
      responseType: 'blob'
    })
    return response.data
  },

  async gerarRelatorioListarTodas(): Promise<Blob> {
    const response = await api.get('/relatorios/manutencao/listartodas', {
      responseType: 'blob'
    })
    return response.data
  },

  async gerarRelatorioManutencaoDetalhada(id: number): Promise<Blob> {
    const response = await api.get(`/relatorios/manutencao/${id}`, {
      responseType: 'blob'
    })
    return response.data
  }
}
