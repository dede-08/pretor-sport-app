import { HttpInterceptorFn, HttpRequest, HttpEvent, HttpHandlerFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { catchError, switchMap } from 'rxjs/operators';
import { Observable, throwError } from 'rxjs';

let isRefreshing = false;

export const AuthInterceptor: HttpInterceptorFn = (
  req: HttpRequest<any>,
  next: HttpHandlerFn
): Observable<HttpEvent<any>> => {
  const authService = inject(AuthService);

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
  if (shouldSkip) return next(req);

  const token = authService.getAccessToken();
  let authReq = req;

  if (token) {
    authReq = addTokenToRequest(req, token);
  }

  return next(authReq).pipe(
    catchError((error) => {
      if (error.status === 401 && token && !isRefreshing) {
        return handleTokenExpired(authReq, next, authService);
      }
      if (error.status === 403) {
        console.error('Acceso denegado: permisos insuficientes');
      }
      return throwError(() => error as HttpErrorResponse);
    })
  );
};

const addTokenToRequest = (request: HttpRequest<any>, token: string): HttpRequest<any> => {
  return request.clone({
    setHeaders: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });
};

const handleTokenExpired = (
  request: HttpRequest<any>,
  next: HttpHandlerFn,
  authService: AuthService
): Observable<HttpEvent<any>> => {
  isRefreshing = true;

  return authService.refreshToken().pipe(
    switchMap(() => {
      isRefreshing = false;
      const newToken = authService.getAccessToken();

      if (newToken) {
        const newRequest = addTokenToRequest(request, newToken);
        return next(newRequest);
      }
      return throwError(() => new HttpErrorResponse({ error: 'No se pudo renovar el token', status: 401 }));
    }),
    catchError((error) => {
      isRefreshing = false;
      console.error('Error al renovar token, cerrando sesión');
      authService.logout();
      return throwError(() => error as HttpErrorResponse);
    })
  );
};