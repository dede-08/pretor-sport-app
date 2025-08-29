import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent implements OnInit {
  isAuthenticated$!: Observable<boolean>;
  currentUser$!: Observable<any>;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.isAuthenticated$ = this.authService.isAuthenticated$;
    this.currentUser$ = this.authService.currentUser$;
  }

  onLogout(): void {
    this.authService.logout().subscribe({
      next: () => {
        console.log('Logout exitoso');
        this.router.navigate(['/']);
      },
      error: (error) => {
        console.error('Error en logout:', error);
        // Incluso si hay error, navegar al home
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
