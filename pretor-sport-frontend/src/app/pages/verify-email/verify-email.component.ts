import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './verify-email.component.html',
})
export class VerifyEmailComponent implements OnInit {
  tokenInput = '';
  loading = false;
  message = '';
  errorMessage = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly authService: AuthService
  ) { }

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (token) {
      this.tokenInput = token;
      this.verifyToken(token);
    }
  }

  verifyByInput(): void {
    const token = this.tokenInput.trim();
    if (!token) {
      this.errorMessage = 'Debes ingresar un token de verificación.';
      return;
    }
    this.verifyToken(token);
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }

  private verifyToken(token: string): void {
    this.loading = true;
    this.message = '';
    this.errorMessage = '';

    this.authService.verifyEmail(token).subscribe({
      next: () => {
        this.message = 'Correo verificado exitosamente. Ya puedes iniciar sesión.';
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || error || 'No se pudo verificar el correo.';
        this.loading = false;
      }
    });
  }
}
