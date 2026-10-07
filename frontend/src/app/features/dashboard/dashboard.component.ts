import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TicketService } from '../../core/services/ticket.service';
import { TicketDTO } from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  tickets: TicketDTO[] = [];
  createdCount = 0;
  diagnosingCount = 0;
  waitingPartsCount = 0;
  isLoading = true;
  error: string | null = null;

  private ticketService = inject(TicketService);
  authService = inject(AuthService);

  ngOnInit() {
    this.loadDashboard();
  }

  loadDashboard() {
    this.isLoading = true;
    this.error = null;
    this.ticketService.getTickets().subscribe({
      next: (data) => {
        const allTickets = data.content;
        const isTech = this.authService.hasRole('TECH') && !this.authService.hasRole('ADMIN');
        const relevantTickets = isTech ? allTickets.filter(t => t.technician !== null) : allTickets;
        
        this.tickets = relevantTickets;
        this.createdCount = relevantTickets.filter(t => t.status === 'CREATED').length;
        this.diagnosingCount = relevantTickets.filter(t => t.status === 'DIAGNOSING').length;
        this.waitingPartsCount = relevantTickets.filter(t => t.status === 'WAITING_PARTS').length;
        this.isLoading = false;
      },
      error: (err) => {
        this.error = 'Failed to load data';
        this.isLoading = false;
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
