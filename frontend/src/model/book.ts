export interface BookDTO {
  title: string;
  isbn: string;
  id: number;
  description: string;
  userDTO: {
    fullName: string;
  };
}
