import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { CartService } from '../../../services/cart.service';
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
    private router: Router
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
        console.log('Logout exitoso');
        this.router.navigate(['/']);
      },
      error: (error) => {
        console.error('Error en logout:', error);
        //incluso si hay error, navegar al home
        this.router.navigate(['/']);
      }
    });
  }

  onSearch(event: Event): void {
    event.preventDefault();
    // TODO: Implementar lógica de búsqueda
    console.log('Funcionalidad de búsqueda pendiente');
  }
}
