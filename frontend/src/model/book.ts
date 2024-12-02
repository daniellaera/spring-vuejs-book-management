import type {CommentDTO} from "@/model/comment";

export interface BookDTO {
  title: string;
  isbn: string;
  id: number;
  description: string;
  author: string;
  genre: string;
  comments: CommentDTO[];
}
