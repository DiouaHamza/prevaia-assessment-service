package msa.emaia.client.iam;

import msa.emaia.client.iam.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "iam-service", url = "${feign.iam-service.url:http://localhost:8081}/v1")
public interface IamFeignClient {

    @GetMapping("/api/users/{id}")
    UserDto getUserById(@PathVariable("id") String id);

    @GetMapping("/api/users/email/{email}")
    UserDto getUserByEmail(@PathVariable("email") String email);

    @GetMapping("/api/users/role/{roleCode}")
    List<UserDto> getUserByRole(@PathVariable("roleCode") String roleCode);

    @GetMapping("/api/users/current")
    UserDto getCurrentUser();
}
