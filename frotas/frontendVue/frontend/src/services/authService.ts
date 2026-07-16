import api from './api'
import type { LoginDTORequest, LoginDTOResponse } from '../types'

export const authService = {
  async login(credentials: LoginDTORequest): Promise<LoginDTOResponse> {
    const response = await api.post<LoginDTOResponse>('/login', credentials)
    return response.data
  },
}
