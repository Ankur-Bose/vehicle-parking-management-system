import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../core/services/user.service';
import { AuthService } from '../../core/services/auth.service';
import { UserResponse } from '../../models/user.models';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  private userSvc = inject(UserService);
  auth = inject(AuthService);

  profile = signal<UserResponse | null>(null);
  loading = signal(true);
  saving = signal(false);
  error = signal('');
  success = signal('');
  editMode = signal(false);

  form = { name: '', phoneNumber: '', password: '' };

  ngOnInit() { this.load(); }

  load() {
    this.loading.set(true);
    const userId = this.auth.getUserId();
    if (userId) {
      this.userSvc.getById(userId).subscribe({
        next: data => this.applyProfile(data),
        error: () => this.userSvc.getMe().subscribe({
          next: data => this.applyProfile(data),
          error: () => { this.error.set('Failed to load profile.'); this.loading.set(false); }
        })
      });
    } else {
      this.userSvc.getMe().subscribe({
        next: data => this.applyProfile(data),
        error: () => { this.error.set('Failed to load profile.'); this.loading.set(false); }
      });
    }
  }

  private applyProfile(data: UserResponse) {
    this.profile.set(data);
    this.auth.setUserId(data.id);
    this.form = { name: data.name, phoneNumber: data.phoneNumber, password: '' };
    this.loading.set(false);
  }

 save() {
  if (!this.form.name.trim()) { this.error.set('Name is required.'); return; }
  if (!/^\d{10}$/.test(this.form.phoneNumber)) { this.error.set('Phone must be 10 digits.'); return; }
  this.saving.set(true); this.error.set(''); this.success.set('');

  const p = this.profile()!;
  this.userSvc.update(p.id, {
    name: this.form.name,
    email: p.email,
    phoneNumber: this.form.phoneNumber,
    password: this.form.password || undefined,
    role: p.role
  } as any).subscribe({
    next: () => {
      this.success.set('Profile updated successfully.');
      this.saving.set(false);
      this.editMode.set(false);
      // Update the local profile signal directly instead of re-fetching
      this.profile.set({ ...p, name: this.form.name, phoneNumber: this.form.phoneNumber });
    },
    error: err => {
      console.error('Update error:', err); // â† add this to see the real error
      this.error.set(err.error?.message || err.message || 'Update failed.');
      this.saving.set(false);
    }
  });
}

  cancelEdit() {
    const p = this.profile();
    if (p) this.form = { name: p.name, phoneNumber: p.phoneNumber, password: '' };
    this.editMode.set(false);
    this.error.set('');
  }
}
