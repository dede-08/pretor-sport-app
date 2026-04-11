import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CartService } from '../../../services/cart.service';
import { LoggerService } from '../../../services/logger.service';
import { CartItem } from '../../../models/cart.model';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.css']
})
export class CartComponent implements OnInit {
  // signals directos desde el servicio
  cart = this.cartService.cart;
  cartSummary = this.cartService.cartSummary;

  loading = false;
  error: string | null = null;
  cuponCodigo = '';
  aplicandoCupon = false;

  constructor(
    private cartService: CartService,
    private router: Router,
    private logger: LoggerService
  ) { }

  ngOnInit(): void {
    this.loadCart();
  }

  private loadCart(): void {
    this.loading = true;
    this.error = null;

    this.cartService.getCart().subscribe({
      next: () => {
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Error al cargar el carrito';
        this.loading = false;
        this.logger.error('Error loading cart:', error);
      }
    });
  }

  onQuantityChange(item: CartItem, value: string): void {
    const newQuantity = parseInt(value, 10);
    if (!isNaN(newQuantity)) {
      this.updateQuantity(item, newQuantity);
    }
  }

  setDefaultImage(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = 'assets/default-product.png';
  }

  updateQuantity(item: CartItem, newQuantity: number): void {
    if (isNaN(newQuantity) || newQuantity < 1) {
      this.removeItem(item.id);
      return;
    }

    if (newQuantity > item.producto.stock) {
      this.error = `Solo hay ${item.producto.stock} unidades disponibles`;
      return;
    }

    this.cartService.updateCartItem({
      itemId: item.id,
      cantidad: newQuantity
    }).subscribe({
      next: () => this.error = null,
      error: (error) => {
        this.error = 'Error al actualizar la cantidad';
        this.logger.error('Error updating cart item:', error);
      }
    });
  }

  removeItem(itemId: string): void {
    this.cartService.removeFromCart(itemId).subscribe({
      next: () => {
        this.error = null;
      },
      error: (error) => {
        this.error = 'Error al eliminar el producto';
        this.logger.error('Error removing cart item:', error);
      }
    });
  }

  clearCart(): void {
    if (confirm('¿Estás seguro de que quieres vaciar el carrito?')) {
      this.cartService.clearCart().subscribe({
        next: () => {
          this.error = null;
        },
        error: (error) => {
          this.error = 'Error al vaciar el carrito';
          this.logger.error('Error clearing cart:', error);
        }
      });
    }
  }

  applyCoupon(): void {
    if (!this.cuponCodigo.trim()) {
      this.error = 'Por favor ingresa un código de cupón';
      return;
    }

    this.aplicandoCupon = true;
    this.error = null;

    this.cartService.applyCoupon(this.cuponCodigo.trim()).subscribe({
      next: () => {
        this.cuponCodigo = '';
        this.aplicandoCupon = false;
      },
      error: (error) => {
        this.error = error.error?.message || 'Código de cupón inválido';
        this.aplicandoCupon = false;
      }
    });
  }

  removeCoupon(): void {
    this.cartService.removeCoupon().subscribe({
      next: () => {
        this.error = null;
      },
      error: (error) => {
        this.error = 'Error al remover el cupón';
        this.logger.error('Error removing coupon:', error);
      }
    });
  }

  proceedToCheckout(): void {
    if (this.isCartEmpty()) {
      this.error = 'El carrito está vacío';
      return;
    }

    this.router.navigate(['/checkout']);
  }

  continueShopping(): void {
    this.router.navigate(['/productos']);
  }

  formatPrice(price: number): string {
    return `S/. ${price.toFixed(2)}`;
  }

  getStockStatus(item: CartItem): string {
    if (item.producto.stock === 0) return 'Sin stock';
    if (item.producto.stock < 5) return 'Stock bajo';
    return 'Disponible';
  }

  getStockStatusClass(item: CartItem): string {
    if (item.producto.stock === 0) return 'text-danger';
    if (item.producto.stock < 5) return 'text-warning';
    return 'text-success';
  }

  isCartEmpty(): boolean {
    const currentCart = this.cart();
    return !currentCart || currentCart.items.length === 0;
  }

  getShippingMessage(): string {
    const summary = this.cartSummary();
    if (summary.subtotal >= 500) {
      return '¡Envío gratis!';
    }
    if (summary.subtotal >= 200) {
      return 'Agrega S/. ' + (500 - summary.subtotal).toFixed(2) + ' más para envío gratis';
    }
    return 'Envío estándar: S/. ' + summary.envio.toFixed(2);
  }

  getShippingMessageClass(): string {
    const summary = this.cartSummary();
    if (summary.subtotal >= 500) {
      return 'text-success';
    }
    if (summary.subtotal >= 200) {
      return 'text-info';
    }
    return 'text-muted';
  }
}