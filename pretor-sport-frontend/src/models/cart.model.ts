export interface CartItem {
  id: string; // ID único del item en el carrito
  producto: {
    id: number;
    nombre: string;
    precio: number;
    imagenUrl?: string;
    stock: number;
    categoria?: {
      id: number;
      nombre: string;
    };
  };
  cantidad: number;
  precioUnitario: number;
  precioTotal: number;
  fechaAgregado: Date;
  variantes?: {
    talla?: string;
    color?: string;
    genero?: string;
  };
}

export interface Cart {
  id?: string;
  items: CartItem[];
  subtotal: number;
  descuento: number;
  envio: number;
  total: number;
  fechaCreacion: Date;
  fechaActualizacion: Date;
  cuponDescuento?: {
    codigo: string;
    descuento: number;
    tipo: 'PORCENTAJE' | 'MONTO_FIJO';
  };
}

export interface CartSummary {
  totalItems: number;
  subtotal: number;
  descuento: number;
  envio: number;
  total: number;
}

export interface AddToCartRequest {
  productoId: number;
  cantidad: number;
  variantes?: {
    talla?: string;
    color?: string;
    genero?: string;
  };
}

export interface UpdateCartItemRequest {
  itemId: string;
  cantidad: number;
}

export interface CartCheckoutRequest {
  direccionEnvio: {
    nombre: string;
    apellidos: string;
    direccion: string;
    ciudad: string;
    codigoPostal: string;
    telefono: string;
    email: string;
  };
  metodoPago: {
    tipo: 'TARJETA' | 'PAYPAL' | 'TRANSFERENCIA';
    datos?: any;
  };
  metodoEnvio: {
    tipo: 'ESTANDAR' | 'EXPRESS' | 'PREMIUM';
    costo: number;
  };
  cuponDescuento?: string;
  notas?: string;
}
