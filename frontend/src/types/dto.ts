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
  topicDistributionLink: string;
  googleClassId: string;
  googleClassLink: string;
  googleAssignmentId: string;
  googleAssignmentLink: string;
  updating: boolean;
  type: string;
  students: User[];
  supervisors: User[];
  works: Work[];
}

export interface DisciplineDTO {
  name: string;
  year: number;
  works: Work[];
}

export interface Work {
  id: number;
  theme: string | null;
  classroomLink: string;
  fullTextLink: string | null;
  checkTextLink: string | null;
  googleSubmissionLink: string;
  correctStudent: boolean;
  correctSupervisor: boolean;
  correctTheme: boolean;
  themeDifference: string;
  type: string;
  student: User;
  supervisor: User | null;
  reviewer: User | null;
  plagiarismReport: null;
}
