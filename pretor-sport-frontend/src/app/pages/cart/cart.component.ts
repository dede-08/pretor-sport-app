import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { CartService } from '../../../services/cart.service';
import { Cart, CartItem, CartSummary } from '../../../models/cart.model';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.css']
})

export class CartComponent implements OnInit, OnDestroy {
  cart: Cart | null = null;
  cartSummary: CartSummary = {
    totalItems: 0,
    subtotal: 0,
    descuento: 0,
    envio: 0,
    total: 0
  };
  loading = false;
  error: string | null = null;
  cuponCodigo = '';
  aplicandoCupon = false;

  private destroy$ = new Subject<void>();

  constructor(
    private cartService: CartService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadCart();
    this.subscribeToCartChanges();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadCart(): void {
    this.loading = true;
    this.error = null;

    this.cartService.getCart()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (cart) => {
          this.cart = cart;
          this.loading = false;
        },
        error: (error) => {
          this.error = 'Error al cargar el carrito';
          this.loading = false;
          console.error('Error loading cart:', error);
        }
      });
  }

  private subscribeToCartChanges(): void {
    this.cartService.cart$
      .pipe(takeUntil(this.destroy$))
      .subscribe(cart => {
        this.cart = cart;
      });

    this.cartService.cartSummary$
      .pipe(takeUntil(this.destroy$))
      .subscribe(summary => {
        this.cartSummary = summary;
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
        console.error('Error updating cart item:', error);
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
        console.error('Error removing cart item:', error);
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
          console.error('Error clearing cart:', error);
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
        console.error('Error removing coupon:', error);
      }
    });
  }

  proceedToCheckout(): void {
    if (this.cart?.items.length === 0) {
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
    return !this.cart || this.cart.items.length === 0;
  }

  getShippingMessage(): string {
    if (this.cartSummary.subtotal >= 500) {
      return '¡Envío gratis!';
    }
    if (this.cartSummary.subtotal >= 200) {
      return 'Agrega S/. ' + (500 - this.cartSummary.subtotal).toFixed(2) + ' más para envío gratis';
    }
    return 'Envío estándar: S/. ' + this.cartSummary.envio.toFixed(2);
  }

  getShippingMessageClass(): string {
    if (this.cartSummary.subtotal >= 500) {
      return 'text-success';
    }
    if (this.cartSummary.subtotal >= 200) {
      return 'text-info';
    }
    return 'text-muted';
  }
}