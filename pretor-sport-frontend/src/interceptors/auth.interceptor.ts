import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  private isRefreshing = false;

  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // URLs que no necesitan token
    const skipUrls = [
      '/api/auth/login',
      '/api/auth/register',
      '/api/auth/refresh',
      '/api/auth/verify-email',
      '/api/auth/health',
      '/api/auth/roles',
      '/api/public/'
    ];

    const shouldSkip = skipUrls.some(url => req.url.includes(url));
    
    if (shouldSkip) {
      return next.handle(req);
    }

    // Agregar token de autorización
    const token = this.authService.getAccessToken();
    let authReq = req;

    if (token) {
      authReq = this.addTokenToRequest(req, token);
    }

    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        // Si el token ha expirado (401), intentar renovarlo
        if (error.status === 401 && token && !this.isRefreshing) {
          return this.handleTokenExpired(authReq, next);
        }
        
        // Si es un error 403, el usuario no tiene permisos
        if (error.status === 403) {
          console.error('Acceso denegado: permisos insuficientes');
        }

        return throwError(() => error);
      })
    );
  }

  private addTokenToRequest(request: HttpRequest<any>, token: string): HttpRequest<any> {
    return request.clone({
      setHeaders: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
  }

  private handleTokenExpired(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    this.isRefreshing = true;

    return this.authService.refreshToken().pipe(
      switchMap(() => {
        this.isRefreshing = false;
        const newToken = this.authService.getAccessToken();
        
        if (newToken) {
          const newRequest = this.addTokenToRequest(request, newToken);
          return next.handle(newRequest);
        }
        
        return throwError(() => 'No se pudo renovar el token');
      }),
      catchError((error) => {
        this.isRefreshing = false;
        
        // Si falla la renovación del token, cerrar sesión
        console.error('Error al renovar token, cerrando sesión');
        this.authService.logout();
        
        return throwError(() => error);
      })
    );
  }
}
