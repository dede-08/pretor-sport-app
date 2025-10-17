import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../../services/notification.service';
import { NotificationComponent, NotificationData } from '../notification/notification.component';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-notification-container',
  standalone: true,
  imports: [CommonModule, NotificationComponent],
  template: `
    <div class="notification-container">
      <app-notification 
        *ngFor="let notification of notifications; trackBy: trackByNotification"
        [notification]="notification"
        (close)="onNotificationClose(notification)">
      </app-notification>
    </div>
  `,
  styles: [`
    .notification-container {
      position: fixed;
      top: 20px;
      right: 20px;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      pointer-events: none;
    }

    .notification-container > * {
      pointer-events: auto;
    }

    @media (max-width: 768px) {
      .notification-container {
        top: 10px;
        right: 10px;
        left: 10px;
      }
    }
  `]
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
