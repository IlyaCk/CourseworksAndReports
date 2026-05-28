import React from "react";
import {
  List,
  Datagrid,
  TextField,
  Edit,
  Create,
  SimpleForm,
  TextInput,
  SelectInput,
  BooleanInput,
  DateTimeInput,
  ReferenceInput,
  ReferenceField,
  SelectField,
  required,
  ListProps,
  EditProps,
  CreateProps,
  BooleanField,
} from "react-admin";

const notificationTypeChoices = [
  { id: "WORK_CREATED", name: "Створено роботу" },
  { id: "WORK_UPDATED", name: "Оновлено роботу" },
  { id: "CHECK_RESULT", name: "Результат перевірки" },
  { id: "UNDER_REVIEW", name: "На перевірці" },
  { id: "REPORT_ADDED", name: "Додано звіт" },
  { id: "WORK_ABORTED", name: "Роботу відхилено" },
];

export const NotificationList = (props: ListProps) => (
  <List {...props}>
    <Datagrid rowClick="edit">
      <TextField source="id" />
      <TextField source="message" />
      <BooleanField source="read" label="Прочитано" />
      <SelectField source="type" choices={notificationTypeChoices} />
      <TextField source="targetUrl" label="Цільове посилання" />
      <ReferenceField source="recipient.id" reference="users" label="Отримувач">
        <TextField source="name" />
      </ReferenceField>
    </Datagrid>
  </List>
);

const NotificationFormFields = () => (
  <>
    <TextInput source="message" validate={[required()]} fullWidth />
    <SelectInput
      source="type"
      choices={notificationTypeChoices}
      validate={[required()]}
    />
    <BooleanInput source="read" />
    <TextInput source="targetUrl" fullWidth />
    <ReferenceInput
      source="recipient.id"
      reference="users"
      label="Отримувач"
      isRequired={true}
    >
      <SelectInput optionText="name" />
    </ReferenceInput>
    <DateTimeInput source="createdAt" disabled />
  </>
);

export const NotificationEdit = (props: EditProps) => (
  <Edit {...props}>
    <SimpleForm>
      <TextInput disabled source="id" />
      <NotificationFormFields />
    </SimpleForm>
  </Edit>
);

export const NotificationCreate = (props: CreateProps) => (
  <Create {...props}>
    <SimpleForm>
      <NotificationFormFields />
    </SimpleForm>
  </Create>
);
