"use client";
import { dataProvider } from "@/utils/dataProvider";
import { Admin, Resource } from "react-admin";
import AdminLayout from "./AdminLayout";
import { UserCreate, UserEdit, UserList } from "./Users";
import { RoleCreate, RoleEdit, RoleList } from "./Roles";
import {
  DepartmentCreate,
  DepartmentEdit,
  DepartmentList,
} from "./Departments";
import {
  DisciplineCreate,
  DisciplineEdit,
  DisciplineList,
} from "./Disciplines";
import {
  NotificationCreate,
  NotificationEdit,
  NotificationList,
} from "./Notifications";
import {
  PlagiarismReportCreate,
  PlagiarismReportEdit,
  PlagiarismReportList,
} from "./PlagiarismReport";
import { WorkCreate, WorkEdit, WorkList } from "./Work";

const AdminApp = () => (
  <Admin dataProvider={dataProvider} layout={AdminLayout} defaultTheme="light">
    <Resource
      name="users"
      list={UserList}
      edit={UserEdit}
      create={UserCreate}
    />
    <Resource
      name="roles"
      list={RoleList}
      edit={RoleEdit}
      create={RoleCreate}
    />
    <Resource
      name="departments"
      list={DepartmentList}
      edit={DepartmentEdit}
      create={DepartmentCreate}
    />
    <Resource
      name="disciplines"
      list={DisciplineList}
      edit={DisciplineEdit}
      create={DisciplineCreate}
    />
    <Resource
      name="works"
      list={WorkList}
      edit={WorkEdit}
      create={WorkCreate}
    />
    <Resource
      name="plagiarism-reports"
      list={PlagiarismReportList}
      edit={PlagiarismReportEdit}
      create={PlagiarismReportCreate}
    />
    <Resource
      name="notifications"
      list={NotificationList}
      edit={NotificationEdit}
      create={NotificationCreate}
    />
  </Admin>
);

export default AdminApp;
