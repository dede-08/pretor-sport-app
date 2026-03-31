import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';
import { catchError, tap } from 'rxjs/operators';
import { Observable, of } from 'rxjs';

export type LogLevel = 'DEBUG' | 'INFO' | 'WARN' | 'ERROR';

export interface LoggerPayload {
  level: LogLevel;
  message: string;
  context?: string;
  data?: unknown;
  timestamp?: string;
}

@Injectable({
  providedIn: 'root'
})
export class LoggerService {
  private readonly endpoint = `${environment.apiUrl}/logs`;
  private readonly enabled = !environment.production;

  constructor(private http: HttpClient) { }

  debug(message: string, data?: unknown, context?: string): void {
    this.log('DEBUG', message, data, context);
  }

  info(message: string, data?: unknown, context?: string): void {
    this.log('INFO', message, data, context);
  }

  warn(message: string, data?: unknown, context?: string): void {
    this.log('WARN', message, data, context);
  }

  error(message: string, data?: unknown, context?: string): void {
    this.log('ERROR', message, data, context);
  }

  private log(level: LogLevel, message: string, data?: unknown, context?: string): void {
    const payload: LoggerPayload = {
      level,
      message,
      context,
      data,
      timestamp: new Date().toISOString()
    };

    // En modo desarrollo siempre mostramos en consola
    if (this.enabled) {
      const logger = console[level.toLowerCase() as 'log' | 'warn' | 'error' | 'info'] ?? console.log;
      logger(`[${level}]${context ? ` [${context}]` : ''} ${message}`, data ?? '');
    }

    // En producción intentamos enviar al backend (si está disponible)
    if (!environment.production) {
      return;
    }

    this.http.post(this.endpoint, payload)
      .pipe(
        tap(() => { /* no-op */ }),
        catchError(() => of(null))
      )
      .subscribe();
  }
}
