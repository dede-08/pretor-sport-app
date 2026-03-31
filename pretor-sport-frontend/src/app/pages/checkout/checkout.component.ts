import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="container py-5">
      <h2>Checkout</h2>
      <p class="text-muted mb-3">Estamos preparando esta funcionalidad.</p>
      <a routerLink="/carrito" class="btn btn-primary">Volver al carrito</a>
    </div>
  `
})
export class CheckoutComponent {}
