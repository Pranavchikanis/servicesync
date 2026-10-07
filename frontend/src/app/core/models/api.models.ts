export interface LoginRequest {
  email?: string;
  password?: string;
}

export interface AuthResponse {
  token: string;
  role: string;
  name: string;
}

export interface UserDTO {
  id: number;
  name: string;
  email: string;
  role: string;
}

export interface TicketPartDTO {
  id: number;
  sku: string;
  partName: string;
  quantityUsed: number;
  priceAtTime: number;
}

export interface TicketDTO {
  id: number;
  customerName: string;
  customerPhone: string;
  deviceInfo: string;
  issueDesc: string;
  status: string;
  createdAt: string;
  resolvedAt?: string;
  totalCost?: number;
  createdBy?: UserDTO;
  technician?: UserDTO;
  parts?: TicketPartDTO[];
}

export interface TicketCreateRequest {
  customerName: string;
  customerPhone: string;
  deviceInfo: string;
  issueDesc: string;
}

export interface TicketStatusUpdateRequest {
  status: string;
  notes?: string;
}

export interface TicketAssignmentRequest {
  technicianId: number;
}

export interface PartConsumptionRequest {
  inventoryId: number;
  quantity: number;
}

export interface InventoryDTO {
  id: number;
  sku: string;
  partName: string;
  quantityInStock: number;
  price: number;
}

export interface TicketHistoryDTO {
  id: number;
  previousStatus: string;
  newStatus: string;
  changedByName: string;
  changedAt: string;
  notes: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface ApiErrorDetail {
  field: string;
  issue: string;
}

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  details?: ApiErrorDetail[];
}
