import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../core/services/user.service';
import { UserResponse } from '../../models/user.models';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.css'],
})
export class UsersComponent implements OnInit {
  private svc = inject(UserService);

  users = signal<UserResponse[]>([]);
  loading = signal(true);
  saving = signal(false);
  error = signal('');
  success = signal('');
  searchTerm = '';
  filterRole = '';

  showModal = signal(false);
  editMode = signal(false);
  selectedUser = signal<UserResponse | null>(null);

  form = { name: '', email: '', phoneNumber: '', password: '', role: 'CUSTOMER' as any };

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.svc.getAll().subscribe({
      next: (data) => {
        this.users.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load users.');
        this.loading.set(false);
      },
    });
  }

  get filtered() {
    return this.users().filter((u) => {
      const matchSearch =
        !this.searchTerm ||
        u.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        u.email.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchRole = !this.filterRole || u.role === this.filterRole;
      return matchSearch && matchRole;
    });
  }

  openAdd() {
    this.editMode.set(false);
    this.selectedUser.set(null);
    this.form = { name: '', email: '', phoneNumber: '', password: '', role: 'CUSTOMER' };
    this.showModal.set(true);
    this.error.set('');
    this.success.set('');
  }

  openEdit(u: UserResponse) {
    this.editMode.set(true);
    this.selectedUser.set(u);
    this.form = {
      name: u.name,
      email: u.email,
      phoneNumber: u.phoneNumber,
      password: '',
      role: u.role,
    };
    this.showModal.set(true);
    this.error.set('');
    this.success.set('');
  }

  save() {
    this.saving.set(true);
    this.error.set('');
    this.success.set('');
    if (this.editMode() && this.selectedUser()) {
      this.svc.update(this.selectedUser()!.id, this.form as any).subscribe({
        next: () => {
          this.success.set('User updated.');
          this.saving.set(false);
          this.showModal.set(false);
          this.load();
        },
        error: (err) => {
          this.error.set(err.error || 'Update failed.');
          this.saving.set(false);
        },
      });
    } else {
      this.svc.register(this.form as any).subscribe({
        next: () => {
          this.success.set('User created.');
          this.saving.set(false);
          this.showModal.set(false);
          this.load();
        },
        error: (err) => {
          this.error.set(err.error?.message || 'Create failed.');
          this.saving.set(false);
        },
      });
    }
  }

  delete(u: UserResponse) {
    if (!confirm(`Delete user "${u.name}"?`)) return;
    this.svc.delete(u.id).subscribe({
      next: () => {
        this.success.set('User deleted.');
        this.load();
      },
      error: (err) => this.error.set(err.error || 'Delete failed.'),
    });
  }

  roleBadge(role: string) {
    return role === 'ADMIN' ? 'danger' : role === 'STAFF' ? 'warning' : 'primary';
  }
}
