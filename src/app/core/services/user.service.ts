import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { UserResponse, UpdateUserRequest } from '../../models/user.models';
import { RegisterRequest, AuthResponse } from '../../models/auth.models';

@Injectable({ providedIn: 'root' })
export class UserService {
  private base = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) {}

  getMe() {
    return this.http.get<UserResponse>(`${this.base}/me`);
  }

  getAll() {
    return this.http.get<UserResponse[]>(`${this.base}/`);
  }

  getById(id: number) {
    return this.http.get<UserResponse>(`${this.base}/${id}`);
  }

  register(req: RegisterRequest) {
    return this.http.post<AuthResponse>(`${this.base}/register`, req);
  }

  update(id: number, req: UpdateUserRequest) {
    return this.http.put(`${this.base}/updateUser/${id}`, req, { responseType: 'text' });
  }

  delete(id: number) {
    return this.http.delete(`${this.base}/${id}`, { responseType: 'text' });
  }
}