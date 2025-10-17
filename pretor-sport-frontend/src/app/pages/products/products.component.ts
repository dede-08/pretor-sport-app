import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductoService } from '../../../services/producto.service';
import { CartService } from '../../../services/cart.service';
import { NotificationService } from '../../../services/notification.service';
import { Producto, CategoriaSimple } from '../../../models/producto.model';
import { RouterModule } from '@angular/router';
import { ConfigService } from '../../../services/config.service';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './products.component.html',
  styleUrls: ['./products.component.css']
})
export class ProductsComponent implements OnInit {
  productos: Producto[] = [];
  categorias: CategoriaSimple[] = [];
  selectedCategoriaId: number | null = null;
  selectedCategoriaNombre: string | null = null;

  constructor(
    private productoService: ProductoService,
    private cartService: CartService,
    private notificationService: NotificationService,
    public configService: ConfigService
  ) { }

  ngOnInit(): void {
    this.cargarProductos();
    this.cargarCategorias();
  }

  cargarProductos(categoriaId?: number): void {
    const filtro = categoriaId ? { categoriaIds: [categoriaId] } : {};
    this.productoService.getProductos(filtro).subscribe(response => {
      this.productos = response.content;
    });
  }

  cargarCategorias(): void {
    this.productoService.getProductos().subscribe(response => {
      const categoriasUnicas = new Map<number, CategoriaSimple>();
      response.content.forEach(producto => {
        if (producto.categoria) {
          categoriasUnicas.set(producto.categoria.id, producto.categoria);
        }
      });
      this.categorias = Array.from(categoriasUnicas.values());
    });
  }

  selectCategoria(categoriaId: number | null): void {
    this.selectedCategoriaId = categoriaId;
    if (categoriaId === null) {
      this.selectedCategoriaNombre = null;
    } else {
      const categoria = this.categorias.find(c => c.id === categoriaId);
      this.selectedCategoriaNombre = categoria ? categoria.nombre : null;
    }
    this.cargarProductos(categoriaId ?? undefined);
  }

  /**
   * Obtiene la URL de la imagen del producto
   */
  obtenerImagenProducto(producto: Producto): string {
    return this.configService.getImageUrl(producto.imagenUrl);
  }

  /**
   * Verifica si el producto tiene stock
   */
  tieneStock(producto: Producto): boolean {
    return producto.stock > 0;
  }

  /**
   * Agrega un producto al carrito
   */
  agregarAlCarrito(producto: Producto): void {
    if (!this.tieneStock(producto)) {
      this.notificationService.showWarning('Este producto no está disponible');
      return;
    }

    this.cartService.addToCartLocal(producto, 1);
    
    // Mostrar notificación de éxito
    this.notificationService.showSuccess(`"${producto.nombre}" agregado al carrito`);
  }
}