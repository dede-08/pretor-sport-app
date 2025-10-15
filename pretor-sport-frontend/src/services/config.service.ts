import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ConfigService {
  // URL base del backend
  private readonly backendUrl = 'http://localhost:8080';
  
  // URL base para las imágenes
  private readonly imagesBaseUrl = `${this.backendUrl}/images`;

  constructor() { }

  /**
   * Obtiene la URL base del backend
   */
  getBackendUrl(): string {
    return this.backendUrl;
  }

  /**
   * Obtiene la URL base para las imágenes
   */
  getImagesBaseUrl(): string {
    return this.imagesBaseUrl;
  }

  /**
   * Construye la URL completa de una imagen
   * @param imagenUrl URL de la imagen (puede ser relativa o absoluta)
   * @returns URL completa de la imagen
   */
  getImageUrl(imagenUrl?: string): string {
    if (!imagenUrl) {
      return this.getDefaultImageUrl();
    }

    // Si ya es una URL absoluta, la devuelve tal como está
    if (imagenUrl.startsWith('http://') || imagenUrl.startsWith('https://')) {
      return imagenUrl;
    }

    // Si es una URL relativa que empieza con /images, construye la URL completa
    if (imagenUrl.startsWith('/images/')) {
      return `${this.backendUrl}${imagenUrl}`;
    }

    // Si es solo el nombre del archivo, construye la URL completa
    if (!imagenUrl.startsWith('/')) {
      return `${this.imagesBaseUrl}/${imagenUrl}`;
    }

    // Para cualquier otro caso, usa la URL del backend
    return `${this.backendUrl}${imagenUrl}`;
  }

  /**
   * Obtiene la URL de la imagen por defecto
   */
  getDefaultImageUrl(): string {
    return 'https://via.placeholder.com/300x300?text=Sin+Imagen';
  }

  /**
   * Verifica si una URL es válida para mostrar
   */
  isValidImageUrl(url: string): boolean {
    return !!(url && url !== this.getDefaultImageUrl());
  }
}
