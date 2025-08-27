import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import 'bootstrap/dist/css/bootstrap.min.css';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterModule,
    
  ],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent {

}
