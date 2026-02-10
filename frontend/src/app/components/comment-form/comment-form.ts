import { Component, Input, Output, EventEmitter, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';

// PrimeNG
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';

import { AuthService } from '../../services/auth.service';
import { environment } from '../../../environments/environment';
import {Textarea} from 'primeng/textarea';

@Component({
  selector: 'app-comment-form',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    MessageModule,
    Textarea
  ],
  templateUrl: './comment-form.html',
  styleUrl: './comment-form.scss'
})
export class CommentForm {
  @Input() bookId!: number;
  @Output() commentAdded = new EventEmitter<void>();

  commentContent = signal('');
  isSubmitting = signal(false);
  errorMessage = signal<string | null>(null);

  constructor(
    public authService: AuthService,
    private http: HttpClient
  ) {}

  submitComment(): void {
    const content = this.commentContent().trim();

    // Validation
    if (!content) {
      this.errorMessage.set('Comment cannot be empty.');
      return;
    }

    if (content.length > 500) {
      this.errorMessage.set('Comment cannot exceed 500 characters.');
      return;
    }

    if (!this.authService.isLoggedIn()) {
      this.errorMessage.set('You must be logged in to post a comment.');
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    const apiUrl = `${environment.apiUrl}/comment/${this.bookId}`;

    this.http.post(apiUrl, { content }).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.commentContent.set('');
        this.commentAdded.emit(); // Notify parent to refresh comments
      },
      error: (error: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        console.error('Error submitting comment:', error);

        if (error.status === 401) {
          this.errorMessage.set('Unauthorized. Please log in again.');
        } else {
          this.errorMessage.set('Failed to submit the comment. Please try again later.');
        }
      }
    });
  }

  getButtonLabel(): string {
    if (this.isSubmitting()) return 'Submitting...';
    if (!this.authService.isLoggedIn()) return 'Login to Comment';
    return 'Post Comment';
  }

  isSubmitDisabled(): boolean {
    return (
      !this.commentContent().trim() ||
      this.isSubmitting() ||
      !this.authService.isLoggedIn()
    );
  }
}
