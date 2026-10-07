import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TicketService } from '../../../core/services/ticket.service';
import { InventoryService } from '../../../core/services/inventory.service';
import { AuthService } from '../../../core/services/auth.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { TicketDTO, UserDTO, InventoryDTO, TicketHistoryDTO } from '../../../core/models/api.models';

@Component({
  selector: 'app-ticket-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './ticket-detail.component.html',
  styleUrl: './ticket-detail.component.css'
})
export class TicketDetailComponent implements OnInit {
  ticket: TicketDTO | null = null;
  history: TicketHistoryDTO[] = [];
  technicians: UserDTO[] = [];
  inventory: InventoryDTO[] = [];
  
  newStatus = '';
  resolutionNotes = '';
  selectedTechId = '';
  consumePartId = '';
  consumeQty = 1;
  error: string | null = null;

  authService = inject(AuthService);
  private ticketService = inject(TicketService);
  private inventoryService = inject(InventoryService);
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('id'));
      if (id) {
        this.loadTicketDetails(id);
      }
    });

    if (this.authService.hasRole('ADMIN')) {
      this.http.get<UserDTO[]>(`${environment.apiUrl}/users/technicians`).subscribe({
        next: (techs) => this.technicians = techs
      });
    }

    if (this.authService.hasRole('TECH')) {
      // Need a lot of inventory so size = 100
      this.inventoryService.getInventory(0, 100).subscribe({
        next: (data) => this.inventory = data.content
      });
    }
  }

  loadTicketDetails(id: number) {
    this.ticketService.getTicketById(id).subscribe({
      next: (ticket) => {
        if (ticket) {
          this.ticket = ticket;
          this.newStatus = ticket.status;
          this.loadHistory(ticket);
        }
      },
      error: (err) => {
        this.error = 'Failed to load ticket details.';
      }
    });
  }

  loadHistory(ticket: TicketDTO) {
    this.ticketService.getTicketHistory(ticket.id).subscribe({
      next: (data) => this.history = data,
      error: (err) => console.error('Failed to load history', err)
    });
  }

  updateStatus(event: Event) {
    event.preventDefault();
    if (this.ticket) {
      this.ticketService.updateStatus(this.ticket.id, { status: this.newStatus, notes: this.resolutionNotes }).subscribe({
        next: (updatedTicket) => {
          this.ticket = updatedTicket;
          this.newStatus = updatedTicket.status;
          this.resolutionNotes = '';
          this.closeModal('updateStatusModal');
          this.loadHistory(updatedTicket);
        },
        error: (err) => {
          alert('Failed to update status.');
        }
      });
    }
  }

  assignTech(event: Event) {
    event.preventDefault();
    if (this.ticket && this.selectedTechId) {
      this.ticketService.assignTechnician(this.ticket.id, { technicianId: Number(this.selectedTechId) }).subscribe({
        next: (updatedTicket) => {
          this.ticket = updatedTicket;
          this.selectedTechId = '';
          this.closeModal('assignTechModal');
        },
        error: (err) => {
          alert('Failed to assign technician.');
        }
      });
    }
  }

  consumePart(event: Event) {
    event.preventDefault();
    if (this.ticket && this.consumePartId) {
      const id = parseInt(this.consumePartId.split(' - ')[0], 10);
      this.ticketService.consumePart(this.ticket.id, { inventoryId: id, quantity: this.consumeQty }).subscribe({
        next: (partUsed) => {
          if (!this.ticket!.parts) this.ticket!.parts = [];
          this.ticket!.parts.push(partUsed);
          
          this.consumePartId = '';
          this.consumeQty = 1;
          this.closeModal('consumePartModal');
        },
        error: (err) => {
          if (err.status === 409) {
            alert('Conflict: Insufficient stock available for this part.');
          } else {
            alert('Failed to consume part.');
          }
        }
      });
    }
  }

  closeModal(modalId: string) {
    const modalEl = document.getElementById(modalId);
    if (modalEl && (window as any).bootstrap) {
      const modal = (window as any).bootstrap.Modal.getInstance(modalEl);
      if (modal) modal.hide();
    }
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
