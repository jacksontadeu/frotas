package br.com.novotriunfo.frotas.client;

import br.com.novotriunfo.frotas.entities.dto.request.BaseDTORequest;
import br.com.novotriunfo.frotas.entities.dto.response.BaseDTOEntityResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient(name="BaseClient", url = "${spring.feign.url}")
public interface BaseClient {

    @RequestMapping(method = RequestMethod.POST, value="/base")
    void cadastrarBase(@RequestBody BaseDTORequest request);

    @RequestMapping(method = RequestMethod.GET, value="/base")
    List<BaseDTOEntityResponse> listarTodas();



}
