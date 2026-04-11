import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CartService } from '../../../services/cart.service';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.css']
})

export class CheckoutComponent {
  private fb = inject(FormBuilder);
  public cartService = inject(CartService);
  private router = inject(Router);

  checkoutForm: FormGroup;
  isProcessing = false;
  cartSummary = this.cartService.cartSummary;

  constructor() {
    this.checkoutForm = this.fb.group({
      direccionEnvio: this.fb.group({
        nombre: ['', [Validators.required, Validators.minLength(2)]],
        apellidos: ['', [Validators.required]],
        email: ['', [Validators.required, Validators.email]],
        telefono: ['', [Validators.required, Validators.pattern('^[0-9+ ]+$')]],
        direccion: ['', [Validators.required]],
        ciudad: ['', [Validators.required]],
        codigoPostal: ['', [Validators.required]]
      }),
      metodoPago: this.fb.group({
        tipo: ['TARJETA', [Validators.required]],
        numeroTarjeta: ['', [Validators.required, Validators.minLength(16), Validators.maxLength(19)]],
        fechaExpiracion: ['', [Validators.required, Validators.pattern('^(0[1-9]|1[0-2])\/([0-9]{2})$')]],
        cvv: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(4)]]
      }),
      metodoEnvio: this.fb.group({
        tipo: ['ESTANDAR', [Validators.required]],
        costo: [50]
      }),
      notas: ['']
    });
  }

  get direccionForm() {
    return this.checkoutForm.get('direccionEnvio') as FormGroup;
  }

  get pagoForm() {
    return this.checkoutForm.get('metodoPago') as FormGroup;
  }

  onEnvioChange(tipo: string, costo: number) {
    this.checkoutForm.get('metodoEnvio')?.patchValue({ tipo, costo });
  }

  onSubmit() {
    if (this.checkoutForm.invalid) {
      this.checkoutForm.markAllAsTouched();
      return;
    }

    this.isProcessing = true;

    //preparar el request omitiendo datos sensibles de la tarjeta (en un flujo real estos se tokenizan)
    const formValue = this.checkoutForm.value;
    const request = {
      direccionEnvio: formValue.direccionEnvio,
      metodoPago: { tipo: formValue.metodoPago.tipo },
      metodoEnvio: formValue.metodoEnvio,
      notas: formValue.notas
    };

    this.cartService.checkout(request).subscribe({
      next: () => {
        this.isProcessing = false;
        //navegar a los pedidos temporalmente como confirmación
        this.router.navigate(['/pedidos']);
      },
      error: (err) => {
        this.isProcessing = false;
        console.error('Error al procesar el checkout', err);
      }
    });
  }
}
