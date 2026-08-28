import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ProductoService } from '../../../services/producto.service';
import { CartService } from '../../../services/cart.service';
import { NotificationService } from '../../../services/notification.service';
import { Producto, CategoriaSimple } from '../../../models/producto.model';
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
  searchTerm: string | null = null;

  private destroyRef = inject(DestroyRef);

  constructor(
    private productoService: ProductoService,
    private cartService: CartService,
    private notificationService: NotificationService,
    public configService: ConfigService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.route.queryParams
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(params => {
        const termino = params['q'];
        if (termino) {
          this.searchTerm = termino;
          this.buscarProductos(termino);
        } else {
          this.searchTerm = null;
          this.cargarProductos();
        }
      });

    this.cargarCategorias();
  }

  cargarProductos(categoriaId?: number): void {
    const filtro = categoriaId ? { categoriaIds: [categoriaId] } : {};
    this.productoService.getProductos(filtro)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(response => {
        this.productos = response.content;
      });
  }

  cargarCategorias(): void {
    this.productoService.getProductos()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(response => {
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
    this.searchTerm = null;
    this.router.navigate([], { queryParams: { q: null }, queryParamsHandling: 'merge' });

    this.selectedCategoriaId = categoriaId;
    if (categoriaId === null) {
      this.selectedCategoriaNombre = null;
    } else {
      const categoria = this.categorias.find(c => c.id === categoriaId);
      this.selectedCategoriaNombre = categoria ? categoria.nombre : null;
    }
    this.cargarProductos(categoriaId ?? undefined);
  }

  private buscarProductos(termino: string): void {
    this.productoService.buscarProductos(termino)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(response => {
        this.productos = response.content;
        this.selectedCategoriaId = null;
        this.selectedCategoriaNombre = null;
        this.searchTerm = termino;
      });
  }

  //obtiene la UTL de la imagen del producto
  obtenerImagenProducto(producto: Producto): string {
    return this.configService.getImageUrl(producto.imagenUrl);
  }

  //verifica si el producto tiene stock disponible
  tieneStock(producto: Producto): boolean {
    return producto.stock > 0;
  }

  
  //agrega un producto al carrito
  agregarAlCarrito(producto: Producto): void {
    if (!this.tieneStock(producto)) {
      this.notificationService.showWarning('Este producto no está disponible');
      return;
    }

    this.cartService.addToCartLocal(producto, 1);
    
    //mostrar notificación de exito al agregar al carrito
    this.notificationService.showSuccess(`"${producto.nombre}" agregado al carrito`);
  }
}