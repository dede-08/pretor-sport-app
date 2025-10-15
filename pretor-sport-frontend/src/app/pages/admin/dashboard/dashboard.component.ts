import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CategoriaSimple, Producto } from '../../../../models/producto.model';
import { ProductoService } from '../../../../services/producto.service';
import { ConfigService } from '../../../../services/config.service';

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

  constructor(
    private productoService: ProductoService,
    private configService: ConfigService
  ) { }

  ngOnInit(): void {
    this.cargarProductos();

  }

  cargarProductos(categoriaId?: number): void {
    const filtro = categoriaId ? { categoriaIds: [categoriaId] } : {};
    this.productoService.getProductos(filtro).subscribe(response => {
      this.productos = response.content;
    });
  }

  updateProduct(product: any) {
    // Implement navigation to update form or open modal
    console.log('Update product:', product);
  }

  deleteProduct(id: number) {
    if (confirm('Are you sure you want to delete this product?')) {
      this.productoService.deleteProduct(id).subscribe({
        next: () => {
          this.cargarProductos(); // Reload the list after deletion
        },
        error: (error) => {
          console.error('Error deleting product:', error);
        }
      });
    }
  }

  /**
   * Obtiene la URL de la imagen del producto
   */
  obtenerImagenProducto(producto: Producto): string {
    return this.configService.getImageUrl(producto.imagenUrl);
  }
}


