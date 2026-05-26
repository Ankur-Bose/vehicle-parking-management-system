import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SlotService } from '../../core/services/slot.service';
import { SlotResponse } from '../../models/slot.models';

@Component({
  selector: 'app-slots',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './slots.component.html',
  styleUrls: ['./slots.component.css']
})
export class SlotsComponent implements OnInit {
  private svc = inject(SlotService);

  slots = signal<SlotResponse[]>([]);
  loading = signal(true);
  saving = signal(false);
  error = signal('');
  success = signal('');

  filterType = '';
  filterStatus = '';

  showModal = signal(false);
  editMode = signal(false);
  selectedSlot = signal<SlotResponse | null>(null);
  form = { type: '2W' as '2W' | '4W', location: 'A' as any };

  ngOnInit() { this.load(); }

  load() {
    this.loading.set(true);
    const type = this.filterType || undefined;
    const status = this.filterStatus !== '' ? parseInt(this.filterStatus) : undefined;
    this.svc.getAll(type, status).subscribe({
      next: data => { this.slots.set(data); this.loading.set(false); },
      error: () => { this.error.set('Failed to load slots.'); this.loading.set(false); }
    });
  }

  applyFilter() { this.load(); }

  openAdd() {
    this.editMode.set(false);
    this.selectedSlot.set(null);
    this.form = { type: '2W', location: 'A' };
    this.showModal.set(true);
    this.error.set(''); this.success.set('');
  }

  openEdit(s: SlotResponse) {
    this.editMode.set(true);
    this.selectedSlot.set(s);
    this.form = { type: s.type, location: s.location };
    this.showModal.set(true);
    this.error.set(''); this.success.set('');
  }

  save() {
    this.saving.set(true);
    this.error.set('');
    if (this.editMode() && this.selectedSlot()) {
      this.svc.update(this.selectedSlot()!.id, this.form).subscribe({
        next: () => { this.success.set('Slot updated.'); this.saving.set(false); this.showModal.set(false); this.load(); },
        error: err => { this.error.set(err.error?.message || 'Update failed.'); this.saving.set(false); }
      });
    } else {
      this.svc.add(this.form).subscribe({
        next: () => { this.success.set('Slot added.'); this.saving.set(false); this.showModal.set(false); this.load(); },
        error: err => { this.error.set(err.error?.message || 'Add failed.'); this.saving.set(false); }
      });
    }
  }

  updateStatus(s: SlotResponse, status: number) {
    this.svc.updateStatus(s.id, status).subscribe({
      next: () => { this.success.set('Status updated.'); this.load(); },
      error: err => this.error.set(err.error?.message || 'Update failed.')
    });
  }

  delete(s: SlotResponse) {
    if (!confirm(`Delete slot #${s.id}?`)) return;
    this.svc.delete(s.id).subscribe({
      next: () => { this.success.set('Slot deleted.'); this.load(); },
      error: err => this.error.set(err.error?.message || 'Delete failed.')
    });
  }

  statusLabel(s: number) { return s === -1 ? 'Available' : s === 0 ? 'Reserved' : 'Occupied'; }
  statusBadge(s: number) { return s === -1 ? 'success' : s === 0 ? 'warning' : 'danger'; }
  typeBadge(t: string) { return t === '2W' ? 'info' : 'primary'; }

  get stats() {
    const all = this.slots();
    return {
      total: all.length,
      available: all.filter(s => s.occupiedStatus === -1).length,
      reserved: all.filter(s => s.occupiedStatus === 0).length,
      occupied: all.filter(s => s.occupiedStatus === 1).length,
      twoW: all.filter(s => s.type === '2W').length,
      fourW: all.filter(s => s.type === '4W').length,
    };
  }
}
