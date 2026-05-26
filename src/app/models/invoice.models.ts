export interface CustomerBillDTO {
  invoiceId: number;
  vehicleNumber: string;
  durationMinutes: number;
  amount: number;
  paymentQRCodeBase64: string | null;
  status: 'PENDING' | 'PAID' | 'CANCELLED';
  createdAt: string;
}

export interface AdminBillDTO {
  invoiceId: number;
  userId: number;
  vehicleNumber: string;
  slotType: string | null;
  durationMinutes: number;
  amount: number;
  paymentMethod: string | null;
  status: 'PENDING' | 'PAID' | 'CANCELLED';
  createdAt: string;
}

export type InvoiceDTO = CustomerBillDTO | AdminBillDTO;

export interface PaymentRequest {
  invoiceId: number;
  paymentMethod: 'UPI' | 'CASH' | 'CARD' | 'ONLINE';
}