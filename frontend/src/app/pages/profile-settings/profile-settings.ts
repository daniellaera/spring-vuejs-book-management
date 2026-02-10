import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

// PrimeNG
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';

import { AuthService } from '../../services/auth.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-profile-settings',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CardModule,
    InputTextModule,
    ButtonModule,
    MessageModule,
    ToastModule
  ],
  providers: [MessageService],
  templateUrl: './profile-settings.html',
  styleUrl: './profile-settings.scss'
})
export class ProfileSettings implements OnInit {
  firstName = signal('');
  lastName = signal('');
  email = signal('');
  loading = signal(false);

  firstNameError = signal('');
  lastNameError = signal('');

  constructor(
    public authService: AuthService,
    private http: HttpClient,
    private router: Router,
    private messageService: MessageService
  ) {}

  ngOnInit(): void {
    // Check if logged in
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    this.loadUserProfile();
  }

  loadUserProfile(): void {
    // USE CACHED DATA - no API call!
    const userDetails = this.authService.userDetails();

    this.firstName.set(userDetails.firstName || '');
    this.lastName.set(userDetails.lastName || '');

    const username = localStorage.getItem('username');
    this.email.set(username || '');
  }

  validateFirstName(): void {
    const value = this.firstName();
    if (!value) {
      this.firstNameError.set('First name is required');
    } else if (value.length < 2) {
      this.firstNameError.set('First name must be at least 2 characters');
    } else {
      this.firstNameError.set('');
    }
  }

  validateLastName(): void {
    const value = this.lastName();
    if (!value) {
      this.lastNameError.set('Last name is required');
    } else if (value.length < 2) {
      this.lastNameError.set('Last name must be at least 2 characters');
    } else {
      this.lastNameError.set('');
    }
  }

  isFormValid(): boolean {
    return !!(
      this.firstName() &&
      this.lastName() &&
      !this.firstNameError() &&
      !this.lastNameError()
    );
  }

  saveProfile(): void {
    this.validateFirstName();
    this.validateLastName();

    if (!this.isFormValid()) return;

    this.loading.set(true);
    const apiUrl = `${environment.apiUrl}/auth`;

    // PATCH expects: email, password, firstName, lastName
    const updateData = {
      email: this.email(), // Keep existing email
      password: '', // Empty password means "don't change"
      firstName: this.firstName(),
      lastName: this.lastName()
    };

    this.http.patch(apiUrl, updateData).subscribe({
      next: () => {
        this.loading.set(false);
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Profile updated successfully',
          life: 3000
        });

        // Refresh user details in auth service
        this.authService.fetchUserDetails();
      },
      error: (error) => {
        this.loading.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: error.error?.message || 'Failed to update profile',
          life: 3000
        });
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/books']);
  }
}
