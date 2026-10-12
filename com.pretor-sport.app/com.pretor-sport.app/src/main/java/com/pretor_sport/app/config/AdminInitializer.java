package com.pretor_sport.app.config;

import com.pretor_sport.app.model.Usuario;
import com.pretor_sport.app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Crea un usuario administrador inicial si se definen ADMIN_EMAIL y ADMIN_PASSWORD.
 * No hace nada si las variables no estan definidas o si el email ya existe.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Value("${app.admin.nombre:Administrador}")
    private String adminNombre;

    @Value("${app.admin.apellidos:PretorSport}")
    private String adminApellidos;

    @Override
    public void run(String... args) {
        if (!StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
            return;
        }

        if (usuarioRepository.existsByEmail(adminEmail)) {
            log.info("Admin seed: el usuario {} ya existe, no se crea", adminEmail);
            return;
        }

        Usuario admin = new Usuario();
        admin.setNombre(adminNombre);
        admin.setApellidos(adminApellidos);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRol(Usuario.Rol.ROLE_ADMIN);
        admin.setActivo(true);
        admin.setEmailVerificado(true);

        usuarioRepository.save(admin);
        log.info("Admin seed: usuario administrador {} creado", adminEmail);
    }
}
