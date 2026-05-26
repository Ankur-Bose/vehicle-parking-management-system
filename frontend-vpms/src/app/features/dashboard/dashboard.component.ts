import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { SlotService } from '../../core/services/slot.service';
import { ReservationService } from '../../core/services/reservation.service';
import { VehicleLogService } from '../../core/services/vehicle-log.service';
import { InvoiceService } from '../../core/services/invoice.service';
import { UserService } from '../../core/services/user.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  auth = inject(AuthService);
  private slotSvc = inject(SlotService);
  private resSvc = inject(ReservationService);
  private logSvc = inject(VehicleLogService);
  private invoiceSvc = inject(InvoiceService);
  private userSvc = inject(UserService);

  loading = signal(true);
  stats = signal({
    totalSlots: 0, availableSlots: 0, occupiedSlots: 0, reservedSlots: 0,
    activeReservations: 0, totalReservations: 0,
    activeLogs: 0, totalInvoices: 0, pendingInvoices: 0
  });

  recentReservations = signal<any[]>([]);
  activeLogs = signal<any[]>([]);

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.loading.set(true);
    if (this.auth.isCustomer()) {
      this.loadCustomerData();
    } else {
      this.loadAdminStaffData();
    }
  }

  private loadCustomerData() {
    const userId = this.auth.getUserId();
    if (!userId) {
      this.userSvc.getMe().subscribe({
        next: me => { this.auth.setUserId(me.id); this.fetchCustomerStats(me.id); },
        error: () => this.loading.set(false)
      });
      return;
    }
    this.fetchCustomerStats(userId);
  }

  private fetchCustomerStats(userId: number) {
    forkJoin({
      allSlots: this.slotSvc.getAll(),
      myReservations: this.resSvc.getByUser(userId),
      myLogs: this.logSvc.getByUser(userId)
    }).subscribe({
      next: ({ allSlots, myReservations, myLogs }) => {
        const available = allSlots.filter(s => s.occupiedStatus === -1).length;
        const occupied = allSlots.filter(s => s.occupiedStatus === 1).length;
        const reserved = allSlots.filter(s => s.occupiedStatus === 0).length;
        const activeReservations = myReservations.filter(r => r.status === 'ACTIVE').length;
        const currentlyParked = myLogs.filter(l => l.status === 'ACTIVE').length;

        this.stats.set({
          totalSlots: allSlots.length,
          availableSlots: available,
          occupiedSlots: occupied,
          reservedSlots: reserved,
          activeReservations,
          totalReservations: myReservations.length,
          activeLogs: currentlyParked,
          totalInvoices: 0,
          pendingInvoices: 0
        });
        this.recentReservations.set(myReservations.slice(0, 5));
        this.activeLogs.set(myLogs.filter(l => l.status === 'ACTIVE'));
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  private loadAdminStaffData() {
    forkJoin({
      allSlots: this.slotSvc.getAll(),
      activeRes: this.resSvc.getActive(),
      allRes: this.resSvc.getAll(),
      activeLogs: this.logSvc.getActive()
    }).subscribe({
      next: ({ allSlots, activeRes, allRes, activeLogs }) => {
        const available = allSlots.filter(s => s.occupiedStatus === -1).length;
        const occupied = allSlots.filter(s => s.occupiedStatus === 1).length;
        const reserved = allSlots.filter(s => s.occupiedStatus === 0).length;

        this.stats.set({
          totalSlots: allSlots.length,
          availableSlots: available,
          occupiedSlots: occupied,
          reservedSlots: reserved,
          activeReservations: activeRes.length,
          totalReservations: allRes.length,
          activeLogs: activeLogs.length,
          totalInvoices: 0,
          pendingInvoices: 0
        });

        this.recentReservations.set(allRes.slice(0, 5));
        this.activeLogs.set(activeLogs.slice(0, 5));

        if (this.auth.hasRole('ADMIN', 'STAFF')) {
          this.invoiceSvc.getAll().subscribe({
            next: invoices => {
              this.stats.update(s => ({
                ...s,
                totalInvoices: invoices.length,
                pendingInvoices: invoices.filter(i => i.status === 'PENDING').length
              }));
            },
            error: () => {}
          });
        }
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  getStatusClass(status: number): string {
    if (status === -1) return 'success';
    if (status === 0) return 'warning';
    return 'danger';
  }

  getStatusLabel(status: number): string {
    if (status === -1) return 'Available';
    if (status === 0) return 'Reserved';
    return 'Occupied';
  }
}
