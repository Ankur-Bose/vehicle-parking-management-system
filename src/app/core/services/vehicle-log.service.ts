import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { EntryRequest, ExitRequest, VehicleLogResponse } from '../../models/vehicle-log.models';

@Injectable({ providedIn: 'root' })
export class VehicleLogService {
  private base = `${environment.apiUrl}/logs`;

  constructor(private http: HttpClient) {}

  logEntry(req: EntryRequest) {
    return this.http.post<VehicleLogResponse>(`${this.base}/entry`, req);
  }

  logExit(req: ExitRequest) {
    return this.http.post<VehicleLogResponse>(`${this.base}/exit`, req);
  }

  getById(logId: number) {
    return this.http.get<VehicleLogResponse>(`${this.base}/${logId}`);
  }

  getByVehicle(vehicleNumber: string) {
    return this.http.get<VehicleLogResponse[]>(`${this.base}/vehicle/${vehicleNumber}`);
  }

  getByUser(userId: number) {
    return this.http.get<VehicleLogResponse[]>(`${this.base}/user/${userId}`);
  }

  getActive() {
    return this.http.get<VehicleLogResponse[]>(`${this.base}/active`);
  }

  getAll() {
    return this.http.get<VehicleLogResponse[]>(`${this.base}`);
  }
}
