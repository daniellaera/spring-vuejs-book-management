import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import {Observable, of, tap} from 'rxjs';
import {Book, PageResponse} from '../models/book.model';
import {environment} from '../../environments/environment';
import {CreateBookDTO} from '../models/create-book.model';

@Injectable({
  providedIn: 'root'
})
export class BookService {
  private apiUrl = `${environment.apiUrl}/book`;

  constructor(private http: HttpClient) {
    // Log environment on service initialization
    console.log('📚 BookService initialized');
    console.log('🌍 Environment:', environment.production ? 'PRODUCTION' : 'DEVELOPMENT');
    console.log('🔗 API URL:', this.apiUrl);
  }

  getAllBooks(
    page: number = 0,
    size: number = 10,
    sortField: string = 'createdDate',
    sortOrder: number = -1,
    keyword: string = '' // 1. Added 5th argument
  ): Observable<PageResponse<Book>> {
    const sortDirection = sortOrder === 1 ? 'asc' : 'desc';
    const sort = `${sortField},${sortDirection}`;

    // 2. Initialize params
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    // 3. Add search keyword only if it's not empty
    if (keyword && keyword.trim() !== '') {
      params = params.set('search', keyword.trim());
      // Note: Ensure 'search' matches the parameter name your backend expects (e.g., 'query', 'title', etc.)
    }

    console.log(`📖 Fetching books - Page: ${page}, Size: ${size}, Sort: ${sort}, Keyword: ${keyword}`);

    return this.http.get<PageResponse<Book>>(this.apiUrl, { params }).pipe(
      tap(response => console.log('✅ Books loaded:', response.content.length, 'books'))
    );
  }

  getBookById(id: number): Observable<Book> {
    return this.http.get<Book>(`${this.apiUrl}/${id}`);
  }

  borrowBook(bookId: number, borrowStartDate: Date, borrowEndDate: Date): Observable<any> {
    const body = {
      bookId,
      borrowStartDate: borrowStartDate.toISOString().split('T')[0], // Format: YYYY-MM-DD
      borrowEndDate: borrowEndDate.toISOString().split('T')[0]
    };

    return this.http.post(`${environment.apiUrl}/borrow/${bookId}`, body);
  }

  searchBooks(keyword: string, page: number = 0, size: number = 10): Observable<Book[]> {
    const params = new HttpParams()
      .set('keyword', keyword)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Book[]>(`${this.apiUrl}/search`, { params });
  }

  deleteBook(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  createBook(book: CreateBookDTO): Observable<Book> {
    return this.http.post<Book>(this.apiUrl, book);
  }

  // Mock data for testing without backend
  getMockBooks(page: number = 0, size: number = 10): Observable<PageResponse<Book>> {
    const mockBooks: Book[] = [
      { id: 1, title: 'The Great Gatsby', author: 'F. Scott Fitzgerald', isbn: '978-0-7432-7356-5', isAvailable: true, genre: 'Fiction', averageRating: 4.5 },
      { id: 2, title: '1984', author: 'George Orwell', isbn: '978-0-452-28423-4', isAvailable: false, genre: 'Fiction', averageRating: 4.8 },
      { id: 3, title: 'To Kill a Mockingbird', author: 'Harper Lee', isbn: '978-0-06-112008-4', isAvailable: true, genre: 'Fiction', averageRating: 4.7 },
      { id: 4, title: 'The Hobbit', author: 'J.R.R. Tolkien', isbn: '978-0-547-92822-7', isAvailable: true, genre: 'Fantasy', averageRating: 4.9 },
      { id: 5, title: 'Harry Potter', author: 'J.K. Rowling', isbn: '978-0-439-70818-8', isAvailable: false, genre: 'Fantasy', averageRating: 4.8 },
    ];

    const start = page * size;
    const end = start + size;
    const content = mockBooks.slice(start, end);

    return of({
      content,
      pageNumber: page,
      pageSize: size,
      totalElements: mockBooks.length,
      totalPages: Math.ceil(mockBooks.length / size),
      first: page === 0,
      last: end >= mockBooks.length
    });
  }
}
