import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TicketService } from '../../../core/services/ticket.service';
import { TicketDTO, TicketCreateRequest } from '../../../core/models/api.models';

@Component({
  selector: 'app-ticket-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './ticket-list.component.html',
  styleUrl: './ticket-list.component.css'
})
export class TicketListComponent implements OnInit {
  tickets: TicketDTO[] = [];
  filteredTickets: TicketDTO[] = [];
  statusFilter = '';
  isLoading = true;
  error: string | null = null;
  
  currentPage = 0;
  pageSize = 10;
  
  newTicket = { customerName: '', customerPhone: '', deviceInfo: '', issueDesc: '' };
  isCreating = false;

  private ticketService = inject(TicketService);

  ngOnInit() {
    this.loadTickets();
  }

  loadTickets() {
    this.isLoading = true;
    this.error = null;
    this.ticketService.getTickets().subscribe({
      next: (data) => {
        this.tickets = data.content;
        this.applyFilter();
        this.isLoading = false;
      },
      error: (err) => {
        this.error = 'Failed to load tickets';
        this.isLoading = false;
      }
    });
  }

  applyFilter() {
    if (this.statusFilter) {
      this.filteredTickets = this.tickets.filter(t => t.status === this.statusFilter);
    } else {
      this.filteredTickets = [...this.tickets];
    }
  }

  onFilterChange(event: any) {
    this.statusFilter = event.target.value;
    this.applyFilter();
  }

  createTicket(event: Event) {
    event.preventDefault();
    this.isCreating = true;
    
    const payload: TicketCreateRequest = {
      customerName: this.newTicket.customerName,
      customerPhone: this.newTicket.customerPhone,
      deviceInfo: this.newTicket.deviceInfo,
      issueDesc: this.newTicket.issueDesc
    };

    this.ticketService.createTicket(payload).subscribe({
      next: (created) => {
        this.tickets.unshift(created);
        this.applyFilter();
        this.isCreating = false;
        
        // Reset form
        this.newTicket = { customerName: '', customerPhone: '', deviceInfo: '', issueDesc: '' };
        
        // Close modal if bootstrap is available globally
        const modalEl = document.getElementById('newTicketModal');
        if (modalEl && (window as any).bootstrap) {
          const modal = (window as any).bootstrap.Modal.getInstance(modalEl);
          if (modal) modal.hide();
        }
      },
      error: (err) => {
        this.error = 'Failed to create ticket';
        this.isCreating = false;
      }
    });
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
