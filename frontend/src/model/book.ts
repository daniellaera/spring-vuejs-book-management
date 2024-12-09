import type {CommentDTO} from "@/model/comment";

export interface BookDTO {
  title: string;
  image: string;
  isbn: string;
  id: number;
  description: string;
  author: string;
  genre: string;
  comments: CommentDTO[];
  createdDate: Date;
  publishedDate: Date | null; // Allow null only
}
