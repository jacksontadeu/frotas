package br.com.novotriunfo.frotas.client;

import br.com.novotriunfo.frotas.entities.dto.request.VeiculoDTORequest;
import br.com.novotriunfo.frotas.entities.dto.response.VeiculoDTOResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient(name="VeiculoClient", url = "${spring.feign.url}")
public interface VeiculoClient {
    @RequestMapping(method = RequestMethod.POST, value="/veiculo")
    void cadastrarVeiculo(@RequestBody VeiculoDTORequest request);

    @RequestMapping(method = RequestMethod.GET, value="/veiculo")
    List<VeiculoDTOResponse> listarTodos();


}
