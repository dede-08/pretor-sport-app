import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, RegisterRequest } from '../../../services/auth.service';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css'
})
export class SignupComponent implements OnInit {
  signupForm!: FormGroup;
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initializeForm();
  }

  private initializeForm(): void {
    this.signupForm = this.fb.group({
      nombre: ['', [
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(100)
      ]],
      apellidos: ['', [
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(100)
      ]],
      email: ['', [
        Validators.required,
        Validators.email,
        Validators.maxLength(100)
      ]],
      password: ['', [
        Validators.required,
        Validators.minLength(8),
        Validators.maxLength(60),
        this.passwordValidator
      ]],
      confirmPassword: ['', [
        Validators.required
      ]],
      direccion: ['', [
        Validators.maxLength(255)
      ]],
      telefono: ['', [
        this.phoneValidator
      ]]
    }, {
      validators: [this.passwordMatchValidator]
    });
  }

  // Validador personalizado para contraseña
  private passwordValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return null;
    }

    const hasLowerCase = /[a-z]/.test(control.value);
    const hasUpperCase = /[A-Z]/.test(control.value);
    const hasNumber = /\d/.test(control.value);
    const hasSpecialChar = /[@$!%*?&]/.test(control.value);

    const valid = hasLowerCase && hasUpperCase && hasNumber && hasSpecialChar;

    return valid ? null : {
      passwordRequirements: {
        hasLowerCase,
        hasUpperCase,
        hasNumber,
        hasSpecialChar
      }
    };
  }

  // Validador para teléfono
  private phoneValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return null; // Teléfono es opcional
    }

    const phoneRegex = /^(\+\d{1,3}[- ]?)?\d{10,14}$/;
    const valid = phoneRegex.test(control.value);

    return valid ? null : { phoneFormat: true };
  }

  // Validador para confirmar contraseña
  private passwordMatchValidator(group: AbstractControl): ValidationErrors | null {
    const password = group.get('password')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;

    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  onSubmit(): void {
    if (this.signupForm.valid && !this.isLoading) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      // Crear objeto que coincida con RegisterRequest
      const registerData: RegisterRequest = {
        nombre: this.signupForm.get('nombre')?.value,
        apellidos: this.signupForm.get('apellidos')?.value,
        email: this.signupForm.get('email')?.value,
        password: this.signupForm.get('password')?.value,
        direccion: this.signupForm.get('direccion')?.value || undefined,
        telefono: this.signupForm.get('telefono')?.value || undefined,
        rol: 'ROLE_CLIENTE' // Por defecto
      };

      this.authService.register(registerData).subscribe({
        next: (response) => {
          console.log('Registro exitoso:', response);
          this.successMessage = 'Cuenta creada exitosamente. Redirigiendo...';
          setTimeout(() => {
            this.router.navigate(['/dashboard']);
          }, 2000);
        },
        error: (error) => {
          console.error('Error en registro:', error);
          this.errorMessage = error || 'Error al crear la cuenta';
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
    Object.keys(this.signupForm.controls).forEach(key => {
      const control = this.signupForm.get(key);
      control?.markAsTouched();
    });
  }

  // Getters para facilitar acceso en template
  get nombre() { return this.signupForm.get('nombre'); }
  get apellidos() { return this.signupForm.get('apellidos'); }
  get email() { return this.signupForm.get('email'); }
  get password() { return this.signupForm.get('password'); }
  get confirmPassword() { return this.signupForm.get('confirmPassword'); }
  get direccion() { return this.signupForm.get('direccion'); }
  get telefono() { return this.signupForm.get('telefono'); }

  // Métodos de utilidad para el template
  getErrorMessage(fieldName: string): string {
    const control = this.signupForm.get(fieldName);
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
      if (control.errors['passwordRequirements']) {
        return this.getPasswordRequirementsMessage(control.errors['passwordRequirements']);
      }
      if (control.errors['phoneFormat']) {
        return 'El formato del teléfono no es válido';
      }
    }

    // Verificar errores del formulario completo
    if (fieldName === 'confirmPassword' && this.signupForm.errors?.['passwordMismatch'] && control?.touched) {
      return 'Las contraseñas no coinciden';
    }

    return '';
  }

  private getPasswordRequirementsMessage(requirements: any): string {
    const missing = [];
    if (!requirements.hasLowerCase) missing.push('una letra minúscula');
    if (!requirements.hasUpperCase) missing.push('una letra mayúscula');
    if (!requirements.hasNumber) missing.push('un número');
    if (!requirements.hasSpecialChar) missing.push('un carácter especial (@$!%*?&)');
    
    return `La contraseña debe contener al menos: ${missing.join(', ')}`;
  }

  private getFieldDisplayName(fieldName: string): string {
    const fieldNames: { [key: string]: string } = {
      nombre: 'El nombre',
      apellidos: 'Los apellidos',
      email: 'El email',
      password: 'La contraseña',
      confirmPassword: 'La confirmación de contraseña',
      direccion: 'La dirección',
      telefono: 'El teléfono'
    };
    return fieldNames[fieldName] || fieldName;
  }
}
