export type Collection = {
  id: string;
  name: string;
  taskId: string;
  isUpdating: boolean;
  updatedAt: string;
};

export type Attachment = {
  id: string;
  studentName: string;
  title: string;
  link: string;
  isCoursework: boolean;
};

export interface User {
  email: number;
  name: string;
  picture: string;
}
