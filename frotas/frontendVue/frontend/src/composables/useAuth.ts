import { ref, computed } from 'vue'
import { authService } from '../services/authService'
import type { LoginDTORequest } from '../types'

interface UserSession {
  email: string
  nome: string
  id: number
  role: string
}

const token = ref<string | null>(sessionStorage.getItem('token'))
const user = ref<UserSession | null>(null)
const errorMsg = ref<string | null>(null)
const isLoading = ref(false)

function parseJwt(t: string): UserSession | null {
  try {
    const base64Url = t.split('.')[1]
    if (!base64Url) return null
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(
      window
        .atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    const decoded = JSON.parse(jsonPayload)
    const rawRole = (decoded.role || '').toString().trim()
    const normalizedRole = rawRole ? (rawRole.startsWith('ROLE_') ? rawRole : `ROLE_${rawRole}`) : ''

    return {
      email: decoded.sub || '',
      nome: decoded.nome || decoded.sub || 'Usuário',
      id: decoded.id || 0,
      role: normalizedRole,
    }
  } catch (e) {
    return null
  }
}

// Inicializa a sessão imediatamente caso já exista um token salvo no sessionStorage
if (token.value && !user.value) {
  const initialUser = parseJwt(token.value)
  if (initialUser) {
    user.value = initialUser
  } else {
    sessionStorage.removeItem('token')
    token.value = null
  }
}

export function useAuth() {
  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ROLE_ADMIN')
  const isTecnico = computed(() => user.value?.role === 'ROLE_TECNICO')

  async function login(credentials: LoginDTORequest): Promise<boolean> {
    isLoading.value = true
    errorMsg.value = null
    try {
      const response = await authService.login(credentials)
      const jwtToken = response.token
      
      const decodedUser = parseJwt(jwtToken)
      if (!decodedUser) {
        throw new Error('Token inválido ou corrompido.')
      }

      if (decodedUser.role !== 'ROLE_ADMIN' && decodedUser.role !== 'ROLE_TECNICO') {
        throw new Error('Acesso negado: apenas administradores e técnicos podem acessar o sistema.')
      }

      token.value = jwtToken
      user.value = decodedUser
      sessionStorage.setItem('token', jwtToken)
      return true
    } catch (err: any) {
      if (err.response?.status === 403 || err.response?.status === 401) {
        errorMsg.value = 'E-mail ou senha incorretos.'
      } else {
        errorMsg.value = err.message || 'Erro ao realizar login. Tente novamente.'
      }
      return false
    } finally {
      isLoading.value = false
    }
  }

  function logout() {
    token.value = null
    user.value = null
    errorMsg.value = null
    sessionStorage.removeItem('token')
  }

  function initAuth() {
    const storedToken = sessionStorage.getItem('token')
    if (storedToken) {
      const decodedUser = parseJwt(storedToken)
      if (decodedUser) {
        token.value = storedToken
        user.value = decodedUser
      } else {
        logout()
      }
    }
  }

  return {
    token,
    user,
    errorMsg,
    isLoading,
    isAuthenticated,
    isAdmin,
    isTecnico,
    login,
    logout,
    initAuth,
  }
}
