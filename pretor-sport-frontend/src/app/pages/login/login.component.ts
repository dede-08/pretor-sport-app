import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService, LoginRequest } from '../../../services/auth.service';
import { LoggerService } from '../../../services/logger.service';

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
  isResendingVerification = false;
  errorMessage = '';
  infoMessage = '';
  verificationUrl: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router,
    private logger: LoggerService
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.showVerificationHintIfNeeded();
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

  private showVerificationHintIfNeeded(): void {
    const verifyPending = this.route.snapshot.queryParamMap.get('verifyPending');
    if (verifyPending === '1') {
      this.infoMessage = 'Tu cuenta fue creada. Verifica tu correo antes de iniciar sesión.';
    }
  }

  onSubmit(): void {
    if (this.loginForm.valid && !this.isLoading) {
      this.isLoading = true;
      this.errorMessage = '';

      const loginData: LoginRequest = this.loginForm.value;

      this.authService.login(loginData).subscribe({
        next: (response) => {
          this.logger.debug('Login exitoso:', response);
          this.logger.debug('Usuario recibido:', response.usuario);
          this.logger.debug('Rol del usuario:', response.usuario.rol);
          this.redirectBasedOnRole(response.usuario.rol);
        },
        error: (error) => {
          this.logger.error('Error en login:', error);
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

  resendVerification(): void {
    const email = this.email?.value?.trim();
    if (!email) {
      this.errorMessage = 'Ingresa tu email para reenviar la verificación.';
      return;
    }

    this.isResendingVerification = true;
    this.errorMessage = '';
    this.infoMessage = '';
    this.verificationUrl = null;

    this.authService.resendVerificationEmail(email).subscribe({
      next: (response) => {
        this.infoMessage = response.message || 'Se reenvió la verificación.';
        this.verificationUrl = response.verificationUrl || null;
        this.isResendingVerification = false;
      },
      error: (error) => {
        this.errorMessage = error || 'No se pudo reenviar la verificación.';
        this.isResendingVerification = false;
      }
    });
  }

  private markFormGroupTouched(): void {
    Object.keys(this.loginForm.controls).forEach(key => {
      const control = this.loginForm.get(key);
      control?.markAsTouched();
    });
  }

  private redirectBasedOnRole(userRole: string): void {
    this.logger.debug('Iniciando redirección basada en rol:', userRole);
    
    //usar setTimeout para asegurar que el estado de autenticación se haya actualizado
    setTimeout(() => {
      switch (userRole) {
        case 'ROLE_ADMIN':
          this.logger.info('Detectado rol ADMIN - Redirigiendo al dashboard de administrador');
          this.router.navigateByUrl('/admin').then(success => {
            if (success) {
              this.logger.info('Redirección exitosa a /admin');
            } else {
              this.logger.error('Error en redirección a /admin, intentando con window.location');
              //fallback usando window.location si router.navigate falla
              window.location.href = '/admin';
            }
          }).catch(error => {
            this.logger.error('Error en redirección a /admin:', error);
            //fallback: redirigir al home
            this.router.navigate(['/']);
          });
          break;
        case 'ROLE_EMPLEADO':
          this.logger.info('Detectado rol EMPLEADO - Redirigiendo al home');
          this.router.navigate(['/']);
          break;
        case 'ROLE_CLIENTE':
          this.logger.info('Detectado rol CLIENTE - Redirigiendo al home');
          this.router.navigate(['/']);
          break;
        default:
          this.logger.warn('Rol no reconocido:', userRole, '- Redirigiendo al home por defecto');
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
