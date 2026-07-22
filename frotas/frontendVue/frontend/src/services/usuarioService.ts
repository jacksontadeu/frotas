import api from './api'
import type { UsuarioResponse } from '../types'

export interface UsuarioDTORequest {
  nome: string
  email: string
  senha?: string
  telefone?: string
  role?: string
}

export const usuarioService = {
  async listarTodos(): Promise<UsuarioResponse[]> {
    try {
      const response = await api.get<UsuarioResponse[]>('/usuario')
      if (Array.isArray(response.data)) {
        return response.data
      }
      return []
    } catch (err: any) {
      console.warn('Tentando rota alternativa /usuario/listar após erro em /usuario:', err?.message)
      try {
        const responseFallback = await api.get<UsuarioResponse[]>('/usuario/listar')
        if (Array.isArray(responseFallback.data)) {
          return responseFallback.data
        }
        return []
      } catch (errFallback: any) {
        console.error('Erro ao buscar lista de usuários do backend:', errFallback)
        throw errFallback
      }
    }
  },

  async cadastrar(data: UsuarioDTORequest): Promise<UsuarioResponse> {
    const response = await api.post<UsuarioResponse>('/usuario', data)
    return response.data
  },

  async atualizar(id: number, data: UsuarioDTORequest): Promise<UsuarioResponse> {
    const response = await api.put<UsuarioResponse>(`/usuario/${id}`, data)
    return response.data
  },

  async excluir(id: number): Promise<void> {
    await api.delete(`/usuario/${id}`)
  },

  async buscarPorId(id: number): Promise<UsuarioResponse> {
    const response = await api.get<UsuarioResponse>(`/usuario/${id}`)
    return response.data
  },
}
