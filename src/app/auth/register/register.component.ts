import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { UserService } from '../../core/services/user.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css'],
})
export class RegisterComponent {
  private userSvc = inject(UserService);
  private router = inject(Router);

  loading = signal(false);
  error = signal('');
  showPassword = signal(false);
  showConfirm = signal(false);

  form = {
    name: '',
    email: '',
    phoneNumber: '',
    password: '',
    confirmPassword: '',
  };

  get passwordsMatch() {
    return (
      this.form.password &&
      this.form.confirmPassword &&
      this.form.password === this.form.confirmPassword
    );
  }

  get passwordsMismatch() {
    return (
      this.form.password &&
      this.form.confirmPassword &&
      this.form.password !== this.form.confirmPassword
    );
  }

  private parseError(err: any, fallback: string): string {
    if (err.status === 0)
      return 'Cannot connect to server. Please make sure the backend is running.';
    if (typeof err.error === 'string') return err.error;
    if (err.error?.message) return err.error.message;
    if (err.message) return err.message;
    return fallback;
  }

  register() {
    this.error.set('');
    if (!this.form.name.trim()) {
      this.error.set('Full name is required.');
      return;
    }
    if (!this.form.email.trim()) {
      this.error.set('Email is required.');
      return;
    }
    if (!/^\d{10}$/.test(this.form.phoneNumber)) {
      this.error.set('Phone number must be exactly 10 digits.');
      return;
    }
    if (this.form.password.length < 6) {
      this.error.set('Password must be at least 6 characters.');
      return;
    }
    if (this.form.password !== this.form.confirmPassword) {
      this.error.set('Passwords do not match.');
      return;
    }

    this.loading.set(true);
    this.userSvc
      .register({
        name: this.form.name.trim(),
        email: this.form.email.trim(),
        phoneNumber: this.form.phoneNumber,
        password: this.form.password,
        role: 'CUSTOMER',
      })
      .subscribe({
        next: () => this.router.navigate(['/login'], { queryParams: { registered: 'true' } }),
        error: (err) => {
          this.loading.set(false);
          this.error.set(this.parseError(err, 'Registration failed. Please try again.'));
        },
      });
  }
}
