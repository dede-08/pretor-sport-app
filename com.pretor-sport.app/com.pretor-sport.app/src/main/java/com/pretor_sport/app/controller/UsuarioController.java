package com.pretor_sport.app.controller;

import com.pretor_sport.app.model.Usuario;
import com.pretor_sport.app.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registro")
    public String registrarUsuario(@Valid @ModelAttribute("usuario") Usuario usuario, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/registro";
        }
        try {
            usuarioService.registrarUsuario(usuario);
            redirectAttributes.addFlashAttribute("registroExitoso", true);
            return "redirect:/login?registroExitoso=true";
        } catch (IllegalStateException e) {
            result.rejectValue("email", "email.existente", e.getMessage());
            return "auth/registro";
        }
    }

    @GetMapping("/perfil")
    public String mostrarPerfil(Authentication authentication, Model model) {
        String email = authentication.getName();
        Usuario usuario = usuarioService.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        model.addAttribute("usuario", usuario);
        return "cliente/perfil";
    }

    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(Authentication authentication, @Valid @ModelAttribute("usuario") Usuario usuario, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "cliente/perfil";
        }

        String email = authentication.getName();
        Usuario usuarioActual = usuarioService.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuarioService.actualizarPerfil(usuarioActual.getId(), usuario);
        redirectAttributes.addFlashAttribute("perfilActualizado", "Tu perfil ha sido actualizado exitosamente.");
        return "redirect:/perfil";
    }
}
