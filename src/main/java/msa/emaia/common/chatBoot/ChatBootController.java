package msa.emaia.common.chatBoot;


import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import msa.emaia.project.Project;
import msa.emaia.project.ProjectService;
import msa.emaia.project.dto.ProjectDto;
import msa.emaia.project.dto.SaveProjectDto;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientException;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@Slf4j
public class ChatBootController {

    @Autowired
    ChatBootApiService chatBootApiService;

    @PostMapping()
    public ChatBootResponse sendMessage(@Valid @RequestBody ChatBootRequest chatBootRequest) throws BadRequestException {
        try {
            return chatBootApiService
                .callChatBoot(chatBootRequest)
                .block();
        } catch (CallNotPermittedException | WebClientException e) {
            log.warn("[ChatBootController] assistant unavailable: {}", e.getMessage());
            return new ChatBootResponse("The assistant is temporarily unavailable, please try again in a moment.", Collections.emptyList());
        }
    }

}
