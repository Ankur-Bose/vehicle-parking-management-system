import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
})
export class LoginComponent implements OnInit {
  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  email = '';
  password = '';
  loading = signal(false);
  error = signal('');
  showPassword = signal(false);
  registered = signal(false);

  ngOnInit() {
    this.route.queryParams.subscribe((p) => {
      if (p['registered'] === 'true') this.registered.set(true);
    });
  }

  private parseError(err: any, fallback: string): string {
    if (err.status === 0)
      return 'Cannot connect to server. Please make sure the backend is running.';
    if (typeof err.error === 'string') return err.error;
    if (err.error?.message) return err.error.message;
    if (err.message) return err.message;
    return fallback;
  }

  login() {
    if (!this.email || !this.password) {
      this.error.set('Please enter email and password.');
      return;
    }
    this.loading.set(true);
    this.error.set('');

    this.auth.login({ email: this.email, password: this.password }).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(this.parseError(err, 'Invalid credentials. Please try again.'));
      },
    });
  }
}
