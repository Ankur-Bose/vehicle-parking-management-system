export interface UserResponse {
  id: number;
  name: string;
  email: string;
  phoneNumber: string;
  role: 'ADMIN' | 'STAFF' | 'CUSTOMER';
  createdAt: string;
}

export interface UpdateUserRequest {
  name: string;
  email: string;
  phoneNumber: string;
  password: string;
  role: 'ADMIN' | 'STAFF' | 'CUSTOMER';
}
