import type {CommentDTO} from "@/model/comment";
import type {RatingDTO} from "@/model/rating";
import type {UserDTO} from "@/model/user";
import type {BorrowDTO} from "@/model/borrow";

export interface BookDTO {
  title: string;
  image: string;
  isbn: string;
  id: number;
  description: string;
  author: string;
  genre: string;
  userDTO?: UserDTO;
  comments: CommentDTO[];
  ratings?: RatingDTO[];
  averageRating?: number;
  createdDate: Date;
  publishedDate: Date | null; // Allow null only
  borrow?: BorrowDTO;
}
