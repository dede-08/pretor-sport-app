import { Injectable } from '@angular/core';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ConfigService {
  private readonly backendUrl = environment.backendUrl;
  private readonly imagesBaseUrl = environment.imagesBaseUrl;

  constructor() { }

  getBackendUrl(): string {
    return this.backendUrl;
  }

  getImagesBaseUrl(): string {
    return this.imagesBaseUrl;
  }

  getImageUrl(imagenUrl?: string): string {
    if (!imagenUrl) {
      return this.getDefaultImageUrl();
    }

    if (imagenUrl.startsWith('http://') || imagenUrl.startsWith('https://')) {
      return imagenUrl;
    }

    if (imagenUrl.startsWith('/images/')) {
      return `${this.backendUrl}${imagenUrl}`;
    }

    if (!imagenUrl.startsWith('/')) {
      return `${this.imagesBaseUrl}/${imagenUrl}`;
    }

    return `${this.backendUrl}${imagenUrl}`;
  }

  handleImageError(event: Event): void {
    (event.target as HTMLImageElement).src = this.getDefaultImageUrl();
  }

  getDefaultImageUrl(): string {
    return '/assets/pelota mikasa white.webp';
  }

  isValidImageUrl(url: string): boolean {
    return !!(url && url !== this.getDefaultImageUrl());
  }
}
