package br.com.novotriunfo.frotas.client;

import br.com.novotriunfo.frotas.entities.dto.request.BaseDTORequest;
import br.com.novotriunfo.frotas.entities.dto.request.ManutencaoDTORequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name="ManutencaoClient", url = "${spring.feign.url}")
public interface ManutencaoClient {

    @RequestMapping(method = RequestMethod.POST, value="/manutencao")
    void cadastrarManutencao(@RequestBody ManutencaoDTORequest request);
}
