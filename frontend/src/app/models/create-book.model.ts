export interface CreateBookDTO {
  title: string;
  author: string;
  isbn: string;
  genre: string;
  description: string;
  publishedDate: string; // ISO string format from toISOString()
}
