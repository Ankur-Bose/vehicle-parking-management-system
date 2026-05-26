import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ReservationRequest, ReservationResponse } from '../../models/reservation.models';

@Injectable({ providedIn: 'root' })
export class ReservationService {
  private base = `${environment.apiUrl}/reservations`;

  constructor(private http: HttpClient) {}

  create(userId: number, slotId: number, req: ReservationRequest) {
    return this.http.post<ReservationResponse>(`${this.base}/${userId}/${slotId}`, req);
  }

  getById(id: number) {
    return this.http.get<ReservationResponse>(`${this.base}/${id}`);
  }

  getAll() {
    return this.http.get<ReservationResponse[]>(this.base);
  }

  getActive() {
    return this.http.get<ReservationResponse[]>(`${this.base}/active`);
  }

  getCancelled() {
    return this.http.get<ReservationResponse[]>(`${this.base}/cancelled`);
  }

  getByUser(userId: number) {
    return this.http.get<ReservationResponse[]>(`${this.base}/user/${userId}`);
  }

  update(id: number, req: ReservationRequest) {
    return this.http.put<ReservationResponse>(`${this.base}/${id}`, req);
  }

  cancel(id: number) {
    return this.http.delete<ReservationResponse>(`${this.base}/${id}`);
  }
}
