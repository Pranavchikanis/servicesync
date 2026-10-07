import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { InventoryDTO, Page } from '../models/api.models';

@Injectable({
  providedIn: 'root'
})
export class InventoryService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/inventory`;

  getInventory(page: number = 0, size: number = 20, name?: string): Observable<Page<InventoryDTO>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
      
    if (name) {
      params = params.set('name', name);
    }

    return this.http.get<Page<InventoryDTO>>(this.baseUrl, { params });
  }
}
