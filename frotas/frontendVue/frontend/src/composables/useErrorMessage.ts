/**
 * Extrai a mensagem de erro de respostas do backend Spring Boot.
 * O Spring pode retornar a mensagem em diferentes campos dependendo
 * do tipo de exceção e configuração.
 */
export function extractErrorMessage(err: any, fallback = 'Ocorreu um erro inesperado.'): string {
  // ResponseStatusException com include-message: always → data.message
  const data = err?.response?.data
  if (!data) return err?.message || fallback

  // Tenta campos comuns do Spring
  return (
    data.message ||      // ResponseStatusException, @Valid
    data.detail ||       // RFC 7807 ProblemDetail (Spring 6+)
    data.error ||        // alguns formatos legados
    data.erro ||         // campo customizado
    err?.response?.statusText ||
    err?.message ||
    fallback
  )
}
