import api from './api'
import type { VeiculoDTORequest, VeiculoResponse } from '../types'

export const veiculoService = {
  async cadastrar(data: VeiculoDTORequest): Promise<any> {
    const response= await api.post('/veiculo', data)
    return response.data;
  },

  async listarTodos(): Promise<VeiculoResponse[]> {
    const response = await api.get<VeiculoResponse[]>('/veiculo')
    return response.data
  },

  async buscarPorPlaca(placa: string): Promise<VeiculoResponse | undefined> {
    const veiculos = await this.listarTodos()
    return veiculos.find(
      (v) => v.placaVeiculo.toUpperCase() === placa.toUpperCase()
    )
  },

  async buscarPorPrefixo(prefixo: string): Promise<VeiculoResponse[]> {
    if (!prefixo.trim()) return []
    const veiculos = await this.listarTodos()
    return veiculos
      .filter((v) =>
        v.placaVeiculo.toUpperCase().includes(prefixo.toUpperCase().trim())
      )
      .slice(0, 8)
  },

  async buscarPorFrota(frota: string): Promise<VeiculoResponse | undefined> {
    const veiculos = await this.listarTodos()
    return veiculos.find(
      (v) => v.frota?.toString().toUpperCase() === frota.toUpperCase().trim()
    )
  },

  async excluir(id: number): Promise<void> {
    await api.delete(`/veiculo/${id}`)
  },

  async atualizarKilometragem(veiculoId: number, novaKilometragem: number): Promise<void> {
    await api.patch('/veiculo/kilometragem', null, {
      params: { veiculoId, novaKilometragem },
    })
  },

  async trocarBase(veiculoId: number, baseId: number): Promise<void> {
    await api.patch('/veiculo/base', null, {
      params: { veiculoId, baseId },
    })
  },
}
