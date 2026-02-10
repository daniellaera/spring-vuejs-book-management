import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

// PrimeNG
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';

import { AuthService } from '../../services/auth.service';
import { BookService } from '../../services/book.service';
import {Textarea} from 'primeng/textarea';
import {CreateBookDTO} from '../../models/create-book.model';

interface BookForm {
  title: string;
  author: string;
  isbn: string;
  genre: string;
  description: string;
  publishedDate: Date | null;
}

@Component({
  selector: 'app-create-book',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CardModule,
    InputTextModule,
    ButtonModule,
    DatePickerModule,
    ToastModule,
    Textarea
  ],
  providers: [MessageService],
  templateUrl: './create-book.html',
  styleUrl: './create-book.scss'
})
export class CreateBook implements OnInit {
  book = signal<BookForm>({
    title: '',
    author: '',
    isbn: '',
    genre: '',
    description: '',
    publishedDate: null
  });

  loading = signal(false);

  constructor(
    private authService: AuthService,
    private bookService: BookService,
    private router: Router,
    private messageService: MessageService
  ) {}

  ngOnInit(): void {
    // Check if logged in
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
    }
  }

  isFormValid(): boolean {
    const bookData = this.book();
    return !!(
      bookData.title.trim() &&
      bookData.author.trim() &&
      bookData.isbn.trim() &&
      bookData.description.trim() &&
      bookData.publishedDate
    );
  }

  submitForm(): void {
    if (!this.isFormValid()) return;

    this.loading.set(true);
    const bookData = this.book();

    const createBookDTO: CreateBookDTO = {
      title: bookData.title,
      author: bookData.author,
      isbn: bookData.isbn,
      genre: bookData.genre,
      description: bookData.description,
      publishedDate: bookData.publishedDate!.toISOString()
    };

    this.bookService.createBook(createBookDTO).subscribe({
      next: (createdBook) => {
        this.loading.set(false);
        console.log('Book created:', createdBook);
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Book created successfully!',
          life: 3000
        });
        this.resetForm();
        setTimeout(() => {
          this.router.navigate(['/books']);
        }, 2000);
      },
      error: (error) => {
        this.loading.set(false);
        console.error('Error creating book', error);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to create book',
          life: 3000
        });
      }
    });
  }

  resetForm(): void {
    this.book.set({
      title: '',
      author: '',
      isbn: '',
      genre: '',
      description: '',
      publishedDate: null
    });
  }

  cancel(): void {
    this.router.navigate(['/books']);
  }
}
