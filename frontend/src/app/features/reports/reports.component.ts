import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TicketService } from '../../core/services/ticket.service';
import { TicketDTO } from '../../core/models/api.models';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.css'
})
export class ReportsComponent implements OnInit {
  breaches: TicketDTO[] = [];
  isLoading = true;
  error: string | null = null;

  private ticketService = inject(TicketService);

  ngOnInit() {
    this.loadReports();
  }

  loadReports() {
    this.isLoading = true;
    this.error = null;
    this.ticketService.getSlaBreaches().subscribe({
      next: (data) => {
        this.breaches = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.error = 'Failed to load reports';
        this.isLoading = false;
      }
    });
  }

  getBreachDuration(ticket: TicketDTO): string {
    const createdDate = new Date(ticket.createdAt);
    const deadline = new Date(createdDate.getTime() + (24 * 60 * 60 * 1000));
    const endTarget = new Date(); // assuming unresolved
    
    if (endTarget > deadline) {
        const diffMs = endTarget.getTime() - deadline.getTime();
        const diffHrs = Math.floor(diffMs / (1000 * 60 * 60));
        return `${diffHrs} hours`;
    }
    return '-';
  }
}
