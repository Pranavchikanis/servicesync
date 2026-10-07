import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InventoryService } from '../../../core/services/inventory.service';
import { InventoryDTO } from '../../../core/models/api.models';

@Component({
  selector: 'app-inventory-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './inventory-list.component.html',
  styleUrl: './inventory-list.component.css'
})
export class InventoryListComponent implements OnInit {
  inventory: InventoryDTO[] = [];
  isLoading = true;
  error: string | null = null;
  
  private inventoryService = inject(InventoryService);

  ngOnInit() {
    this.loadInventory();
  }

  loadInventory() {
    this.isLoading = true;
    this.error = null;
    this.inventoryService.getInventory().subscribe({
      next: (data) => {
        this.inventory = data.content;
        this.isLoading = false;
      },
      error: (err) => {
        this.error = 'Failed to load inventory';
        this.isLoading = false;
      }
    });
  }
}
