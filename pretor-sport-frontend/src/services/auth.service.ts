import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { map, tap, catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { LoggerService } from './logger.service';
import { environment } from '../environments/environment';

export interface LoginRequest {
  email: string;
  password: string;
  rememberMe?: boolean;
}

export interface RegisterRequest {
  nombre: string;
  apellidos: string;
  email: string;
  password: string;
  direccion?: string;
  telefono?: string;
  rol?: 'ROLE_CLIENTE' | 'ROLE_EMPLEADO' | 'ROLE_ADMIN';
}

export interface AuthResponse {
  accessToken: string | null;
  refreshToken: string | null;
  tokenType: string;
  expiresIn: number;
  issuedAt: string;
  expiresAt: string;
  usuario: {
    id: number;
    nombre: string;
    apellidos: string;
    email: string;
    rol: string;
    nombreCompleto: string;
    iniciales: string;
    emailVerificado: boolean;
    ultimoAcceso: string;
  };
}

export interface User {
  id: number;
  nombre: string;
  apellidos: string;
  email: string;
  rol: string;
  nombreCompleto: string;
  iniciales: string;
  emailVerificado: boolean;
  direccion?: string;
  telefono?: string;
  fechaRegistro?: string;
  ultimoAcceso?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = `${environment.apiUrl}/auth`;
  private readonly ACCESS_TOKEN_KEY = 'access_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  private readonly USER_KEY = 'user_data';

  private currentUserSubject = new BehaviorSubject<User | null>(this.getUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasValidToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router,
    private logger: LoggerService
  ) {
    //verificar token al inicializar
    this.checkTokenValidity();
  }

  //iniciar sesion
  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, credentials)
      .pipe(
        tap(response => {
          if (!response.accessToken || !response.refreshToken) {
            throw new Error('La respuesta de login no contiene tokens válidos');
          }
          this.logger.debug('AuthService login: Guardando tokens y usuario');
          this.setTokens(response.accessToken, response.refreshToken);
          this.setUser(response.usuario);
          this.currentUserSubject.next(response.usuario);
          this.isAuthenticatedSubject.next(true);
          
          //verificar que todo se guardó correctamente
          this.logger.debug('AuthService login: Verificación post-login');
          this.logger.debug('AuthService login: Token guardado:', !!this.getAccessToken());
          this.logger.debug('AuthService login: Usuario guardado:', this.getCurrentUserSync());
          this.logger.debug('AuthService login: Estado autenticado:', this.isAuthenticatedSubject.value);
        }),
        catchError(this.handleError)
      );
  }

  //registrar usuario nuevo
  register(userData: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/register`, userData)
      .pipe(
        tap(response => {
          if (response.accessToken && response.refreshToken) {
            this.setTokens(response.accessToken, response.refreshToken);
            this.setUser(response.usuario);
            this.currentUserSubject.next(response.usuario);
            this.isAuthenticatedSubject.next(true);
          } else {
            this.forceLogoutLocal(false);
          }
        }),
        catchError(this.handleError)
      );
  }

  //cerrar sesion
  logout(): Observable<any> {
    return this.http.post(`${this.API_URL}/logout`, {})
      .pipe(
        tap(() => this.forceLogoutLocal()),
        catchError(() => {
          //incluso si falla la petición al servidor, limpiar localmente
          this.forceLogoutLocal();
          return throwError(() => 'Error al cerrar sesión');
        })
      );
  }

  //renovar el token de acceso
  refreshToken(): Observable<AuthResponse> {
    const refreshToken = this.getRefreshToken();
    
    if (!refreshToken) {
      return throwError(() => 'No refresh token available');
    }

    return this.http.post<AuthResponse>(`${this.API_URL}/refresh`, { refreshToken })
      .pipe(
        tap(response => {
          if (!response.accessToken || !response.refreshToken) {
            throw new Error('No se recibieron tokens válidos al renovar sesión');
          }
          this.setTokens(response.accessToken, response.refreshToken);
          this.setUser(response.usuario);
        }),
        catchError(error => {
          this.forceLogoutLocal();
          return throwError(() => error);
        })
      );
  }

  //obtener info del usuario actual
  getCurrentUser(): Observable<User> {
    return this.http.get<User>(`${this.API_URL}/me`)
      .pipe(
        tap(user => {
          this.setUser(user);
          this.currentUserSubject.next(user);
        }),
        catchError(this.handleError)
      );
  }

  //validar el token actual
  validateToken(): Observable<any> {
    return this.http.post(`${this.API_URL}/validate-token`, {})
      .pipe(
        map((response: any) => response.valid),
        catchError(() => {
          this.forceLogoutLocal(false);
          return throwError(() => 'Token inválido');
        })
      );
  }

  public isLoggedIn(): boolean {
    const isAuth = this.isAuthenticatedSubject.value;
    const hasToken = this.hasValidToken();
    const result = isAuth && hasToken;
    this.logger.debug('AuthService isLoggedIn:', { isAuth, hasToken, result });
    return result;
  }

  //verificar email con token
  verifyEmail(token: string): Observable<any> {
    return this.http.get(`${this.API_URL}/verify-email?token=${token}`)
      .pipe(catchError(this.handleError));
  }

  resendVerificationEmail(email: string): Observable<{ message: string; verificationUrl?: string }> {
    return this.http.post<{ message: string; verificationUrl?: string }>(
      `${this.API_URL}/resend-verification`,
      { email }
    ).pipe(catchError(this.handleError));
  }

  //metodos de utilidad para tokens
  getAccessToken(): string | null {
    return localStorage.getItem(this.ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  private setTokens(accessToken: string, refreshToken: string): void {
    localStorage.setItem(this.ACCESS_TOKEN_KEY, accessToken);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, refreshToken);
  }

  forceLogoutLocal(redirectToLogin = true): void {
    this.clearTokens();
    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
    if (redirectToLogin) {
      this.router.navigate(['/login']);
    }
  }

  private clearTokens(): void {
    localStorage.removeItem(this.ACCESS_TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
  }

  private setUser(user: any): void {
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  }

  private getUserFromStorage(): User | null {
    const userData = localStorage.getItem(this.USER_KEY);
    return userData ? JSON.parse(userData) : null;
  }

  private hasValidToken(): boolean {
    const token = this.getAccessToken();
    if (!token) return false;

    try {
      //verificar si el token no está expirado
      const tokenData = JSON.parse(atob(token.split('.')[1]));
      const expirationTime = tokenData.exp * 1000;
      return Date.now() < expirationTime;
    } catch {
      return false;
    }
  }

  private checkTokenValidity(): void {
    const hasToken = this.hasValidToken();
    if (!hasToken && this.getAccessToken()) {
      //token expirado, intentar renovar
      const refreshToken = this.getRefreshToken();
      if (refreshToken) {
        this.refreshToken().subscribe({
          next: () => {
            this.isAuthenticatedSubject.next(true);
          },
          error: () => {
            this.clearTokens();
            this.isAuthenticatedSubject.next(false);
          }
        });
      } else {
        this.clearTokens();
        this.isAuthenticatedSubject.next(false);
      }
    }
  }

  //metodos de utilidad para roles
  hasRole(role: string): boolean {
    const user = this.currentUserSubject.value || this.getUserFromStorage();
    this.logger.debug('AuthService hasRole:', { role, user, userRole: user?.rol });
    return user ? user.rol === role : false;
  }

  hasAnyRole(roles: string[]): boolean {
    const user = this.currentUserSubject.value || this.getUserFromStorage();
    return user ? roles.includes(user.rol) : false;
  }

  isAdmin(): boolean {
    const result = this.hasRole('ROLE_ADMIN');
    this.logger.debug('AuthService isAdmin:', result);
    return result;
  }

  isEmpleado(): boolean {
    return this.hasRole('ROLE_EMPLEADO');
  }

  isCliente(): boolean {
    return this.hasRole('ROLE_CLIENTE');
  }

  getCurrentUserSync(): User | null {
    return this.currentUserSubject.value || this.getUserFromStorage();
  }

  canManageProducts(): boolean {
    return this.hasAnyRole(['ROLE_ADMIN', 'ROLE_EMPLEADO']);
  }

  canViewReports(): boolean {
    return this.hasAnyRole(['ROLE_ADMIN', 'ROLE_EMPLEADO']);
  }

  redirectToUnauthorized(): void {
    this.logger.warn('AuthService: Acceso rechazado - redirigiendo a home');
    this.router.navigate(['/']);
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'Ha ocurrido un error inesperado';

    if (error.error instanceof ErrorEvent) {
      //error del lado del cliente
      errorMessage = error.error.message;
    } else {
      //error del servidor
      if (error.status === 401) {
        errorMessage = error.error?.message || 'Credenciales inválidas';
      } else if (error.status === 403) {
        errorMessage = 'No tienes permisos para realizar esta acción';
      } else if (error.status === 409) {
        errorMessage = 'El email ya está registrado';
      } else if (error.error?.message) {
        errorMessage = error.error.message;
      }
    }

    return throwError(() => errorMessage);
  }
}
