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

  //URLs que no requieren autenticación
  const skipUrls = [
    '/auth/login',
    '/auth/register',
    '/',
    '/auth/refresh',
    '/auth/verify-email',
    '/auth/health',
    '/auth/roles',
    '/public/',
    '/productos',
    '/categorias'
  ];

  //verificar si la URL debe ser omitida del interceptor
  const shouldSkip = skipUrls.some(url => {
    //extraer la ruta de la URL completa
    const urlPath = new URL(req.url).pathname;
    return urlPath.includes(url);
  });

  //si debe omitirse, enviar la petición sin modificar
  if (shouldSkip) {
    console.log('Skipping auth interceptor for:', req.url);
    return next(req);
  }

  //para URLs que requieren autenticación, agregar el token
  const token = authService.getAccessToken();
  let authReq = req;

  if (token) {
    authReq = addTokenToRequest(req, token);
    console.log('Adding auth token to request:', req.url);
  } else {
    console.warn('No auth token available for protected endpoint:', req.url);
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