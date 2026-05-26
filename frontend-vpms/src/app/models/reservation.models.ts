export interface ReservationRequest {
  vehicleNumber: string;
  startTime: string;
  endTime?: string;
}

export interface ReservationResponse {
  id: number;
  vehicleNumber: string;
  startTime: string;
  endTime?: string;
  status: string;
  userId: number;
  userName: string;
  slotId: number;
  slotType: string;
  slotLocation: string;
}
