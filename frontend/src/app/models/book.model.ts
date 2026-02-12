import { CommentDTO } from './comment.model';
import { RatingDTO } from './rating.model';
import { BorrowDTO } from './borrow.model';
import { UserDTO } from './user.model';

export interface Book {
  id: number;
  title: string;
  author: string;
  isbn: string;
  description?: string;
  genre?: string;
  isAvailable: boolean;
  averageRating: number;
  publishedDate?: Date;
  createdDate?: Date;
  image?: string;

  // relationships
  comments?: CommentDTO[];
  ratings?: RatingDTO[];
  borrow?: BorrowDTO | null;
  userDTO?: UserDTO;
}
