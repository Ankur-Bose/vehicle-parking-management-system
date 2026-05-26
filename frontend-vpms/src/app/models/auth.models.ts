export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  phoneNumber: string;
  password: string;
  role: 'ADMIN' | 'STAFF' | 'CUSTOMER';
}

export interface AuthResponse {
  token: string;
  role: string;
  userId: number;
  message: string;
}
