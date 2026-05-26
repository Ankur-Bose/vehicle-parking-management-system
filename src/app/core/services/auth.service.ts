import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { LoginRequest, AuthResponse } from '../../models/auth.models';
import { jwtDecode } from 'jwt-decode';

interface JwtPayload {
  sub: string;
  role: string;
  exp: number;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'vpms_token';
  private readonly ROLE_KEY = 'vpms_role';
  private readonly EMAIL_KEY = 'vpms_email';
  private readonly USER_ID_KEY = 'vpms_user_id';

  isLoggedIn = signal(this.hasValidToken());
  currentRole = signal(this.getRole());

  constructor(private http: HttpClient, private router: Router) {}

  login(request: LoginRequest) {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/users/login`, request).pipe(
      tap(res => {
        localStorage.setItem(this.TOKEN_KEY, res.token);
        localStorage.setItem(this.ROLE_KEY, res.role);
        if (res.userId) {
          localStorage.setItem(this.USER_ID_KEY, res.userId.toString());
        }
        try {
          const decoded = jwtDecode<JwtPayload>(res.token);
          localStorage.setItem(this.EMAIL_KEY, decoded.sub);
        } catch {}
        this.isLoggedIn.set(true);
        this.currentRole.set(res.role);
      })
    );
  }

  logout() {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.ROLE_KEY);
    localStorage.removeItem(this.EMAIL_KEY);
    localStorage.removeItem(this.USER_ID_KEY);
    this.isLoggedIn.set(false);
    this.currentRole.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getRole(): string | null {
    return localStorage.getItem(this.ROLE_KEY);
  }

  getEmail(): string | null {
    return localStorage.getItem(this.EMAIL_KEY);
  }

  setUserId(id: number) {
    localStorage.setItem(this.USER_ID_KEY, id.toString());
  }

  getUserId(): number | null {
    const id = localStorage.getItem(this.USER_ID_KEY);
    return id ? parseInt(id, 10) : null;
  }

  isAdmin(): boolean {
    return this.getRole() === 'ADMIN';
  }

  isStaff(): boolean {
    return this.getRole() === 'STAFF';
  }

  isCustomer(): boolean {
    return this.getRole() === 'CUSTOMER';
  }

  hasRole(...roles: string[]): boolean {
    const role = this.getRole();
    return role !== null && roles.includes(role);
  }

  private hasValidToken(): boolean {
    const token = localStorage.getItem(this.TOKEN_KEY);
    if (!token) return false;
    try {
      const decoded = jwtDecode<JwtPayload>(token);
      return decoded.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }
}
