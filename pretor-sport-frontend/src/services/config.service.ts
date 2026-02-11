import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ConfigService {
  //URL base del backend
  private readonly backendUrl = 'http://localhost:8080';
  
  //URL base para las imágenes
  private readonly imagesBaseUrl = 'http://localhost:8080/images';

  constructor() { }

  //obtiene la URL base del backend
  getBackendUrl(): string {
    return this.backendUrl;
  }

  
  //obtiene la URL base para las imagenes
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

    //si ya es una URL absoluta, la devuelve tal como está
    if (imagenUrl.startsWith('http://') || imagenUrl.startsWith('https://')) {
      return imagenUrl;
    }

    //si es una URL relativa que empieza con /images, construye la URL completa
    if (imagenUrl.startsWith('/images/')) {
      return `${this.backendUrl}${imagenUrl}`;
    }

    //si es solo el nombre del archivo, construye la URL completa
    if (!imagenUrl.startsWith('/')) {
      return `${this.imagesBaseUrl}/${imagenUrl}`;
    }

    //para cualquier otro caso, usa la URL del backend
    return `${this.backendUrl}${imagenUrl}`;
  }

  //maneja errores de carga de imagen y devuelve una imagen de respaldo
  handleImageError(event: any): void {
    event.target.src = this.getDefaultImageUrl();
  }

  //obtiene la URL de la imagen por defecto para usar como fallback
  getDefaultImageUrl(): string {
    return '/assets/pelota mikasa white.webp';
  }

  
  //verifica si una URL es valida para mostrar
  isValidImageUrl(url: string): boolean {
    return !!(url && url !== this.getDefaultImageUrl());
  }
}
