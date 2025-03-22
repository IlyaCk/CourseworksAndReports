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

export interface Principal {
  attributes: {
    email: number;
    name: string;
    picture: string;
  };
  authorities: { authority: string }[];
}

export interface User {
  id: number;
  name: string;
  email: string;
  roles: Role[];
}

export interface Role {
  id: number;
  name: string;
}

export interface Department {
  id: number;
  name: string;
  responsibleUser: User;
  headUsers: User[];
  disciplines: Discipline[];
}

export interface Discipline {
  id: number;
  name: string;
  year: number;
  topic_distribution_link: string;
  users: User[];
}
