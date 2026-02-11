import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, LoginRequest } from '../../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})

export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  isLoading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initializeForm();
  }

  private initializeForm(): void {
    this.loginForm = this.fb.group({
      email: ['', [
        Validators.required,
        Validators.email,
        Validators.maxLength(100)
      ]],
      password: ['', [
        Validators.required,
        Validators.minLength(6)
      ]],
      rememberMe: [false]
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid && !this.isLoading) {
      this.isLoading = true;
      this.errorMessage = '';

      const loginData: LoginRequest = this.loginForm.value;

      this.authService.login(loginData).subscribe({
        next: (response) => {
          console.log('Login exitoso:', response);
          console.log('Usuario recibido:', response.usuario);
          console.log('Rol del usuario:', response.usuario.rol);
          this.redirectBasedOnRole(response.usuario.rol);
        },
        error: (error) => {
          console.error('Error en login:', error);
          this.errorMessage = error || 'Error de autenticación';
          this.isLoading = false;
        },
        complete: () => {
          this.isLoading = false;
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  private markFormGroupTouched(): void {
    Object.keys(this.loginForm.controls).forEach(key => {
      const control = this.loginForm.get(key);
      control?.markAsTouched();
    });
  }

  private redirectBasedOnRole(userRole: string): void {
    console.log('Iniciando redirección basada en rol:', userRole);
    
    //usar setTimeout para asegurar que el estado de autenticación se haya actualizado
    setTimeout(() => {
      switch (userRole) {
        case 'ROLE_ADMIN':
          console.log('Detectado rol ADMIN - Redirigiendo al dashboard de administrador');
          this.router.navigateByUrl('/admin').then(success => {
            if (success) {
              console.log('Redirección exitosa a /admin');
            } else {
              console.error('Error en redirección a /admin, intentando con window.location');
              //fallback usando window.location si router.navigate falla
              window.location.href = '/admin';
            }
          }).catch(error => {
            console.error('Error en redirección a /admin:', error);
            //fallback: redirigir al home
            this.router.navigate(['/']);
          });
          break;
        case 'ROLE_EMPLEADO':
          console.log('Detectado rol EMPLEADO - Redirigiendo al home');
          this.router.navigate(['/']);
          break;
        case 'ROLE_CLIENTE':
          console.log('Detectado rol CLIENTE - Redirigiendo al home');
          this.router.navigate(['/']);
          break;
        default:
          console.warn('Rol no reconocido:', userRole, '- Redirigiendo al home por defecto');
          this.router.navigate(['/']);
          break;
      }
    }, 100); //pequeño delay para asegurar que el estado se actualice
  }

  //getters
  get email() { return this.loginForm.get('email'); }
  get password() { return this.loginForm.get('password'); }
  get rememberMe() { return this.loginForm.get('rememberMe'); }

  //metodos de utilidad para el template
  getErrorMessage(fieldName: string): string {
    const control = this.loginForm.get(fieldName);
    if (control?.errors && control?.touched) {
      if (control.errors['required']) {
        return `${this.getFieldDisplayName(fieldName)} es obligatorio`;
      }
      if (control.errors['email']) {
        return 'El formato del email no es válido';
      }
      if (control.errors['minlength']) {
        return `${this.getFieldDisplayName(fieldName)} debe tener al menos ${control.errors['minlength'].requiredLength} caracteres`;
      }
      if (control.errors['maxlength']) {
        return `${this.getFieldDisplayName(fieldName)} no puede exceder ${control.errors['maxlength'].requiredLength} caracteres`;
      }
    }
    return '';
  }

  private getFieldDisplayName(fieldName: string): string {
    const fieldNames: { [key: string]: string } = {
      email: 'El email',
      password: 'La contraseña'
    };
    return fieldNames[fieldName] || fieldName;
  }
}
