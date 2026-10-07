import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { 
  TicketDTO, 
  Page, 
  TicketCreateRequest, 
  TicketStatusUpdateRequest, 
  TicketAssignmentRequest,
  PartConsumptionRequest,
  TicketPartDTO,
  TicketHistoryDTO
} from '../models/api.models';

@Injectable({
  providedIn: 'root'
})
export class TicketService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/tickets`;

  getTickets(page: number = 0, size: number = 20, status?: string, technicianId?: number): Observable<Page<TicketDTO>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
      
    if (status) {
      params = params.set('status', status);
    }
    if (technicianId) {
      params = params.set('technicianId', technicianId.toString());
    }

    return this.http.get<Page<TicketDTO>>(this.baseUrl, { params });
  }

  getTicketById(id: number): Observable<TicketDTO> {
    return this.http.get<TicketDTO>(`${this.baseUrl}/${id}`);
  }

  createTicket(payload: TicketCreateRequest): Observable<TicketDTO> {
    return this.http.post<TicketDTO>(this.baseUrl, payload);
  }

  getTicketHistory(id: number): Observable<TicketHistoryDTO[]> {
    return this.http.get<TicketHistoryDTO[]>(`${this.baseUrl}/${id}/history`);
  }

  updateStatus(id: number, payload: TicketStatusUpdateRequest): Observable<TicketDTO> {
    return this.http.patch<TicketDTO>(`${this.baseUrl}/${id}/status`, payload);
  }

  assignTechnician(id: number, payload: TicketAssignmentRequest): Observable<TicketDTO> {
    return this.http.patch<TicketDTO>(`${this.baseUrl}/${id}/assignment`, payload);
  }

  consumePart(id: number, payload: PartConsumptionRequest): Observable<TicketPartDTO> {
    return this.http.post<TicketPartDTO>(`${this.baseUrl}/${id}/parts`, payload);
  }

  getPublicTicket(id: number, phone: string): Observable<TicketDTO> {
    const params = new HttpParams().set('phone', phone);
    return this.http.get<TicketDTO>(`${environment.apiUrl}/public/tickets/${id}`, { params });
  }

  getSlaBreaches(days: number = 30): Observable<any[]> {
    const params = new HttpParams().set('days', days.toString());
    return this.http.get<any[]>(`${environment.apiUrl}/reports/sla-breaches`, { params });
  }
}
