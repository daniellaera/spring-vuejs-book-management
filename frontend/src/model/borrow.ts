export interface BorrowDTO {
  id: number;
  borrowStartDate: Date;
  borrowEndDate: Date;
  isReturned: boolean;
  bookId: number;
  userId: number;
}
