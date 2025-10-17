import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { SignupComponent } from './pages/signup/signup.component';
import { LoginComponent } from './pages/login/login.component';
import { ProductsComponent } from './pages/products/products.component';
import { CategorysComponent } from './pages/categorys/categorys.component';
import { ContactComponent } from './pages/contact/contact.component';
import { DashboardComponent } from './pages/admin/dashboard/dashboard.component';
import { CartComponent } from './pages/cart/cart.component';
import { adminGuard } from '../guards/admin.guard';

export const routes: Routes = [
    {
        path : '',
        component : HomeComponent,
        pathMatch : 'full'
    },
    {
        path : 'signup',
        component : SignupComponent,
        pathMatch : 'full'
    },
    {
        path : 'login',
        component : LoginComponent,
        pathMatch : 'full'
    },
    {
        path : 'productos',
        component : ProductsComponent,
        pathMatch : 'full'
    },
    {
        path : 'categorias',
        component : CategorysComponent,
        pathMatch : 'full'
    },
    {
        path : 'contacto',
        component : ContactComponent,
        pathMatch : 'full'
    },
    {
        path : 'carrito',
        component : CartComponent,
        pathMatch : 'full'
    },
    {
        path : 'admin',
        component : DashboardComponent,
        canActivate : [adminGuard],
        /*children : [
            {
                path : 'profile',
                component : ProfileComponent
            },
            {
                path : '',
                component : WelcomeComponent
            }
        ]*/
    },
];
