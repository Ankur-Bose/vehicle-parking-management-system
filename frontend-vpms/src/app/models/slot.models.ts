export interface SlotResponse {
  id: number;
  type: '2W' | '4W';
  occupiedStatus: -1 | 0 | 1;
  location: 'A' | 'B' | 'C' | 'D' | 'G';
}

export interface AddSlotRequest {
  type: '2W' | '4W';
  location: 'A' | 'B' | 'C' | 'D' | 'G';
}

export interface UpdateSlotRequest {
  type: '2W' | '4W';
  location: 'A' | 'B' | 'C' | 'D' | 'G';
}
