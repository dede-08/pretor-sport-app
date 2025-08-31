import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

interface Product {
  nombre: string;
  descripcion: string;
  precio: number;
  categoria: string;
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterModule,
    CommonModule
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  products: Product[] = [];
  loading = false;
  error = false;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loading = true;
    this.error = false;
    this.http.get<Product[]>('/api/productos')
      .subscribe({
        next: (data) => {
          this.products = data;
          this.loading = false;
        },
        error: () => {
          this.error = true;
          this.loading = false;
        }
      });
  }
}
