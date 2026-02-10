import { Component, Input, Output, EventEmitter, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';

// PrimeNG
import { RatingModule } from 'primeng/rating';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';

import { AuthService } from '../../services/auth.service';
import { environment } from '../../../environments/environment';

interface Rating {
  score: number;
  userId: number;
}

@Component({
  selector: 'app-rating-form',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RatingModule,
    ButtonModule,
    MessageModule,
    ToastModule
  ],
  providers: [MessageService],
  templateUrl: './rating-form.html',
  styleUrl: './rating-form.scss'
})
export class RatingForm {
  @Input() sessionUserId!: number;
  @Input() bookId!: number;
  @Input() bookRatings: Rating[] | null | undefined = [];
  @Output() ratingAdded = new EventEmitter<void>();

  ratingValue = signal<number | null>(null);
  isSubmitting = signal(false);
  errorMessage = signal<string | null>(null);

  // Check if user has already rated this book
  canRate = computed(() => {
    if (!this.authService.isLoggedIn()) return false;
    return !this.bookRatings?.some(rating => rating.userId === this.sessionUserId);
  });

  constructor(
    protected authService: AuthService,
    private http: HttpClient,
    private messageService: MessageService
  ) {}

  submitRating(): void {
    const score = this.ratingValue();

    if (score === null) {
      this.errorMessage.set('Please select a rating before submitting.');
      return;
    }

    if (!this.authService.isLoggedIn()) {
      this.errorMessage.set('You must be logged in to rate books.');
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    const apiUrl = `${environment.apiUrl}/rating/${this.bookId}`;

    this.http.post(apiUrl, { score }).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.ratingValue.set(null);
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Rating submitted successfully!',
          life: 3000
        });
        this.ratingAdded.emit(); // Notify parent to refresh
      },
      error: (error: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        console.error('Error submitting rating:', error);

        if (error.status === 401) {
          this.errorMessage.set('Unauthorized. Please log in again.');
        } else {
          this.errorMessage.set('Failed to submit the rating. Please try again.');
        }
      }
    });
  }
}
