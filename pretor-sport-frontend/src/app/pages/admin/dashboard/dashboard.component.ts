import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CategoriaSimple, Producto } from '../../../../models/producto.model';
import { ProductoService } from '../../../../services/producto.service';

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

  constructor(private productoService: ProductoService) {}

  ngOnInit(): void{
    this.cargarProductos();
    this.cargarCategorias();
  }

  cargarProductos(categoriaId?: number): void{
    const filtro = categoriaId ? {categoriaIds:[categoriaId]} : {};
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
}


