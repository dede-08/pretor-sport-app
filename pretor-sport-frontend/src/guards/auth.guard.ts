import { Injectable } from '@angular/core';
import { CanActivate, CanActivateChild, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { LoggerService } from '../services/logger.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate, CanActivateChild {

  constructor(
    private authService: AuthService,
    private router: Router,
    private logger: LoggerService
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> {
    return this.checkAuth(route, state.url);
  }

  canActivateChild(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> {
    return this.canActivate(route, state);
  }

  private checkAuth(route: ActivatedRouteSnapshot, url: string): Observable<boolean> {
    return this.authService.isAuthenticated$.pipe(
      map(isAuthenticated => {
        if (!isAuthenticated) {
          this.logger.warn('Usuario no autenticado, redirigiendo a login');
          this.router.navigate(['/login'], { queryParams: { returnUrl: url } });
          return false;
        }

        //verificar roles si estan especificados en la ruta
        const requiredRoles = route.data?.['roles'] as string[];
        if (requiredRoles && requiredRoles.length > 0 && !this.authService.hasAnyRole(requiredRoles)) {
          this.logger.warn('Usuario sin permisos suficientes');
          this.router.navigate(['/unauthorized']);
          return false;
        }

        return true;
      })
    );
  }
}
