package com.pretor_sport.app.controller;

import com.pretor_sport.app.model.Cliente;
import com.pretor_sport.app.service.ClienteService;
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
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping("/registro")
    public String registrarCliente(@Valid @ModelAttribute("cliente") Cliente cliente, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/registro";
        }
        try {
            clienteService.registrarCliente(cliente);
            redirectAttributes.addFlashAttribute("registroExitoso", true);
            return "redirect:/login?registroExitoso=true";
        } catch (IllegalStateException e) {
            result.rejectValue("email", "email.existente", e.getMessage());
            return "auth/registro";
        }
    }

    @GetMapping("/perfil")
    public String mostrarPerfil(Authentication authentication, Model model) {
        String email = authentication.getName(); // Obtiene el email del usuario autenticado
        Cliente cliente = clienteService.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        model.addAttribute("cliente", cliente);
        return "cliente/perfil";
    }

    @PostMapping("/perfil/actualizar")
    public String actualizarPerfil(Authentication authentication, @ModelAttribute Cliente clienteDatos, RedirectAttributes redirectAttributes) {
        String email = authentication.getName();
        Cliente clienteActual = clienteService.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        clienteService.actualizarPerfil(clienteActual.getId(), clienteDatos);
        redirectAttributes.addFlashAttribute("perfilActualizado", "Tu perfil ha sido actualizado exitosamente.");
        return "redirect:/perfil";
    }
}