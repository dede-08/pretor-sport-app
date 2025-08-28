export interface Producto {
  id?: number;
  nombre: string;
  descripcion?: string;
  precio: number;
  stock: number;
  imagenUrl?: string;
  marca?: string;
  modelo?: string;
  talla?: string;
  color?: string;
  genero?: 'HOMBRE' | 'MUJER' | 'NIÑO' | 'NIÑA' | 'UNISEX';
  material?: string;
  peso?: number;
  caracteristicas?: string[];
  activo?: boolean;
  fechaCreacion?: Date;
  fechaActualizacion?: Date;
  categoria?: CategoriaSimple;
  proveedor?: ProveedorSimple;
  
  // Campos calculados
  disponible?: boolean;
  estadoStock?: 'SIN_STOCK' | 'STOCK_BAJO' | 'STOCK_MEDIO' | 'STOCK_ALTO';
  precioConDescuento?: number;
  descuentoPorcentaje?: number;
}

export interface CategoriaSimple {
  id: number;
  nombre: string;
  tipo?: string;
  iconoUrl?: string;
}

export interface ProveedorSimple {
  id: number;
  nombre: string;
  email?: string;
}

export interface ProductoRequest {
  nombre: string;
  descripcion?: string;
  precio: number;
  stock: number;
  imagenUrl?: string;
  categoriaId: number;
  proveedorId?: number;
  marca?: string;
  modelo?: string;
  talla?: string;
  color?: string;
  genero?: 'HOMBRE' | 'MUJER' | 'NIÑO' | 'NIÑA' | 'UNISEX';
  material?: string;
  peso?: number;
  caracteristicas?: string[];
}

export interface ProductoFilter {
  busqueda?: string;
  categoriaIds?: number[];
  marca?: string;
  precioMin?: number;
  precioMax?: number;
  tallas?: string[];
  colores?: string[];
  genero?: 'HOMBRE' | 'MUJER' | 'NIÑO' | 'NIÑA' | 'UNISEX';
  materiales?: string[];
  soloDisponibles?: boolean;
  pesoMin?: number;
  pesoMax?: number;
  ordenarPor?: 'nombre' | 'precio' | 'fechaCreacion' | 'popularidad' | 'descuento';
  direccion?: 'asc' | 'desc';
  pagina?: number;
  tamanoPagina?: number;
}
