// ============================
// User & Auth DTOs
// ============================

export interface UsuarioResponse {
  id: number
  nome: string
  email: string
  telefone?: string
  role?: string
}

export interface LoginDTORequest {
  email: string
  senha?: string
}

export interface LoginDTOResponse {
  token: string
}

// ============================
// Request DTOs
// ============================

export interface BaseDTORequest {
  nome: string
  usuarioId?: number | null
  responsavelId?: number | null
  emailBase?: string
  telefoneBase?: string
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
  servicos?: Servico[]
}

// ============================
// Response DTOs
// ============================

export interface BaseResponse {
  id: number
  nome: string
  responsavel?: string
  email: string
  telefone: string
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

export interface AtendimentoDTORequest {
  servicos?: Servico[]
  kilometragem?: number | null
  dataRealizacao?: string
}

export interface ManutencaoResponse {
  id: number
  dataRealizacao: string
  dataProximaManutencao: string | null
  kilometragem: number
  tipoManutencao: string
  veiculo: VeiculoResponse
  status: 'EM_ABERTO' | 'FINALIZADA'
  servicos?: Servico[]
}

// ============================
// Enums & Services List
// ============================

export type TipoVeiculo = 'CARRO' | 'CAMINHAO' | 'VAN'
export const TIPOS_VEICULO: TipoVeiculo[] = ['CARRO', 'CAMINHAO', 'VAN']

export type TipoManutencao = 'PREVENTIVA' | 'CORRETIVA' | 'INSPECAO'
export const TIPOS_MANUTENCAO = [
  { value: 'PREVENTIVA', label: 'Preventiva' },
  { value: 'CORRETIVA', label: 'Corretiva' },
  { value: 'INSPECAO', label: 'Inspeção' },
]

export type Servico =
  | 'TROCA_DE_OLEO'
  | 'EMBREAGEM'
  | 'SUSPENSAO'
  | 'PNEUS'
  | 'LATARIA'
  | 'ADESIVOS'
  | 'ITENS_DE_SEGURANCA'
  | 'KIT_CORREIA_DENTADA'
  | 'TAPECARIA'
  | 'FAROIS_LAMPADAS'
  | 'FREIOS'
  | 'ARREFECIMENTO'

export interface ServicoItem {
  value: Servico
  label: string
  icon: string
  description: string
}

export const SERVICOS_LIST: ServicoItem[] = [
  { value: 'TROCA_DE_OLEO', label: 'Troca de Óleo', icon: '🛢️', description: 'Substituição do lubrificante e filtro do motor' },
  { value: 'EMBREAGEM', label: 'Embreagem', icon: '⚙️', description: 'Inspeção do pedal, disco e rolamento' },
  { value: 'SUSPENSAO', label: 'Suspensão', icon: '🔩', description: 'Verificação de amortecedores, molas e pivôs' },
  { value: 'PNEUS', label: 'Pneus', icon: '🛞', description: 'Troca, alinhamento, balanceamento e rodízio' },
  { value: 'LATARIA', label: 'Lataria', icon: '🚘', description: 'Funilaria e reparos estruturais na lataria' },
  { value: 'ADESIVOS', label: 'Adesivos', icon: '🏷️', description: 'Aplicação e substituição de adesivos de identificação' },
  { value: 'ITENS_DE_SEGURANCA', label: 'Itens de Segurança', icon: '🪺', description: 'Triângulo, extintor, chave de roda e macaco' },
  { value: 'KIT_CORREIA_DENTADA', label: 'Kit Correia Dentada', icon: '⛓️', description: 'Troca da correia dentada e esticadores' },
  { value: 'TAPECARIA', label: 'Tapeçaria', icon: '🪑', description: 'Higienização e reforma dos estofados e revestimentos' },
  { value: 'FAROIS_LAMPADAS', label: 'Faróis e Lâmpadas', icon: '💡', description: 'Regulagem e substituição de lâmpadas e faróis' },
  { value: 'FREIOS', label: 'Freios', icon: '🛑', description: 'Pastilhas, discos, lonas e fluido de freio' },
  { value: 'ARREFECIMENTO', label: 'Sistema de Arrefecimento', icon: '🧪', description: 'Revisão do radiador, bomba d\'água e aditivo' },
]
