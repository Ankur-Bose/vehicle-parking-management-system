import { Component, inject, OnInit, Signal, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VehicleLogService } from '../../core/services/vehicle-log.service';
import { InvoiceService } from '../../core/services/invoice.service';
import { SlotService } from '../../core/services/slot.service';
import { AuthService } from '../../core/services/auth.service';
import { ReservationService } from '../../core/services/reservation.service';
import { VehicleLogResponse } from '../../models/vehicle-log.models';
import { CustomerBillDTO } from '../../models/invoice.models';
import { SlotResponse } from '../../models/slot.models';
import { ReservationResponse } from '../../models/reservation.models';

@Component({
  selector: 'app-vehicle-logs',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './vehicle-logs.component.html',
  styleUrls: ['./vehicle-logs.component.css'],
})
export class VehicleLogsComponent implements OnInit {
  private svc = inject(VehicleLogService);
  private invoiceSvc = inject(InvoiceService);
  private slotSvc = inject(SlotService);
  private resSvc = inject(ReservationService);
  auth = inject(AuthService);

  logs = signal<VehicleLogResponse[]>([]);
  availableSlots = signal<SlotResponse[]>([]);
  activeReservations = signal<ReservationResponse[]>([]);
  loading = signal(true);
  saving = signal(false);
  error = signal('');
  success = signal('');

  filterStatus = '';
  searchVehicle = '';
  entrySearch = '';
  exitSearch = '';
  showEntryDropdown = false;
  showExitDropdown = false;

  showEntryModal = signal(false);
  showExitModal = signal(false);
  showInvoiceModal = signal(false);

  // Entry form — two modes
  entryMode: 'reserved' | 'walkin' = 'reserved';

  // Reserved mode fields
  reservedVehicleNumber = '';
  reservedSlotId: number | null = null;
  reservedReservationId: number | null = null;
  reservationSelected = false;

  // Walk-in mode fields
  walkinVehicleNumber = '';
  walkinSlotId: number = 0;

  exitVehicleNumber = '';

  // After exit: invoice generation
  exitedLog = signal<VehicleLogResponse | null>(null);
  paymentMethod = signal<'UPI' | 'CASH'>('UPI');
  generatedInvoice = signal<CustomerBillDTO | null>(null);
  generatingInvoice = signal(false);
  awaitingConfirmation = signal(false);
  paymentDone = signal(false);

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.svc.getAll().subscribe({
      next: (data) => {
        this.logs.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load logs.');
        this.loading.set(false);
      },
    });
  }

  get filtered() {
    return this.logs().filter((l) => {
      const matchStatus = !this.filterStatus || l.status === this.filterStatus;
      const matchVehicle =
        !this.searchVehicle ||
        l.vehicleNumber.toLowerCase().includes(this.searchVehicle.toLowerCase());
      return matchStatus && matchVehicle;
    });
  }

  openEntryModal() {
    // Reset both modes
    this.entryMode = 'reserved';
    this.entrySearch = '';
    this.showEntryDropdown = false;
    this.reservedVehicleNumber = '';
    this.reservedSlotId = null;
    this.reservedReservationId = null;
    this.reservationSelected = false;
    this.walkinVehicleNumber = '';
    this.walkinSlotId = 0;

    this.resSvc.getActive().subscribe({
      next: (res) => this.activeReservations.set(res),
      error: () => this.activeReservations.set([]),
    });
    this.slotSvc.getAll(undefined, -1).subscribe({
      next: (slots) => this.availableSlots.set(slots),
      error: () => {},
    });
    this.showEntryModal.set(true);
    this.error.set('');
    this.success.set('');
  }

  switchEntryMode(mode: 'reserved' | 'walkin') {
    this.entryMode = mode;
    this.error.set('');
  }

  selectReservationFromDropdown(
    res: import('../../models/reservation.models').ReservationResponse,
  ) {
    this.reservedVehicleNumber = res.vehicleNumber;
    this.reservedSlotId = res.slotId;
    this.reservedReservationId = res.id;
    this.entrySearch = res.vehicleNumber;
    this.reservationSelected = true;
    this.showEntryDropdown = false;
  }

  clearEntrySearch() {
    this.entrySearch = '';
    this.reservedVehicleNumber = '';
    this.reservedSlotId = null;
    this.reservedReservationId = null;
    this.reservationSelected = false;
    this.showEntryDropdown = false;
  }

  closeEntryDropdown() {
    setTimeout(() => {
      this.showEntryDropdown = false;
    }, 150);
  }

  selectExitFromDropdown(log: VehicleLogResponse) {
    this.exitVehicleNumber = log.vehicleNumber;
    this.exitSearch = log.vehicleNumber;
    this.showExitDropdown = false;
  }

  clearExitSearch() {
    this.exitSearch = '';
    this.exitVehicleNumber = '';
    this.showExitDropdown = false;
  }

  closeExitDropdown() {
    setTimeout(() => {
      this.showExitDropdown = false;
    }, 150);
  }

  openExitModal() {
    this.exitVehicleNumber = '';
    this.exitSearch = '';
    this.showExitDropdown = false;
    this.showExitModal.set(true);
    this.error.set('');
    this.success.set('');
  }

  get activeVehicleLogs() {
    return this.logs().filter((l) => l.status === 'ACTIVE');
  }

  get filteredReservations() {
    // exclude vehicles already parked (ACTIVE log entry exists)
    const parked = new Set(
      this.logs()
        .filter((l) => l.status === 'ACTIVE')
        .map((l) => l.vehicleNumber),
    );
    const q = this.entrySearch.toLowerCase();
    return this.activeReservations()
      .filter((r) => !parked.has(r.vehicleNumber))
      .filter(
        (r) =>
          !q ||
          r.vehicleNumber.toLowerCase().includes(q) ||
          String(r.slotId).includes(q) ||
          (r.userName ?? '').toLowerCase().includes(q),
      );
  }

  get filteredActiveLogs() {
    const q = this.exitSearch.toLowerCase();
    return this.activeVehicleLogs.filter(
      (l) => !q || l.vehicleNumber.toLowerCase().includes(q) || String(l.slotId).includes(q),
    );
  }

  logEntry() {
    const userId = this.auth.getUserId();
    if (!userId) {
      this.error.set('User session expired. Please log in again.');
      return;
    }

    let req: import('../../models/vehicle-log.models').EntryRequest;

    if (this.entryMode === 'reserved') {
      if (!this.reservedVehicleNumber || !this.reservedReservationId) {
        this.error.set('Please select a reserved vehicle from the list.');
        return;
      }
      req = {
        vehicleNumber: this.reservedVehicleNumber,
        slotId: this.reservedSlotId ?? undefined,
        reservationId: this.reservedReservationId,
        userId,
      };
    } else {
      const vn = this.walkinVehicleNumber.trim().toUpperCase();
      if (!vn) {
        this.error.set('Vehicle number is required.');
        return;
      }
      if (!this.walkinSlotId) {
        this.error.set('Please select an available slot.');
        return;
      }
      req = { vehicleNumber: vn, slotId: this.walkinSlotId, userId };
    }

    this.saving.set(true);
    this.error.set('');
    this.svc.logEntry(req).subscribe({
      next: () => {
        this.success.set('Vehicle entry logged successfully.');
        this.saving.set(false);
        this.showEntryModal.set(false);
        this.load();
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Entry failed.');
        this.saving.set(false);
      },
    });
  }

  logExit() {
    if (!this.exitVehicleNumber.trim()) {
      this.error.set('Vehicle number required.');
      return;
    }
    this.saving.set(true);
    this.error.set('');
    this.svc.logExit({ vehicleNumber: this.exitVehicleNumber.trim() }).subscribe({
      next: (log) => {
        this.saving.set(false);
        this.showExitModal.set(false);
        this.exitedLog.set(log);
        this.paymentMethod.set('UPI')
        this.generatedInvoice.set(null);
        this.showInvoiceModal.set(true);
        this.load();
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Exit failed.');
        this.saving.set(false);
      },
    });
  }

  // Step 1 â†’ Step 2: generate invoice, wait for staff confirmation
