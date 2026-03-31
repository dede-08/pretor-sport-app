import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { CartService } from '../../../services/cart.service';
import { LoggerService } from '../../../services/logger.service';
import { Observable, Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})

export class NavbarComponent implements OnInit, OnDestroy {
  isAuthenticated$!: Observable<boolean>;
  currentUser$!: Observable<any>;
  cartItemCount = 0;
  
  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private cartService: CartService,
    private router: Router,
    private logger: LoggerService
  ) {}

  ngOnInit(): void {
    this.isAuthenticated$ = this.authService.isAuthenticated$;
    this.currentUser$ = this.authService.currentUser$;
    
    //suscribirse a cambios en el carrito
    this.cartService.cartSummary$
      .pipe(takeUntil(this.destroy$))
      .subscribe(summary => {
        this.cartItemCount = summary.totalItems;
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onLogout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.logger.info('Logout exitoso');
        this.router.navigate(['/']);
      },
      error: (error) => {
        this.logger.error('Error en logout:', error);
        //incluso si hay error, navegar al home
        this.router.navigate(['/']);
      }
    });
  }

  onSearch(event: Event): void {
    event.preventDefault();
    const form = event.target as HTMLFormElement;
    const input = form.querySelector('input') as HTMLInputElement | null;
    const termino = input?.value.trim() || '';
    if (termino) {
      // Navegar a la página de productos con el término de búsqueda en query params
      this.router.navigate(['/productos'], { queryParams: { q: termino } });
      // opcional: limpiar campo de búsqueda
      if (input) {
        input.value = '';
      }
    }
  }
}
