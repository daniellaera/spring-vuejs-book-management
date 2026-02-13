import {Component, computed, OnInit, signal} from '@angular/core';
import {Book} from '../../models/book.model';
import {ActivatedRoute, Router} from '@angular/router';
import {BookService} from '../../services/book.service';
import {ConfirmationService, MessageService, PrimeTemplate} from 'primeng/api';
import {CardModule} from 'primeng/card';
import {TagModule} from 'primeng/tag';
import {DividerModule} from 'primeng/divider';
import {CommonModule, NgOptimizedImage} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {ButtonModule} from 'primeng/button';
import {DatePickerModule} from 'primeng/datepicker';
import {ConfirmDialogModule} from 'primeng/confirmdialog';
import {ToastModule} from 'primeng/toast';
import {DialogModule} from 'primeng/dialog';
import {CommentForm} from '../../components/comment-form/comment-form';
import {AuthService} from '../../services/auth.service';
import {RatingForm} from '../../components/rating-form/rating-form';

@Component({
  selector: 'app-book-detail',
  imports: [
    CommonModule,
    NgOptimizedImage,
    FormsModule,
    CardModule,
    ButtonModule,
    DatePickerModule,
    TagModule,
    DividerModule,
    ConfirmDialogModule,
    ToastModule,
    DialogModule,
    CommentForm,
    RatingForm
  ],
  templateUrl: './book-detail.html',
  styleUrl: './book-detail.scss',
})
export class BookDetail implements OnInit {
  book = signal<Book | null>(null);
  loading = signal(true);
  borrowDates = signal<Date[] | null>(null);
  showBorrowDialog = signal(false);
  selectedBorrowDates = signal<Date[] | null>(null);
  minDate = new Date(); // Can't borrow in the past
  maxDate = new Date(new Date().setMonth(new Date().getMonth() + 3)); // Max 3 months

  isOwner = computed(() => {
    const currentBook = this.book();
    const currentUserId = this.authService.userDetails().userId;
    return currentBook?.userDTO?.id === currentUserId;
  });

  isAvailable = computed(() => this.book()?.isAvailable ?? false);

  hasBorrow = computed(() => {
    const currentBook = this.book();
    return currentBook?.borrow && !currentBook.borrow.isReturned;
  });

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookService: BookService,
    protected authService: AuthService,
    private confirmationService: ConfirmationService,
    private messageService: MessageService
  ) {}

  ngOnInit(): void {
    const bookId = this.route.snapshot.params['id'];
    if (bookId) {
      this.loadBookDetails(+bookId);
    }
  }

  openBorrowDialog(): void {
    // Check if logged in first
    if (!this.authService.isLoggedIn()) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Login Required',
        detail: 'Please log in to borrow books'
      });
      this.router.navigate(['/login']);
      return;
    }

    this.showBorrowDialog.set(true);
  }

  confirmBorrow(): void {
    const dates = this.selectedBorrowDates();
    const currentBook = this.book();

    if (!dates || dates.length !== 2 || !currentBook) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Invalid Dates',
        detail: 'Please select a valid date range'
      });
      return;
    }

    this.bookService.borrowBook(currentBook.id, dates[0], dates[1]).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Book borrowed successfully!'
        });
        this.showBorrowDialog.set(false);
        this.loadBookDetails(currentBook.id); // Reload book data
      },
      error: (error) => {
        console.error('Error borrowing book:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to borrow book'
        });
      }
    });
  }

  loadBookDetails(bookId: number): void {
    this.loading.set(true);

    this.bookService.getBookById(bookId).subscribe({
      next: (book) => {
        this.book.set(book);

        // Set borrow dates if exists
        if (book.borrow && !book.borrow.isReturned) {
          this.borrowDates.set([
            new Date(book.borrow.borrowStartDate),
            new Date(book.borrow.borrowEndDate)
          ]);
        }

        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading book:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to load book details'
        });
        this.loading.set(false);
      }
    });
  }

  confirmDelete(): void {
    this.confirmationService.confirm({
      message: 'Do you want to delete this book?',
      header: 'Delete Confirmation',
      icon: 'pi pi-info-circle',
      acceptButtonStyleClass: 'p-button-danger p-button-text',
      rejectButtonStyleClass: 'p-button-text p-button-text',
      acceptIcon: 'none',
      rejectIcon: 'none',
      accept: () => {
        this.deleteBook();
      }
    });
  }

  deleteBook(): void {
    const currentBook = this.book();
    if (!currentBook?.id) return;

    this.bookService.deleteBook(currentBook.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Book deleted successfully'
        });
        setTimeout(() => this.router.navigate(['/']), 1500);
      },
      error: (error) => {
        console.error('Error deleting book:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to delete book'
        });
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/']).then(r => console.info('navigated->>>', r));
  }

  formatDate(date: Date | string | null | undefined): string {
    if (!date) return 'Not available';

    const parsedDate = new Date(date);
    if (isNaN(parsedDate.getTime())) return 'Invalid date';

    return parsedDate.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  getAvailabilityTag(): { severity: 'success' | 'danger', text: string } {
    return this.isAvailable()
      ? { severity: 'success', text: 'Available' }
      : { severity: 'danger', text: 'Borrowed' };
  }

  getRatingStars(rating: number): string {
    return '⭐'.repeat(Math.round(rating));
  }

  protected reloadBook(): void {
    const currentBookId = this.book()?.id;
    if (currentBookId) {
      this.loadBookDetails(currentBookId);
    }
  }

  protected isBorrower = computed(() => {
    const currentBook = this.book();
    const currentUserId = this.authService.userDetails().userId;
    return currentBook?.borrow
      && !currentBook.borrow.isReturned
      && currentBook.borrow.userId === currentUserId;
  });

  protected confirmReturn(): void {
    this.confirmationService.confirm({
      message: 'Do you want to return this book?',
      header: 'Return Confirmation',
      icon: 'pi pi-undo',
      acceptButtonStyleClass: 'p-button-warning p-button-text',
      rejectButtonStyleClass: 'p-button-text',
      acceptIcon: 'none',
      rejectIcon: 'none',
      accept: () => {
        this.returnBook();
      }
    });
  }

  private returnBook(): void {
    const currentBook = this.book();
    if (!currentBook?.id) return;

    this.bookService.returnBook(currentBook.id).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Book returned successfully!'
        });
        this.loadBookDetails(currentBook.id);
      },
      error: (error) => {
        console.error('Error returning book:', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to return book'
        });
      }
    });
  }
}
