import React from "react";
import {
  List,
  Datagrid,
  TextField,
  TextInput,
  SimpleForm,
  Edit,
  Create,
  ListProps,
  EditProps,
  CreateProps,
  required,
} from "react-admin";

export const PlagiarismReportList = (props: ListProps) => (
  <List {...props}>
    <Datagrid rowClick="edit">
      <TextField source="id" />
      <TextField source="fullReportLink" label="Full Report Link" />
      <TextField source="shortReportLink" label="Short Report Link" />
    </Datagrid>
  </List>
);

export const PlagiarismReportEdit = (props: EditProps) => (
  <Edit {...props}>
    <SimpleForm>
      <TextInput disabled source="id" />
      <TextInput
        source="fullReportLink"
        label="Full Report Link"
        validate={[required()]}
        fullWidth
      />
      <TextInput
        source="shortReportLink"
        label="Short Report Link"
        validate={[required()]}
        fullWidth
      />
    </SimpleForm>
  </Edit>
);

export const PlagiarismReportCreate = (props: CreateProps) => (
  <Create {...props}>
    <SimpleForm>
      <TextInput
        source="fullReportLink"
        label="Full Report Link"
        validate={[required()]}
        fullWidth
      />
      <TextInput
        source="shortReportLink"
        label="Short Report Link"
        validate={[required()]}
        fullWidth
      />
    </SimpleForm>
  </Create>
);
