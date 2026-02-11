import {computed, effect, Injectable, signal} from '@angular/core';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';
import {Observable, tap} from 'rxjs';
import {UserDetails} from '../models/user-details.model';
import {AuthResponse} from '../models/auth-response.model';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;

  // Signals for reactive state
  private tokenExpiration = signal<number | null>(null);
  timeLeft = signal(0);
  userDetails = signal<UserDetails>({
    fullName: null,
    firstName: null,
    lastName: null,
    githubId: null,
    userId: null
  });
  // Computed property for login status
  isLoggedIn = computed(() => {
    const exp = this.tokenExpiration();
    return exp !== null && exp > Date.now();
  });

  private sessionTimer: any = null;

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    // Check session on service init
    this.checkSession();

    // Auto-cleanup on logout
    effect(() => {
      if (!this.isLoggedIn() && this.sessionTimer) {
        this.resetSession();
      }
    });
  }

  signup(email: string, password: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/signup`, { email, password });
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/signin`, { email, password })
      .pipe(
        tap(response => {
          this.setTokenExpiration(response.token);
          localStorage.setItem('auth_token', response.token);
          localStorage.setItem('username', response.username);
          this.fetchUserDetails();
        })
      );
  }

  logout(): void {
    this.resetSession();
    localStorage.removeItem('auth_token');
    localStorage.removeItem('username');
    this.router.navigate(['/login']);
  }

  private setTokenExpiration(token: string): void {
    const expiration = this.getTokenExpiration(token);
    if (expiration) {
      this.tokenExpiration.set(expiration);
      this.updateCountdown();
      this.startSessionTimer();
    }
  }

  private getTokenExpiration(token: string): number | null {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp * 1000; // Convert to milliseconds
    } catch (error) {
      console.error('Failed to parse token', error);
      return null;
    }
  }

  private updateCountdown(): void {
    const exp = this.tokenExpiration();
    if (exp) {
      const now = Date.now();
      this.timeLeft.set(Math.max(0, Math.floor((exp - now) / 1000)));
    }
  }

  private startSessionTimer(): void {
    if (this.sessionTimer) {
      clearInterval(this.sessionTimer);
    }

    this.sessionTimer = setInterval(() => {
      this.updateCountdown();
      const timeLeft = this.timeLeft();

      if (timeLeft <= 0) {
        clearInterval(this.sessionTimer);
        this.sessionTimer = null;
        this.resetSession();
      }
    }, 1000);
  }

  private resetSession(): void {
    this.tokenExpiration.set(null);
    this.timeLeft.set(0);
    this.userDetails.set({
      fullName: null,
      firstName: null,
      lastName: null,
      githubId: null,
      userId: null
    });

    if (this.sessionTimer) {
      clearInterval(this.sessionTimer);
      this.sessionTimer = null;
    }
  }

  checkSession(): void {
    const token = localStorage.getItem('auth_token');
    if (token) {
      const expiration = this.getTokenExpiration(token);

      // Check if token is already expired
      if (expiration && expiration > Date.now()) {
        this.setTokenExpiration(token);
        this.fetchUserDetails();
      } else {
        // Token expired - clear it
        this.resetSession();
        localStorage.removeItem('auth_token');
        localStorage.removeItem('username');
      }
    }
  }

  fetchUserDetails(): void {
    const token = localStorage.getItem('auth_token');
    if (!token) return;

    this.http.get<any>(`${this.apiUrl}/me`).subscribe({
      next: (details) => {
        this.userDetails.set({
          fullName: details.fullName,
          firstName: details.firstName || null,
          lastName: details.lastName || null,
          githubId: details.githubId || null,
          userId: details.userId || null
        });
      },
      error: (error) => {
        console.error('Failed to fetch user details:', error);
        this.resetSession();
      }
    });
  }

  getFullName(): string {
    const details = this.userDetails();
    return details.fullName === 'null null' || !details.fullName
      ? 'Guest'
      : details.fullName;
  }
}
