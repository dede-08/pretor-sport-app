import { Component, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
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
  private destroyRef = inject(DestroyRef);

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
        tipo: ['TARJETA', [Validators.required]]
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

  onEnvioChange(tipo: string, costo: number) {
    this.checkoutForm.get('metodoEnvio')?.patchValue({ tipo, costo });
  }

  onSubmit() {
    if (this.checkoutForm.invalid) {
      this.checkoutForm.markAllAsTouched();
      return;
    }

    this.isProcessing = true;

    const formValue = this.checkoutForm.value;
    const request = {
      direccionEnvio: formValue.direccionEnvio,
      metodoPago: { tipo: formValue.metodoPago.tipo },
      metodoEnvio: formValue.metodoEnvio,
      notas: formValue.notas
    };

    this.cartService.checkout(request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
      next: () => {
        this.isProcessing = false;
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.isProcessing = false;
      }
    });
  }
}
