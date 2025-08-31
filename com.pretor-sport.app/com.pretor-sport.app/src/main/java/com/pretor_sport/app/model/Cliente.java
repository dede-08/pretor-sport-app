package com.pretor_sport.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "clientes", indexes = {
    @Index(name = "idx_cliente_email", columnList = "email"),
    @Index(name = "idx_cliente_rol", columnList = "rol"),
    @Index(name = "idx_cliente_activo", columnList = "activo")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
    private String apellidos;

    @Column(nullable = false, unique = true, length = 100)
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    private String email;

    @Column(nullable = false, length = 255)
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    @Column(length = 255)
    private String direccion;

    @Column(length = 20)
    @Pattern(
        regexp = "^(\\+\\d{1,3}[- ]?)?\\d{10,14}$",
        message = "El formato del teléfono no es válido"
    )
    private String telefono;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Rol rol = Rol.ROLE_CLIENTE;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "email_verificado", nullable = false)
    private Boolean emailVerificado = false;

    @Column(name = "token_verificacion")
    private String tokenVerificacion;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @CreatedDate
    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Enum para roles
    public enum Rol {
        ROLE_CLIENTE("Cliente"),
        ROLE_EMPLEADO("Empleado"),
        ROLE_ADMIN("Administrador");

        private final String displayName;

        Rol(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Métodos de utilidad
    @Transient
    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }

    @Transient
    public String getIniciales() {
        String inicial1 = nombre != null && !nombre.isEmpty() ? nombre.substring(0, 1).toUpperCase() : "";
        String inicial2 = apellidos != null && !apellidos.isEmpty() ? apellidos.substring(0, 1).toUpperCase() : "";
        return inicial1 + inicial2;
    }

    @Transient
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(rol.name()));
    }

    //metodo para verificar si el usuario esta habilitado
    @Transient
    public boolean isEnabled() {
        return activo && emailVerificado;
    }

    @Transient
    public boolean isAccountNonExpired() {
        return true;
    }

    @Transient
    public boolean isAccountNonLocked() {
        return activo;
    }

    @Transient
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
