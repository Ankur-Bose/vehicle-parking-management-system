import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { SlotResponse, AddSlotRequest, UpdateSlotRequest } from '../../models/slot.models';

@Injectable({ providedIn: 'root' })
export class SlotService {
  private base = `${environment.apiUrl}/slots`;

  constructor(private http: HttpClient) {}

  getAll(type?: string, status?: number) {
    let params = new HttpParams();
    if (type) params = params.set('type', type);
    if (status !== undefined && status !== null) params = params.set('status', status.toString());
    return this.http.get<SlotResponse[]>(this.base, { params });
  }

  getById(id: number) {
    return this.http.get<SlotResponse>(`${this.base}/${id}`);
  }

  getAvailable(type?: string) {
    let params = new HttpParams();
    if (type) params = params.set('type', type);
    return this.http.get<SlotResponse[]>(`${this.base}/available`, { params });
  }

  add(req: AddSlotRequest) {
    return this.http.post<SlotResponse>(this.base, req);
  }

  update(id: number, req: UpdateSlotRequest) {
    return this.http.put<SlotResponse>(`${this.base}/${id}`, req);
  }

  updateStatus(id: number, status: number) {
    return this.http.patch<SlotResponse>(`${this.base}/${id}`, null, {
      params: new HttpParams().set('status', status.toString())
    });
  }

  delete(id: number) {
    return this.http.delete<SlotResponse>(`${this.base}/${id}`);
  }
}
