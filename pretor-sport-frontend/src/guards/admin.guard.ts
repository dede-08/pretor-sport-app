import { CanActivateFn } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  console.log('AdminGuard: Verificando acceso a:', state.url);
  
  // Obtener el usuario actual directamente del localStorage para evitar problemas de timing
  const userData = localStorage.getItem('user_data');
  const user = userData ? JSON.parse(userData) : null;
  
  console.log('AdminGuard: Usuario del localStorage:', user);
  console.log('AdminGuard: Usuario autenticado (service):', authService.isLoggedIn());
  console.log('AdminGuard: Es admin (service):', authService.isAdmin());
  console.log('AdminGuard: Token válido:', !!authService.getAccessToken());

  // Verificar si hay token válido
  const token = authService.getAccessToken();
  if (!token) {
    console.log('AdminGuard: No hay token, redirigiendo a login');
    router.navigate(['/login']);
    return false;
  }

  // Verificar si el usuario tiene rol de administrador (usando datos directos del localStorage)
  if (!user || user.rol !== 'ROLE_ADMIN') {
    console.log('AdminGuard: Usuario no es admin, acceso denegado');
    alert('Acceso denegado. Solo los administradores pueden acceder a esta sección.');
    router.navigate(['/']);
    return false;
  }

  console.log('AdminGuard: Acceso permitido');
  return true;
};
