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
  templateUrl: './notification.component.html',
  styleUrl: './notification.component.css'
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