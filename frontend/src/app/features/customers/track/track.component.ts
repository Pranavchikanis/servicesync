import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TicketService } from '../../../core/services/ticket.service';
import { TicketDTO } from '../../../core/models/api.models';

@Component({
  selector: 'app-track',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './track.component.html',
  styleUrl: './track.component.css'
})
export class TrackComponent {
  ticketId: number | null = null;
  phoneNumber = '';
  
  isLoading = false;
  error: string | null = null;
  result: TicketDTO | null = null;

  private ticketService = inject(TicketService);

  onSubmit(event: Event) {
    event.preventDefault();
    if (!this.ticketId || !this.phoneNumber) return;
    
    this.isLoading = true;
    this.error = null;
    this.result = null;

    this.ticketService.getPublicTicket(this.ticketId, this.phoneNumber).subscribe({
      next: (ticket) => {
        this.result = ticket;
        this.isLoading = false;
      },
      error: (err) => {
        this.error = 'Ticket not found or phone number does not match.';
        this.isLoading = false;
      }
    });
  }

  searchAgain() {
    this.result = null;
    this.ticketId = null;
    this.phoneNumber = '';
    this.error = null;
  }

  getStatusBadge(status: string): string {
    switch (status) {
        case 'CREATED': return 'bg-warning text-dark';
        case 'DIAGNOSING': return 'bg-info text-dark';
        case 'WAITING_PARTS': return 'bg-danger';
        case 'RESOLVED': return 'bg-success';
        case 'CLOSED': return 'bg-secondary';
        default: return 'bg-secondary';
    }
  }
}
