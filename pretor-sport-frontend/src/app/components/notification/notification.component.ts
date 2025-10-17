import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface NotificationData {
  message: string;
  type: 'success' | 'error' | 'warning' | 'info';
  duration?: number;
}

@Component({
  selector: 'app-notification',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="notification" [ngClass]="'notification-' + notification.type" *ngIf="show">
      <div class="notification-content">
        <i class="fas" [ngClass]="getIconClass()"></i>
        <span class="notification-message">{{ notification.message }}</span>
        <button class="btn-close" (click)="close()" aria-label="Cerrar">
          <i class="fas fa-times"></i>
        </button>
      </div>
    </div>
  `,
  styles: [`
    .notification {
      position: fixed;
      top: 20px;
      right: 20px;
      z-index: 9999;
      min-width: 300px;
      max-width: 500px;
      border-radius: 8px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      animation: slideInRight 0.3s ease-out;
    }

    .notification-content {
      display: flex;
      align-items: center;
      padding: 1rem;
      gap: 0.75rem;
    }

    .notification-message {
      flex: 1;
      font-weight: 500;
    }

    .btn-close {
      background: none;
      border: none;
      color: inherit;
      cursor: pointer;
      padding: 0.25rem;
      border-radius: 4px;
      transition: background-color 0.2s;
    }

    .btn-close:hover {
      background-color: rgba(0, 0, 0, 0.1);
    }

    .notification-success {
      background-color: #d4edda;
      border: 1px solid #c3e6cb;
      color: #155724;
    }

    .notification-error {
      background-color: #f8d7da;
      border: 1px solid #f5c6cb;
      color: #721c24;
    }

    .notification-warning {
      background-color: #fff3cd;
      border: 1px solid #ffeaa7;
      color: #856404;
    }

    .notification-info {
      background-color: #d1ecf1;
      border: 1px solid #bee5eb;
      color: #0c5460;
    }

    @keyframes slideInRight {
      from {
        transform: translateX(100%);
        opacity: 0;
      }
      to {
        transform: translateX(0);
        opacity: 1;
      }
    }

    @keyframes slideOutRight {
      from {
        transform: translateX(0);
        opacity: 1;
      }
      to {
        transform: translateX(100%);
        opacity: 0;
      }
    }

    .notification.closing {
      animation: slideOutRight 0.3s ease-in;
    }

    @media (max-width: 768px) {
      .notification {
        top: 10px;
        right: 10px;
        left: 10px;
        min-width: auto;
      }
    }
  `]
})
export class NotificationComponent implements OnInit, OnDestroy {
  @Input() notification!: NotificationData;
  
  show = true;
  private timeoutId?: number;

  ngOnInit(): void {
    const duration = this.notification.duration || 3000;
    this.timeoutId = window.setTimeout(() => {
      this.close();
    }, duration);
  }

  ngOnDestroy(): void {
    if (this.timeoutId) {
      clearTimeout(this.timeoutId);
    }
  }

  getIconClass(): string {
    switch (this.notification.type) {
      case 'success':
        return 'fa-check-circle';
      case 'error':
        return 'fa-exclamation-circle';
      case 'warning':
        return 'fa-exclamation-triangle';
      case 'info':
        return 'fa-info-circle';
      default:
        return 'fa-info-circle';
    }
  }

  close(): void {
    this.show = false;
    // Esperar a que termine la animación antes de destruir el componente
    setTimeout(() => {
      // El componente se destruirá automáticamente
    }, 300);
  }
}
