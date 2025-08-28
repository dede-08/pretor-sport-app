import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { 
  Producto, 
  ProductoRequest, 
  ProductoFilter 
} from '../models/producto.model';

export interface ApiResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ProductoService {
  private readonly baseUrl = 'http://localhost:8080/api/productos';
  private productosSubject = new BehaviorSubject<Producto[]>([]);
  public productos$ = this.productosSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Obtener todos los productos con filtros y paginación
   */
  getProductos(filtros?: ProductoFilter): Observable<ApiResponse<Producto>> {
    let params = new HttpParams();

    if (filtros) {
      Object.keys(filtros).forEach(key => {
        const value = (filtros as any)[key];
        if (value !== undefined && value !== null) {
          if (Array.isArray(value)) {
            value.forEach(item => {
              params = params.append(key, item.toString());
            });
          } else {
            params = params.set(key, value.toString());
          }
        }
      });
    }

    return this.http.get<ApiResponse<Producto>>(this.baseUrl, { params })
      .pipe(
        tap(response => {
          this.productosSubject.next(response.content);
        })
      );
  }

  /**
   * Obtener un producto por ID
   */
  getProductoPorId(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.baseUrl}/${id}`);
  }

  /**
   * Crear un nuevo producto
   */
  crearProducto(producto: ProductoRequest): Observable<Producto> {
    return this.http.post<Producto>(this.baseUrl, producto)
      .pipe(
        tap(() => {
          // Recargar la lista de productos
          this.refrescarProductos();
        })
      );
  }

  /**
   * Actualizar un producto existente
   */
  actualizarProducto(id: number, producto: ProductoRequest): Observable<Producto> {
    return this.http.put<Producto>(`${this.baseUrl}/${id}`, producto)
      .pipe(
        tap(() => {
          this.refrescarProductos();
        })
      );
  }

  /**
   * Eliminar un producto
   */
  eliminarProducto(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`)
      .pipe(
        tap(() => {
          this.refrescarProductos();
        })
      );
  }

  /**
   * Buscar productos por término
   */
  buscarProductos(termino: string, filtros?: Partial<ProductoFilter>): Observable<ApiResponse<Producto>> {
    const filtrosBusqueda: ProductoFilter = {
      busqueda: termino,
      ...filtros
    };
    return this.getProductos(filtrosBusqueda);
  }

  /**
   * Obtener productos por categoría
   */
  getProductosPorCategoria(categoriaId: number, filtros?: Partial<ProductoFilter>): Observable<ApiResponse<Producto>> {
    const filtrosCategoria: ProductoFilter = {
      categoriaIds: [categoriaId],
      ...filtros
    };
    return this.getProductos(filtrosCategoria);
  }

  /**
   * Obtener productos populares
   */
  getProductosPopulares(limite: number = 12): Observable<Producto[]> {
    const filtros: ProductoFilter = {
      ordenarPor: 'popularidad',
      direccion: 'desc',
      tamanoPagina: limite,
      pagina: 0
    };
    
    return this.getProductos(filtros).pipe(
      map(response => response.content)
    );
  }

  /**
   * Obtener productos en oferta
   */
  getProductosEnOferta(limite: number = 12): Observable<Producto[]> {
    const filtros: ProductoFilter = {
      ordenarPor: 'descuento',
      direccion: 'desc',
      tamanoPagina: limite,
      pagina: 0
    };
    
    return this.getProductos(filtros).pipe(
      map(response => response.content.filter(p => p.descuentoPorcentaje && p.descuentoPorcentaje > 0))
    );
  }

  /**
   * Actualizar stock de un producto
   */
  actualizarStock(id: number, nuevoStock: number): Observable<Producto> {
    return this.http.patch<Producto>(`${this.baseUrl}/${id}/stock`, { stock: nuevoStock })
      .pipe(
        tap(() => {
          this.refrescarProductos();
        })
      );
  }

  /**
   * Activar/Desactivar producto
   */
  toggleActivoProducto(id: number): Observable<Producto> {
    return this.http.patch<Producto>(`${this.baseUrl}/${id}/toggle-activo`, {})
      .pipe(
        tap(() => {
          this.refrescarProductos();
        })
      );
  }

  /**
   * Obtener estadísticas de productos
   */
  getEstadisticasProductos(): Observable<any> {
    return this.http.get(`${this.baseUrl}/estadisticas`);
  }

  /**
   * Refrescar la lista de productos
   */
  private refrescarProductos(): void {
    this.getProductos().subscribe();
  }

  /**
   * Limpiar caché de productos
   */
  limpiarCache(): void {
    this.productosSubject.next([]);
  }
}
