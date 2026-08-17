package msa.emaia;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import msa.emaia.storage.FilesStorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@Slf4j
public class AssessmentServiceApplication implements CommandLineRunner {
	@Resource
	FilesStorageService storageService;

	public static void main(String[] args) {
		SpringApplication.run(AssessmentServiceApplication.class, args);
	}

	@Override
	public void run(String... arg) throws Exception {
		storageService.init();
	}

}
