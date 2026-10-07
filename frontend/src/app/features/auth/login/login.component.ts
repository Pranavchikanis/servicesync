import { Component, inject } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  error: string | null = null;
  email = '';
  password = '';
  
  private authService = inject(AuthService);
  private router = inject(Router);

  onSubmit(event: Event) {
    event.preventDefault();
    this.error = null;
    
    if (!this.email || !this.password) {
      this.error = 'Email and password are required.';
      return;
    }

    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.error = 'Invalid credentials. Please try again.';
      }
    });
  }
}
