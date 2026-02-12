import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import {Observable} from 'rxjs';
import {Book} from '../models/book.model';
import {environment} from '../../environments/environment';
import {CreateBookDTO} from '../models/create-book.model';
import {PageResponse} from '../models/page-response.model';

@Injectable({
  providedIn: 'root'
})
export class BookService {
  private apiUrl = `${environment.apiUrl}/book`;

  constructor(private http: HttpClient) {}

  getAllBooks(
    page: number = 0,
    size: number = 10,
    sortField: string = 'createdDate',
    sortOrder: number = -1,
    keyword: string = ''
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
    }

    return this.http.get<PageResponse<Book>>(this.apiUrl, { params });
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

  deleteBook(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  createBook(book: CreateBookDTO): Observable<Book> {
    return this.http.post<Book>(this.apiUrl, book);
  }
}
