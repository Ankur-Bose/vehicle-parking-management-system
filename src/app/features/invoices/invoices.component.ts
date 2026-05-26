import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InvoiceService } from '../../core/services/invoice.service';
import { AdminBillDTO, CustomerBillDTO } from '../../models/invoice.models';

@Component({
  selector: 'app-invoices',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './invoices.component.html',
  styleUrls: ['./invoices.component.css']
})
export class InvoicesComponent implements OnInit {
  private svc = inject(InvoiceService);

  invoices = signal<AdminBillDTO[]>([]);
  loading = signal(true);
  saving = signal(false);
  error = signal('');
  success = signal('');

  filterStatus = '';
  searchVehicle = '';

  showGenerateModal = signal(false);
  showPayModal = signal(false);
  showQrModal = signal(false);
  selectedInvoice = signal<AdminBillDTO | null>(null);
  qrData = signal<CustomerBillDTO | null>(null);

  generateForm = { userId: 0, reservationId: 0, logId: 0, slotId: 0 };
  payForm = { invoiceId: 0, paymentMethod: 'UPI' as any };

  ngOnInit() { this.load(); }

  load() {
    this.loading.set(true);
    const obs = this.filterStatus ? this.svc.getAll(this.filterStatus) : this.svc.getAll();
    obs.subscribe({
      next: data => { this.invoices.set(data); this.loading.set(false); },
      error: () => { this.error.set('Failed to load invoices.'); this.loading.set(false); }
    });
  }

  get filtered() {
    return this.invoices().filter(i => {
      const matchSearch = !this.searchVehicle ||
        i.vehicleNumber.toLowerCase().includes(this.searchVehicle.toLowerCase());
      const matchStatus = !this.filterStatus || i.status === this.filterStatus;
      return matchSearch && matchStatus;
    });
  }

  openGenerate() {
    this.generateForm = { userId: 0, reservationId: 0, logId: 0, slotId: 0 };
    this.showGenerateModal.set(true);
    this.error.set(''); this.success.set('');
  }

  generate() {
    const f = this.generateForm;
    if (!f.userId || !f.reservationId || !f.logId || !f.slotId) {
      this.error.set('All fields are required.'); return;
    }
    this.saving.set(true); this.error.set('');
    this.svc.generate(f.userId, f.reservationId, f.logId, f.slotId).subscribe({
      next: bill => {
        this.qrData.set(bill);
        this.saving.set(false);
        this.showGenerateModal.set(false);
        this.showQrModal.set(true);
        this.load();
      },
      error: err => { this.error.set(err.error?.message || 'Generate failed.'); this.saving.set(false); }
    });
  }

  openPay(inv: AdminBillDTO) {
    this.selectedInvoice.set(inv);
    this.payForm = { invoiceId: inv.invoiceId, paymentMethod: 'UPI' };
    this.showPayModal.set(true);
    this.error.set(''); this.success.set('');
  }

  pay() {
    this.saving.set(true); this.error.set('');
    this.svc.pay(this.payForm).subscribe({
      next: () => {
        this.success.set('Payment processed successfully.');
        this.saving.set(false); this.showPayModal.set(false); this.load();
      },
      error: err => { this.error.set(err.error?.message || 'Payment failed.'); this.saving.set(false); }
    });
  }

  statusBadge(s: string) {
    return s === 'PAID' ? 'success' : s === 'PENDING' ? 'warning' : 'danger';
  }

  formatDuration(mins?: number | null) {
    if (mins === undefined || mins === null) return '—';
    if (mins === 0) return '< 1 min';
    const h = Math.floor(mins / 60), m = mins % 60;
    return h > 0 ? `${h}h ${m}m` : `${m}m`;
  }

  get totalRevenue() {
    return this.invoices()
      .filter(i => i.status === 'PAID')
      .reduce((sum, i) => sum + i.amount, 0)
      .toFixed(2);
  }

  get pendingCount() { return this.invoices().filter(i => i.status === 'PENDING').length; }
  get paidCount() { return this.invoices().filter(i => i.status === 'PAID').length; }
}
