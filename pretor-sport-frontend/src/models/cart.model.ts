export interface CartItem {
  id: string; 
  producto: {
    id?: number;
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
  envio: number;
  total: number;
  fechaCreacion: Date;
  fechaActualizacion: Date;
}

export interface CartSummary {
  totalItems: number;
  subtotal: number;
  envio: number;
  total: number;
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
  notas?: string;
}
