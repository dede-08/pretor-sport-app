import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { map, tap, catchError } from 'rxjs/operators';
import { 
  Cart, 
  CartItem, 
  CartSummary, 
  AddToCartRequest, 
  UpdateCartItemRequest,
  CartCheckoutRequest 
} from '../models/cart.model';
import { Producto } from '../models/producto.model';

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private readonly API_URL = 'http://localhost:8080/cart';
  private readonly CART_STORAGE_KEY = 'pretor_sport_cart';
  
  private cartSubject = new BehaviorSubject<Cart | null>(this.getCartFromStorage());
  public cart$ = this.cartSubject.asObservable();
  
  private cartSummarySubject = new BehaviorSubject<CartSummary>(this.calculateSummary());
  public cartSummary$ = this.cartSummarySubject.asObservable();

  constructor(private http: HttpClient) {
    //sincronizar carrito local con el servidor si hay usuario autenticado
    this.syncCartWithServer();
  }

  //obtener el carrito actual
  getCart(): Observable<Cart> {
    return this.http.get<Cart>(this.API_URL)
      .pipe(
        tap(cart => {
          this.saveCartToStorage(cart);
          this.cartSubject.next(cart);
          this.updateSummary();
        }),
        catchError(this.handleError)
      );
  }

  //agregar producto al carrito
  addToCart(request: AddToCartRequest): Observable<Cart> {
    return this.http.post<Cart>(`${this.API_URL}/add`, request)
      .pipe(
        tap(cart => {
          this.saveCartToStorage(cart);
          this.cartSubject.next(cart);
          this.updateSummary();
        }),
        catchError(this.handleError)
      );
  }

  //actualizar cantidad de un item
  updateCartItem(request: UpdateCartItemRequest): Observable<Cart> {
    return this.http.put<Cart>(`${this.API_URL}/update`, request)
      .pipe(
        tap(cart => {
          this.saveCartToStorage(cart);
          this.cartSubject.next(cart);
          this.updateSummary();
        }),
        catchError(this.handleError)
      );
  }

  //eliminar item del carrito
  removeFromCart(itemId: string): Observable<Cart> {
    return this.http.delete<Cart>(`${this.API_URL}/remove/${itemId}`)
      .pipe(
        tap(cart => {
          this.saveCartToStorage(cart);
          this.cartSubject.next(cart);
          this.updateSummary();
        }),
        catchError(this.handleError)
      );
  }

  //limpiar carrito
  clearCart(): Observable<void> {
    return this.http.delete<void>(this.API_URL)
      .pipe(
        tap(() => {
          this.clearLocalCart();
          this.cartSubject.next(null);
          this.updateSummary();
        }),
        catchError(this.handleError)
      );
  }

  //aplicar cupón de descuento
  applyCoupon(codigo: string): Observable<Cart> {
    return this.http.post<Cart>(`${this.API_URL}/coupon`, { codigo })
      .pipe(
        tap(cart => {
          this.saveCartToStorage(cart);
          this.cartSubject.next(cart);
          this.updateSummary();
        }),
        catchError(this.handleError)
      );
  }

  //remover cupón de descuento
  removeCoupon(): Observable<Cart> {
    return this.http.delete<Cart>(`${this.API_URL}/coupon`)
      .pipe(
        tap(cart => {
          this.saveCartToStorage(cart);
          this.cartSubject.next(cart);
          this.updateSummary();
        }),
        catchError(this.handleError)
      );
  }

  //procesar checkout
  checkout(request: CartCheckoutRequest): Observable<any> {
    return this.http.post(`${this.API_URL}/checkout`, request)
      .pipe(
        tap(() => {
          this.clearLocalCart();
          this.cartSubject.next(null);
          this.updateSummary();
        }),
        catchError(this.handleError)
      );
  }

  //metodos locales para carrito offline
  addToCartLocal(producto: Producto, cantidad: number = 1, variantes?: any): void {
    const cart = this.getCartFromStorage() || this.createEmptyCart();
    const existingItem = this.findCartItem(cart, producto.id!, variantes);
    
    if (existingItem) {
      existingItem.cantidad += cantidad;
      existingItem.precioTotal = existingItem.cantidad * existingItem.precioUnitario;
    } else {
      const newItem: CartItem = {
        id: this.generateItemId(),
        producto: {
          id: producto.id,
          nombre: producto.nombre,
          precio: producto.precio,
          imagenUrl: producto.imagenUrl,
          stock: producto.stock,
          categoria: producto.categoria
        },
        cantidad,
        precioUnitario: producto.precio,
        precioTotal: producto.precio * cantidad,
        fechaAgregado: new Date(),
        variantes
      };
      cart.items.push(newItem);
    }
    
    this.updateCartTotals(cart);
    this.saveCartToStorage(cart);
    this.cartSubject.next(cart);
    this.updateSummary();
  }

  updateCartItemLocal(itemId: string, cantidad: number): void {
    const cart = this.getCartFromStorage();
    if (!cart) return;
    
    const item = cart.items.find(i => i.id === itemId);
    if (item) {
      if (cantidad <= 0) {
        this.removeFromCartLocal(itemId);
        return;
      }
      
      item.cantidad = cantidad;
      item.precioTotal = item.cantidad * item.precioUnitario;
      this.updateCartTotals(cart);
      this.saveCartToStorage(cart);
      this.cartSubject.next(cart);
      this.updateSummary();
    }
  }

  removeFromCartLocal(itemId: string): void {
    const cart = this.getCartFromStorage();
    if (!cart) return;
    
    cart.items = cart.items.filter(item => item.id !== itemId);
    this.updateCartTotals(cart);
    this.saveCartToStorage(cart);
    this.cartSubject.next(cart);
    this.updateSummary();
  }

  clearCartLocal(): void {
    this.clearLocalCart();
    this.cartSubject.next(null);
    this.updateSummary();
  }

  //obtener resumen del carrito
  getCartSummary(): CartSummary {
    return this.calculateSummary();
  }

  //verificar si el carrito está vacío
  isEmpty(): boolean {
    const cart = this.cartSubject.value;
    return !cart || cart.items.length === 0;
  }

  //obtener cantidad total de items
  getTotalItems(): number {
    const cart = this.cartSubject.value;
    return cart ? cart.items.reduce((total, item) => total + item.cantidad, 0) : 0;
  }

  //metodos privados
  private createEmptyCart(): Cart {
    return {
      items: [],
      subtotal: 0,
      descuento: 0,
      envio: 0,
      total: 0,
      fechaCreacion: new Date(),
      fechaActualizacion: new Date()
    };
  }

  private findCartItem(cart: Cart, productoId: number, variantes?: any): CartItem | undefined {
    return cart.items.find(item => 
      item.producto.id === productoId && 
      this.compareVariantes(item.variantes, variantes)
    );
  }

  private compareVariantes(v1?: any, v2?: any): boolean {
    if (!v1 && !v2) return true;
    if (!v1 || !v2) return false;
    
    return (
      (v1.talla || '') === (v2.talla || '') &&
      (v1.color || '') === (v2.color || '') &&
      (v1.genero || '') === (v2.genero || '')
    );
  }

  private generateItemId(): string {
    return 'item_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
  }

  private updateCartTotals(cart: Cart): void {
    cart.subtotal = cart.items.reduce((total, item) => total + item.precioTotal, 0);
    cart.envio = this.calculateShipping(cart.subtotal);
    cart.total = cart.subtotal + cart.envio - cart.descuento;
    cart.fechaActualizacion = new Date();
  }

  private calculateShipping(subtotal: number): number {
    //logica de cálculo de envío
    if (subtotal >= 500) return 0; //envio gratis
    if (subtotal >= 200) return 50; //envio estandar
    return 100; //envio express
  }

  private calculateSummary(): CartSummary {
    const cart = this.cartSubject.value;
    if (!cart) {
      return {
        totalItems: 0,
        subtotal: 0,
        descuento: 0,
        envio: 0,
        total: 0
      };
    }

    return {
      totalItems: cart.items.reduce((total, item) => total + item.cantidad, 0),
      subtotal: cart.subtotal,
      descuento: cart.descuento,
      envio: cart.envio,
      total: cart.total
    };
  }

  private updateSummary(): void {
    this.cartSummarySubject.next(this.calculateSummary());
  }

  private getCartFromStorage(): Cart | null {
    try {
      const cartData = localStorage.getItem(this.CART_STORAGE_KEY);
      return cartData ? JSON.parse(cartData) : null;
    } catch {
      return null;
    }
  }

  private saveCartToStorage(cart: Cart): void {
    localStorage.setItem(this.CART_STORAGE_KEY, JSON.stringify(cart));
  }

  private clearLocalCart(): void {
    localStorage.removeItem(this.CART_STORAGE_KEY);
  }

  private syncCartWithServer(): void {
    // Sincronizar carrito local con servidor si hay usuario autenticado
    // Esta funcionalidad se implementaría cuando el usuario se autentique
  }

  private handleError(error: any): Observable<never> {
    console.error('Error en CartService:', error);
    return throwError(() => error);
  }
}
