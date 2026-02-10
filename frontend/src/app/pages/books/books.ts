import { ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

// PrimeNG
import { Table, TableLazyLoadEvent, TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TagModule } from 'primeng/tag';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { CardModule } from 'primeng/card';
import { TooltipModule } from 'primeng/tooltip';

import { BookService } from '../../services/book.service';
import { Book } from '../../models/book.model';

@Component({
  selector: 'app-books-component',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    ButtonModule,
    InputTextModule,
    TagModule,
    IconFieldModule,
    InputIconModule,
    CardModule,
    TooltipModule
  ],
  templateUrl: './books.html',
  styleUrl: './books.scss'
})
export class Books implements OnInit {
  // ✅ Added #dt reference to match ViewChild
  @ViewChild('dt') table?: Table;

  books: Book[] = [];
  loading = true;
  totalRecords = 0;

  first = 0;
  rows = 10;
  searchKeyword = '';

  constructor(
    private bookService: BookService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {}

  /**
   * Primary data loader. Triggered by:
   * 1. Initial table init
   * 2. Paging/Sorting/Filtering via UI
   * 3. Calling this.table.reset()
   */
  loadBooks(event: TableLazyLoadEvent): void {
    this.loading = true;

    // Sync local state with table event
    this.first = event.first ?? 0;
    this.rows = event.rows ?? 10;

    const page = Math.floor(this.first / this.rows);
    const sortField = (event.sortField as string) ?? 'createdDate';
    const sortOrder = event.sortOrder ?? -1;

    this.bookService
      .getAllBooks(page, this.rows, sortField, sortOrder, this.searchKeyword)
      .subscribe({
        next: (response) => {
          this.books = response.content;
          this.totalRecords = response.totalElements;
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          console.error('❌ Error loading books:', error);
          this.loading = false;
          this.books = [];
          this.totalRecords = 0;
          this.cdr.detectChanges();
        }
      });
  }

  onSearch(): void {
    if (this.table) {
      // ✅ reset() automatically sets first=0 and triggers onLazyLoad
      this.table.reset();
    } else {
      // Fallback if table isn't ready
      this.loadBooks({ first: 0, rows: this.rows });
    }
  }

  clearSearch(): void {
    this.searchKeyword = '';
    if (this.table) {
      this.table.reset();
    }
  }

  getAvailabilitySeverity(isAvailable: boolean): 'success' | 'danger' {
    return isAvailable ? 'success' : 'danger';
  }

  viewBook(book: Book): void {
    this.router.navigate(['/books', book.id]);
  }

  editBook(book?: Book): void {
    if (book) {
      this.router.navigate(['/books', book.id, 'edit']);
    } else {
      this.router.navigate(['/create-book']);
    }
  }

  deleteBook(book: Book): void {
    if (confirm(`Are you sure you want to delete "${book.title}"?`)) {
      this.bookService.deleteBook(book.id).subscribe({
        next: () => {
          // If we deleted the last item on a page, go back one page
          if (this.books.length === 1 && this.first > 0) {
            this.first = Math.max(0, this.first - this.rows);
          }
          this.table?.reset();
        },
        error: (err) => console.error('❌ Error deleting book:', err)
      });
    }
  }
}
