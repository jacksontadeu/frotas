// ============================
// Request DTOs
// ============================

export interface BaseDTORequest {
  nome: string
  localidade: string
  emailBase: string
}

export interface VeiculoDTORequest {
  nome: string
  placaVeiculo: string
  frota: string
  base_id: number | null
  tipoVeiculo: string
  cor: string
  anoDeFabricacao: number | null
}

export interface ManutencaoDTORequest {
  dataRealizacao: string
  kilometragem: number | null
  tipoManutencao: string
  veiculo_id: number | null
}

// ============================
// Response DTOs
// ============================

export interface BaseResponse {
  id: number
  nome: string
  localidade: string
  emailBase: string
}

export interface VeiculoResponse {
  id: number
  nome: string
  cor: string
  placaVeiculo: string
  anoDeFabricacao: string
  frota: string
  tipoVeiculo: string
  base: BaseResponse
}

// ============================
// Enums
// ============================

export type TipoVeiculo = 'CARRO' | 'CAMINHAO' | 'VAN'
export const TIPOS_VEICULO: TipoVeiculo[] = ['CARRO', 'CAMINHAO', 'VAN']

export type TipoManutencao = 'preventiva' | 'corretiva' | 'inspecao'
export const TIPOS_MANUTENCAO = [
  { value: 'preventiva', label: 'Preventiva' },
  { value: 'corretiva', label: 'Corretiva' },
  { value: 'inspecao', label: 'Inspeção' },
]
