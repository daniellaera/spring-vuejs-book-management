export interface BorrowDTO {
  bookId: number;
  userId: number;
  isReturned: boolean;
  borrowStartDate: Date;
  borrowEndDate: Date;
}
