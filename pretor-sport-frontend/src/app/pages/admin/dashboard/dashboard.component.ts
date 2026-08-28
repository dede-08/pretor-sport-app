import { CommonModule } from '@angular/common';
import { Component, DestroyRef, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CategoriaSimple, Producto } from '../../../../models/producto.model';
import { ProductoService } from '../../../../services/producto.service';
import { ConfigService } from '../../../../services/config.service';
import { LoggerService } from '../../../../services/logger.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    RouterModule,
    CommonModule,

  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent {

  productos: Producto[] = [];
  categorias: CategoriaSimple[] = [];

  private destroyRef = inject(DestroyRef);

  constructor(
    private productoService: ProductoService,
    public configService: ConfigService,
    private logger: LoggerService
  ) { }

  ngOnInit(): void {
    this.cargarProductos();

  }

  cargarProductos(categoriaId?: number): void {
    const filtro = categoriaId ? { categoriaIds: [categoriaId] } : {};
    this.productoService.getProductos(filtro)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(response => {
        this.productos = response.content;
      });
  }

  updateProduct(product: Producto) {
    this.logger.debug('Update product:', product);
  }

  deleteProduct(id: number) {
    if (confirm('¿Estás seguro de que quieres eliminar este producto?')) {
      this.productoService.eliminarProducto(id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.cargarProductos();
          },
          error: (error) => {
            this.logger.error('Error deleting product:', error);
          }
        });
    }
  }

  obtenerImagenProducto(producto: Producto): string {
    return this.configService.getImageUrl(producto.imagenUrl);
  }
}


