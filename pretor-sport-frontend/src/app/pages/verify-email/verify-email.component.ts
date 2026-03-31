import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="container py-5" style="max-width: 720px;">
      <div class="card shadow-sm">
        <div class="card-body p-4 p-md-5">
          <h2 class="mb-3">Verificación de correo</h2>
          <p class="text-muted mb-4">
            Ingresa tu token o abre este enlace con <code>?token=...</code>.
          </p>

          <div *ngIf="message" class="alert alert-success mb-3">{{ message }}</div>
          <div *ngIf="errorMessage" class="alert alert-danger mb-3">{{ errorMessage }}</div>

          <form (ngSubmit)="verifyByInput()" novalidate>
            <div class="mb-3">
              <label for="token" class="form-label">Token de verificación</label>
              <input
                id="token"
                class="form-control"
                type="text"
                [(ngModel)]="tokenInput"
                name="tokenInput"
                placeholder="Pega aquí tu token">
            </div>

            <div class="d-flex gap-2">
              <button class="btn btn-primary" type="submit" [disabled]="loading">
                {{ loading ? 'Verificando...' : 'Verificar correo' }}
              </button>
              <button class="btn btn-outline-secondary" type="button" (click)="goToLogin()">
                Ir al login
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  `
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
  ) {}

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
