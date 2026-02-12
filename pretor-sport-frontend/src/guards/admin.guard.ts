import { CanActivateFn } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { map, take } from 'rxjs';

export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  console.log('AdminGuard: Verificando acceso a:', state.url);
  
  //solo usar AuthService para verificar autenticacion
  return authService.currentUser$.pipe(
    take(1),
    map(user => {
      if (!user || !authService.isLoggedIn()) {
        console.log('AdminGuard: Usuario no autenticado, redirigiendo a login');
        router.navigate(['/login']);
        return false;
      }
      
      if (!authService.isAdmin()) {
        console.log('AdminGuard: Usuario sin rol de admin, acceso denegado');
        authService.redirectToUnauthorized();
        return false;
      }
      
      console.log('AdminGuard: Acceso permitido para admin:', user.email);
      return true;
    })
  );
};
