import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReservationService } from '../../core/services/reservation.service';
import { InvoiceService } from '../../core/services/invoice.service';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { ReservationResponse } from '../../models/reservation.models';

export interface InvoiceRow {
  invoiceId: number;
  vehicleNumber: string;
  durationMinutes: number;
  amount: number;
  paymentMethod: string | null;
  status: 'PENDING' | 'PAID' | 'CANCELLED';
  createdAt: string;
}

@Component({
  selector: 'app-booking-history',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './booking-history.component.html',
  styleUrls: ['./booking-history.component.css'],
})
export class BookingHistoryComponent implements OnInit {
  private resSvc = inject(ReservationService);
  private invoiceSvc = inject(InvoiceService);
  private userSvc = inject(UserService);
  auth = inject(AuthService);

  reservations = signal<ReservationResponse[]>([]);
  invoices = signal<InvoiceRow[]>([]);
  loading = signal(true);
  error = signal('');

  activeTab: 'reservations' | 'invoices' = 'reservations';
  filterStatus = '';
  searchVehicle = '';

  // Invoice detail modal
  selectedInvoice = signal<InvoiceRow | null>(null);
  showInvoiceDetail = signal(false);

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.error.set('');
    const userId = this.auth.getUserId();
    if (!userId) {
      this.userSvc.getMe().subscribe({
        next: (me: any) => {
          this.auth.setUserId(me.id);
          this.doLoad(me.id);
        },
        error: () => {
          this.error.set('Session expired. Please log in again.');
          this.loading.set(false);
        },
      });
      return;
    }
    this.doLoad(userId);
  }

  private doLoad(userId: number) {
    this.resSvc.getByUser(userId).subscribe({
      next: (data: ReservationResponse[]) => {
        this.reservations.set(data);
        this.invoiceSvc.getByUser(userId).subscribe({
          next: (raw: any[]) => {
            const rows: InvoiceRow[] = raw.map((inv: any) => ({
              invoiceId: inv.invoiceId,
              vehicleNumber: inv.vehicleNumber ?? '—',
              durationMinutes: inv.durationMinutes ?? 0,
              amount: inv.amount ?? 0,
              paymentMethod: inv.paymentMethod ?? null,
              status: inv.status,
              createdAt: inv.createdAt,
            }));
            this.invoices.set(rows);
            this.loading.set(false);
          },
          error: () => {
            this.error.set('Failed to load invoices.');
            this.loading.set(false);
          },
        });
      },
      error: () => {
        this.error.set('Failed to load booking history.');
        this.loading.set(false);
      },
    });
  }

  openInvoiceDetail(inv: InvoiceRow) {
    this.selectedInvoice.set(inv);
    this.showInvoiceDetail.set(true);
  }

  closeInvoiceDetail() {
    this.showInvoiceDetail.set(false);
    this.selectedInvoice.set(null);
  }

  get filteredReservations() {
    return this.reservations().filter((r) => {
      const matchStatus = !this.filterStatus || r.status === this.filterStatus;
      const matchVehicle =
        !this.searchVehicle ||
        r.vehicleNumber.toLowerCase().includes(this.searchVehicle.toLowerCase());
      return matchStatus && matchVehicle;
    });
  }

  get filteredInvoices() {
    return this.invoices().filter(
      (i) =>
        !this.searchVehicle ||
        i.vehicleNumber.toLowerCase().includes(this.searchVehicle.toLowerCase()),
    );
  }

  get totalSpent() {
    return this.invoices()
      .filter((i) => i.status === 'PAID')
      .reduce((s, i) => s + i.amount, 0)
      .toFixed(2);
  }

  formatDuration(mins?: number | null) {
    if (mins === undefined || mins === null) return '—';
    if (mins === 0) return '< 1 min';
    const h = Math.floor(mins / 60),
      m = mins % 60;
    return h > 0 ? `${h}h ${m}m` : `${m}m`;
  }
}
