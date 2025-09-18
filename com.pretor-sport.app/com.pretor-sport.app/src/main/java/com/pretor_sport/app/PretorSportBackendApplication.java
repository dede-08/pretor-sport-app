package com.pretor_sport.app;

import com.pretor_sport.app.model.Usuario;
import com.pretor_sport.app.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class PretorSportBackendApplication {

	public static void main(String[] args) {
        SpringApplication.run(PretorSportBackendApplication.class, args);
	}


}