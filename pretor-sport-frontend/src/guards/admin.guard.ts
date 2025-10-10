import { CanActivateFn } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

export const adminGuard: CanActivateFn = (route, state) => {

  const userRole = localStorage.getItem('userRole');
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    router.navigate(['/dashboard']);
    return true;
  }

  if (userRole !== 'ROLE_ADMIN') {
    alert('Acceso denegado. Solo los administradores pueden acceder a esta sección.');
    return false;
  }

  return false;
};
