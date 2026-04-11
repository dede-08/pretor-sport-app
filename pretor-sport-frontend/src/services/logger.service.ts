import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';
import { catchError, filter, bufferTime } from 'rxjs/operators';
import { Observable, of, Subject } from 'rxjs';

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

  private logSubject = new Subject<LoggerPayload>();

  constructor(private http: HttpClient) {
    //en produccion procesamos logs en lotes para optimizar red
    if (environment.production) {
      this.logSubject.pipe(
        bufferTime(5000),
        filter(logs => logs.length > 0)
      ).subscribe(logs => {
        this.http.post(this.endpoint, logs)
          .pipe(catchError(() => of(null)))
          .subscribe();
      });
    }
  }

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

    //en modo desarrollo siempre mostramos en consola
    if (this.enabled) {
      const logger = console[level.toLowerCase() as 'log' | 'warn' | 'error' | 'info'] ?? console.log;
      logger(`[${level}]${context ? ` [${context}]` : ''} ${message}`, data ?? '');
    }

    //en produccion derivamos al subject para batching
    if (environment.production) {
      this.logSubject.next(payload);
    }
  }
}
