import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CustomerBillDTO, AdminBillDTO, PaymentRequest } from '../../models/invoice.models';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class InvoiceService {
  private base = `${environment.apiUrl}/api/billing`;

  constructor(private http: HttpClient, private auth: AuthService) {}

  private roleHeader(): HttpHeaders {
    return new HttpHeaders({ 'X-Role': this.auth.getRole() || '' });
  }

  generate(userId: number, reservationId: number, logId: number, slotId: number) {
    const params = new HttpParams()
      .set('userId', userId.toString())
      .set('reservationId', reservationId.toString())
      .set('logId', logId.toString())
      .set('slotId', slotId.toString());
    return this.http.post<CustomerBillDTO>(`${this.base}/generate`, null, {
      params, headers: this.roleHeader()
    });
  }

  getById(id: number) {
    return this.http.get<CustomerBillDTO | AdminBillDTO>(`${this.base}/${id}`, {
      headers: this.roleHeader()
    });
  }

  getAll(status?: string) {
    let params = new HttpParams();
    if (status) params = params.set('status', status);
    return this.http.get<AdminBillDTO[]>(`${this.base}/all`, {
      params, headers: this.roleHeader()
    });
  }

  // Always returns Observable<any[]> so the component can subscribe without TS union issues.
  // The normalisation into InvoiceRow happens in the component.
  getByUser(userId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/user/${userId}`, {
      headers: this.roleHeader()
    });
  }

  pay(req: PaymentRequest) {
    return this.http.post<any>(`${this.base}/pay`, req);
  }

  getRevenue(from: string, to: string) {
    const params = new HttpParams().set('from', from).set('to', to);
    return this.http.get<any>(`${this.base}/revenue`, {
      params, headers: this.roleHeader()
    });
  }
}