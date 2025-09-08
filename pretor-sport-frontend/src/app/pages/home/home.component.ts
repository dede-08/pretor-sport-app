import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ProductoService } from '../../../services/producto.service';
import { Producto } from '../../../models/producto.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterModule, CommonModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  productos: Producto[] = [];
  productosDestacados: Producto[] = [];
  productosOferta: Producto[] = [];
  loading = false;
  error = false;
  errorMessage = '';

  constructor(private productoService: ProductoService) {}

  ngOnInit() {
    this.cargarProductos();
  }

  cargarProductos() {
    this.loading = true;
    this.error = false;
    this.errorMessage = '';

    // Cargar productos generales
    this.productoService.getProductos({ tamanoPagina: 12, pagina: 0 }).subscribe({
      next: (response) => {
        this.productos = response.content;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error al cargar productos:', error);
        this.error = true;
        this.errorMessage = 'Error al cargar los productos. Por favor, inténtalo de nuevo.';
        this.loading = false;
      }
    });

    // Cargar productos destacados
    this.productoService.getProductosPopulares(8).subscribe({
      next: (productos) => {
        this.productosDestacados = productos;
      },
      error: (error) => {
        console.error('Error al cargar productos destacados:', error);
      }
    });

    // Cargar productos en oferta
    this.productoService.getProductosEnOferta(8).subscribe({
      next: (productos) => {
        this.productosOferta = productos;
      },
      error: (error) => {
        console.error('Error al cargar productos en oferta:', error);
      }
    });
  }

  // Método para formatear precio
  formatearPrecio(precio: number): string {
    return new Intl.NumberFormat('es-MX', {
      style: 'currency',
      currency: 'MXN'
    }).format(precio);
  }

  // Método para calcular precio con descuento
  calcularPrecioConDescuento(precio: number, descuentoPorcentaje?: number): number {
    if (!descuentoPorcentaje || descuentoPorcentaje <= 0) {
      return precio;
    }
    return precio * (1 - descuentoPorcentaje / 100);
  }

  // Método para obtener imagen por defecto
  obtenerImagenProducto(producto: Producto): string {
    return producto.imagenUrl || 'https://via.placeholder.com/300x300?text=Sin+Imagen';
  }

  // Método para verificar si hay stock
  tieneStock(producto: Producto): boolean {
    return producto.stock > 0;
  }

  // Método para obtener el estado del stock
  obtenerEstadoStock(producto: Producto): string {
    if (producto.stock === 0) return 'Agotado';
    if (producto.stock <= 5) return 'Últimas unidades';
    if (producto.stock <= 10) return 'Stock bajo';
    return 'Disponible';
  }

  // Método para obtener la clase CSS del estado del stock
  obtenerClaseEstadoStock(producto: Producto): string {
    if (producto.stock === 0) return 'badge-danger';
    if (producto.stock <= 5) return 'badge-warning';
    if (producto.stock <= 10) return 'badge-info';
    return 'badge-success';
  }
}
