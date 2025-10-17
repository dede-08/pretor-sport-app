import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { NotificationData } from '../app/components/notification/notification.component';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notificationsSubject = new BehaviorSubject<NotificationData[]>([]);
  public notifications$ = this.notificationsSubject.asObservable();

  constructor() {}

  showSuccess(message: string, duration: number = 3000): void {
    this.addNotification({
      message,
      type: 'success',
      duration
    });
  }

  showError(message: string, duration: number = 5000): void {
    this.addNotification({
      message,
      type: 'error',
      duration
    });
  }

  showWarning(message: string, duration: number = 4000): void {
    this.addNotification({
      message,
      type: 'warning',
      duration
    });
  }

  showInfo(message: string, duration: number = 3000): void {
    this.addNotification({
      message,
      type: 'info',
      duration
    });
  }

  private addNotification(notification: NotificationData): void {
    const currentNotifications = this.notificationsSubject.value;
    const newNotifications = [...currentNotifications, notification];
    this.notificationsSubject.next(newNotifications);

    // Auto-remove notification after duration
    setTimeout(() => {
      this.removeNotification(notification);
    }, notification.duration || 3000);
  }

  removeNotification(notification: NotificationData): void {
    const currentNotifications = this.notificationsSubject.value;
    const filteredNotifications = currentNotifications.filter(n => n !== notification);
    this.notificationsSubject.next(filteredNotifications);
  }

  clearAll(): void {
    this.notificationsSubject.next([]);
  }
}
