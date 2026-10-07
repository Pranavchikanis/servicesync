import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { LoginRequest, AuthResponse, UserDTO } from '../models/api.models';
import { BehaviorSubject, Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  
  private isAuthenticatedFlag = new BehaviorSubject<boolean>(false);
  public isAuthenticated$ = this.isAuthenticatedFlag.asObservable();

  private currentUser: AuthResponse | null = null;

  constructor() {
    const token = localStorage.getItem('servicesync_jwt');
    const userStr = localStorage.getItem('servicesync_user');
    if (token && userStr) {
      this.currentUser = JSON.parse(userStr);
      this.isAuthenticatedFlag.next(true);
    }
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/login`, credentials).pipe(
      tap(response => {
        localStorage.setItem('servicesync_jwt', response.token);
        localStorage.setItem('servicesync_user', JSON.stringify(response));
        this.currentUser = response;
        this.isAuthenticatedFlag.next(true);
      })
    );
  }

  logout() {
    localStorage.removeItem('servicesync_jwt');
    localStorage.removeItem('servicesync_user');
    this.currentUser = null;
    this.isAuthenticatedFlag.next(false);
  }

  isAuthenticated(): boolean {
    return this.isAuthenticatedFlag.value;
  }

  getUsername(): string {
    return this.currentUser?.name || '';
  }

  hasRole(role: string): boolean {
    return this.currentUser?.role === role;
  }
}
