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

//	@Bean
//	CommandLineRunner createAdminUser(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
//		return args -> {
//			String adminEmail = "admin@pretor.com";
//			if (usuarioRepository.findByEmail(adminEmail).isEmpty()) {
//				Usuario admin = new Usuario();
//				admin.setNombre("Admin");
//				admin.setApellidos("Pretor Sport");
//				admin.setEmail(adminEmail);
//				admin.setPassword(passwordEncoder.encode("pretorsportadmin100@"));
//				admin.setRol(Usuario.Rol.ROLE_ADMIN);
//				admin.setActivo(true);
//				admin.setEmailVerificado(true);
//				usuarioRepository.save(admin);
//				System.out.println("Usuario administrador creado: " + adminEmail);
//			}
//		};
//	}
}