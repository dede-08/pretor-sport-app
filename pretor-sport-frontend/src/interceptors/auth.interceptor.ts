import { HttpInterceptorFn, HttpRequest, HttpEvent, HttpHandlerFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { LoggerService } from '../services/logger.service';
import { catchError, switchMap } from 'rxjs/operators';
import { Observable, throwError } from 'rxjs';

let isRefreshing = false;
const PUBLIC_EXACT_PATHS = new Set([
  '/auth/login',
  '/auth/register',
  '/auth/refresh',
  '/auth/verify-email',
  '/auth/resend-verification',
  '/auth/health',
  '/auth/roles',
  '/productos',
  '/categorias'
]);

const PUBLIC_PREFIX_PATHS = ['/public/'];

const normalizePath = (path: string): string => {
  if (path.startsWith('/api/')) {
    return path.substring(4);
  }
  if (path === '/api') {
    return '/';
  }
  return path;
};

export const AuthInterceptor: HttpInterceptorFn = (
  req: HttpRequest<any>,
  next: HttpHandlerFn
): Observable<HttpEvent<any>> => {
  const authService = inject(AuthService);
  const logger = inject(LoggerService);

  //URLs que no requieren autenticación
  const rawPath = new URL(req.url, window.location.origin).pathname;
  const urlPath = normalizePath(rawPath);
  const shouldSkip =
    PUBLIC_EXACT_PATHS.has(urlPath) ||
    PUBLIC_PREFIX_PATHS.some(prefix => urlPath.startsWith(prefix));

  //si debe omitirse, enviar la peticion sin modificar
  if (shouldSkip) {
    logger.debug('Skipping auth interceptor for:', req.url);
    return next(req);
  }

  //para URLs que requieren autenticacion, agregar el token
  const token = authService.getAccessToken();
  let authReq = req;

  if (token) {
    authReq = addTokenToRequest(req, token);
    logger.debug('Adding auth token to request:', req.url);
  } else {
    logger.warn('No auth token available for protected endpoint:', req.url);
  }

  return next(authReq).pipe(
    catchError((error) => {
      if (error.status === 401 && token && !isRefreshing) {
        return handleTokenExpired(authReq, next, authService);
      }
      if (error.status === 403) {
        logger.error('Acceso denegado: permisos insuficientes');
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
  const logger = inject(LoggerService);
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
      logger.error('Error al renovar token, cerrando sesión');
      authService.forceLogoutLocal();
      return throwError(() => error as HttpErrorResponse);
    })
  );
};