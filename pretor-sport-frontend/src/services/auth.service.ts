import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { map, tap, catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

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
  accessToken: string;
  refreshToken: string;
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
  private readonly API_URL = 'http://localhost:8080/auth';
  private readonly ACCESS_TOKEN_KEY = 'access_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  private readonly USER_KEY = 'user_data';

  private currentUserSubject = new BehaviorSubject<User | null>(this.getUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasValidToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    //verificar token al inicializar
    this.checkTokenValidity();
  }

  //iniciar sesion
  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, credentials)
      .pipe(
        tap(response => {
          this.setTokens(response.accessToken, response.refreshToken);
          this.setUser(response.usuario);
          this.currentUserSubject.next(response.usuario);
          this.isAuthenticatedSubject.next(true);
        }),
        catchError(this.handleError)
      );
  }

  //registrar usuario nuevo
  register(userData: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/register`, userData)
      .pipe(
        tap(response => {
          this.setTokens(response.accessToken, response.refreshToken);
          this.setUser(response.usuario);
          this.currentUserSubject.next(response.usuario);
          this.isAuthenticatedSubject.next(true);
        }),
        catchError(this.handleError)
      );
  }

  //cerrar sesion
  logout(): Observable<any> {
    return this.http.post(`${this.API_URL}/logout`, {})
      .pipe(
        tap(() => {
          this.clearTokens();
          this.currentUserSubject.next(null);
          this.isAuthenticatedSubject.next(false);
          this.router.navigate(['/login']);
        }),
        catchError(() => {
          //incluso si falla la petición al servidor, limpiar localmente
          this.clearTokens();
          this.currentUserSubject.next(null);
          this.isAuthenticatedSubject.next(false);
          this.router.navigate(['/login']);
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
          this.setTokens(response.accessToken, response.refreshToken);
          this.setUser(response.usuario);
        }),
        catchError(error => {
          this.logout();
          return throwError(() => error);
        })
      );
  }

  //obtener información del usuario actual
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
          this.logout();
          return throwError(() => 'Token inválido');
        })
      );
  }

  public isLoggedIn(){
    let tokenStr = localStorage.getItem('token');
    if(tokenStr == undefined || tokenStr == '' || tokenStr == null){
      return false;
    }else{
      return true;
    }
  }

  //verificar email con token
  verifyEmail(token: string): Observable<any> {
    return this.http.get(`${this.API_URL}/verify-email?token=${token}`)
      .pipe(catchError(this.handleError));
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
    const user = this.currentUserSubject.value;
    return user ? user.rol === role : false;
  }

  hasAnyRole(roles: string[]): boolean {
    const user = this.currentUserSubject.value;
    return user ? roles.includes(user.rol) : false;
  }

  isAdmin(): boolean {
    return this.hasRole('ROLE_ADMIN');
  }

  isEmpleado(): boolean {
    return this.hasRole('ROLE_EMPLEADO');
  }

  isCliente(): boolean {
    return this.hasRole('ROLE_CLIENTE');
  }

  canManageProducts(): boolean {
    return this.hasAnyRole(['ROLE_ADMIN', 'ROLE_EMPLEADO']);
  }

  canViewReports(): boolean {
    return this.hasAnyRole(['ROLE_ADMIN', 'ROLE_EMPLEADO']);
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'Ha ocurrido un error inesperado';

    if (error.error instanceof ErrorEvent) {
      // Error del lado del cliente
      errorMessage = error.error.message;
    } else {
      // Error del servidor
      if (error.status === 401) {
        errorMessage = 'Credenciales inválidas';
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