proceedToConfirm() {
  const log = this.exitedLog();
  if (!log) return;

  const role = this.auth.getRole();
  if (role !== 'STAFF' && role !== 'ADMIN') {
    this.error.set('Only STAFF or ADMIN can generate invoices.');
    return;
  }

  this.generatingInvoice.set(true);
  this.error.set('');

  this.invoiceSvc.generate(log.userId, log.reservationId || 0, log.logId, log.slotId).subscribe({
    next: invoice => {
      this.generatedInvoice.set(invoice);
      this.generatingInvoice.set(false);
      this.awaitingConfirmation.set(true);
    },
    error: err => {
      this.error.set(err.error?.message || 'Invoice generation failed.');
      this.generatingInvoice.set(false);
    }
  });
}

// confirmAndPay — add () to paymentMethod
confirmAndPay() {
  const invoice = this.generatedInvoice();
  if (!invoice) return;
  this.generatingInvoice.set(true);
  this.invoiceSvc.pay({
    invoiceId: invoice.invoiceId,
    paymentMethod: this.paymentMethod()
  }).subscribe({
    next: () => {
      this.generatingInvoice.set(false);
      this.awaitingConfirmation.set(false);
      this.paymentDone.set(true);
    },
    error: () => { this.generatingInvoice.set(false); }
  });
}

// closeInvoiceModal — reset signal
closeInvoiceModal() {
  this.showInvoiceModal.set(false);
  this.exitedLog.set(null);
  this.generatedInvoice.set(null);
  this.awaitingConfirmation.set(false);
  this.paymentDone.set(false);
  this.paymentMethod.set('UPI');
}

  // Step 2 â†’ Step 1: go back to method selection
  // Note: invoice already created as PENDING â€” will appear in Invoices tab
  backToMethodSelect() {
    this.awaitingConfirmation.set(false);
    this.generatedInvoice.set(null);
  }

  get activeCount() {
    return this.logs().filter((l) => l.status === 'ACTIVE').length;
  }
  get completedCount() {
    return this.logs().filter((l) => l.status === 'COMPLETED').length;
  }

  formatDuration(mins?: number | null) {
    if (mins === undefined || mins === null) return '—';
    if (mins === 0) return '< 1 min';
    const h = Math.floor(mins / 60),
      m = mins % 60;
    return h > 0 ? `${h}h ${m}m` : `${m}m`;
  }
}
