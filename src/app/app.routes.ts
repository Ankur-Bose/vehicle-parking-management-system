import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { LayoutComponent } from './layout/layout.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { UsersComponent } from './features/users/users.component';
import { SlotsComponent } from './features/slots/slots.component';
import { ReservationsComponent } from './features/reservations/reservations.component';
import { VehicleLogsComponent } from './features/vehicle-logs/vehicle-logs.component';
import { InvoicesComponent } from './features/invoices/invoices.component';
import { ProfileComponent } from './features/profile/profile.component';
import { BookingHistoryComponent } from './features/booking-history/booking-history.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'users', component: UsersComponent },
      { path: 'slots', component: SlotsComponent },
      { path: 'reservations', component: ReservationsComponent },
      { path: 'vehicle-logs', component: VehicleLogsComponent },
      { path: 'invoices', component: InvoicesComponent },
      { path: 'profile', component: ProfileComponent },
      { path: 'booking-history', component: BookingHistoryComponent },
    ]
  },
  { path: '**', redirectTo: '' }
];
