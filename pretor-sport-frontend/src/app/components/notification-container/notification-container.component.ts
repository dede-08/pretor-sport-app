import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../../services/notification.service';
import { NotificationComponent, NotificationData } from '../notification/notification.component';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-notification-container',
  standalone: true,
  imports: [CommonModule, NotificationComponent],
  templateUrl: './notification-container.component.html',
  styleUrl: './notification-container.component.css'
})

export class NotificationContainerComponent implements OnInit, OnDestroy {
  notifications: NotificationData[] = [];
  private destroy$ = new Subject<void>();

  constructor(private notificationService: NotificationService) {}

  ngOnInit(): void {
    this.notificationService.notifications$
      .pipe(takeUntil(this.destroy$))
      .subscribe(notifications => {
        this.notifications = notifications;
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onNotificationClose(notification: NotificationData): void {
    this.notificationService.removeNotification(notification);
  }

  trackByNotification(index: number, notification: NotificationData): string {
    return `${notification.type}-${notification.message}-${index}`;
  }
}
