import { HttpInterceptorFn, HttpRequest, HttpEvent, HttpHandlerFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { LoggerService } from '../services/logger.service';
import { catchError, switchMap } from 'rxjs/operators';
import { Observable, Subject, throwError } from 'rxjs';

//Cola de peticiones pendientes mientras se renueva el token, para evitar
//múltiples refreshes concurrentes y descartar peticiones que llegaron juntas.
let isRefreshing = false;
let refreshSubject: Subject<boolean> | null = null;
const pendingRequests: { request: HttpRequest<any>, next: HttpHandlerFn }[] = [];

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
      if (error.status === 401 && token) {
        return handleTokenExpired(authReq, next, authService, logger);
      }
      if (error.status === 403) {
        logger.error('Acceso denegado: permisos insuficientes');
      }
      return throwError(() => error as HttpErrorResponse);
    })
  );
};

const addTokenToRequest = (request: HttpRequest<any>, token: string): HttpRequest<any> => {
  const headers: Record<string, string> = {
    'Authorization': `Bearer ${token}`
  };

  if (!request.headers.has('Content-Type')) {
    headers['Content-Type'] = 'application/json';
  }

  return request.clone({ setHeaders: headers });
};

const handleTokenExpired = (
  request: HttpRequest<any>,
  next: HttpHandlerFn,
  authService: AuthService,
  logger: LoggerService
): Observable<HttpEvent<any>> => {
  //si ya hay un refresh en curso, encolar esta peticion para reintentarla después
  if (isRefreshing) {
    return new Observable((subscriber) => {
      pendingRequests.push({ request, next });
      refreshSubject!.subscribe({
        next: (refreshed) => {
          if (refreshed) {
            const newToken = authService.getAccessToken();
            const retried = addTokenToRequest(request, newToken!);
            next(retried).subscribe({
              next: (event) => subscriber.next(event),
              error: (err) => subscriber.error(err),
              complete: () => subscriber.complete()
            });
          } else {
            subscriber.error(new HttpErrorResponse({ error: 'No se pudo renovar el token', status: 401 }));
          }
        },
        error: (err) => subscriber.error(err)
      });
    });
  }

  isRefreshing = true;
  refreshSubject = new Subject<boolean>();

  return authService.refreshToken().pipe(
    switchMap(() => {
      isRefreshing = false;
      const newToken = authService.getAccessToken();

      const refreshed = !!newToken;
      refreshSubject!.next(refreshed);
      refreshSubject!.complete();
      refreshSubject = null;

      //surtir las peticiones encoladas
      flushPendingRequests(authService, logger);

      if (refreshed) {
        const newRequest = addTokenToRequest(request, newToken!);
        return next(newRequest);
      }
      return throwError(() => new HttpErrorResponse({ error: 'No se pudo renovar el token', status: 401 }));
    }),
    catchError((error) => {
      isRefreshing = false;
      refreshSubject?.next(false);
      refreshSubject?.complete();
      refreshSubject = null;
      pendingRequests.length = 0;
      logger.error('Error al renovar token, cerrando sesión');
      authService.forceLogoutLocal();
      return throwError(() => error as HttpErrorResponse);
    })
  );
};

const flushPendingRequests = (authService: AuthService, logger: LoggerService): void => {
  const newToken = authService.getAccessToken();
  while (pendingRequests.length > 0) {
    const pending = pendingRequests.shift()!;
    const retried = addTokenToRequest(pending.request, newToken!);
    pending.next(retried).subscribe({
      error: (err) => logger.warn('Reintento de petición encolada falló:', err.status)
    });
  }
};