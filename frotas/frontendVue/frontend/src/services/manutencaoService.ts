import api from './api'
import type { ManutencaoDTORequest } from '../types'

export const manutencaoService = {
  async cadastrar(data: ManutencaoDTORequest): Promise<void> {
    await api.post('/manutencao', data)
  },
}
