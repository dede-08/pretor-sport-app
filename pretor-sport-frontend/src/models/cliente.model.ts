export interface Cliente {
  id?: number;
  nombre: string;
  apellidos: string;
  email: string;
  direccion?: string;
  telefono?: string;
  fechaRegistro?: Date;
  rol: 'ROLE_CLIENTE' | 'ROLE_EMPLEADO' | 'ROLE_ADMIN';
}

export interface ClienteRequest {
  nombre: string;
  apellidos: string;
  email: string;
  password: string;
  direccion?: string;
  telefono?: string;
  rol?: 'ROLE_CLIENTE' | 'ROLE_EMPLEADO' | 'ROLE_ADMIN';
}

export interface ClienteResponse {
  id: number;
  nombre: string;
  apellidos: string;
  email: string;
  direccion?: string;
  telefono?: string;
  fechaRegistro: Date;
  rol: string;
  nombreCompleto?: string;
  iniciales?: string;
}
