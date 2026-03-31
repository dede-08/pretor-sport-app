import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-unauthorized',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="container py-5">
      <h2>Acceso denegado</h2>
      <p class="text-muted mb-3">No tienes permisos para acceder a esta sección.</p>
      <a routerLink="/" class="btn btn-primary">Ir al inicio</a>
    </div>
  `
})
export class UnauthorizedComponent {}
