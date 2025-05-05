import { Discipline, User } from "@/types/dto";
import React from "react";
import {
  List,
  Datagrid,
  TextField,
  ReferenceField,
  SingleFieldList,
  ChipField,
  Edit,
  Create,
  SimpleForm,
  TextInput,
  ReferenceInput,
  SelectInput,
  ReferenceArrayInput,
  SelectArrayInput,
  required,
  ArrayField,
  ListProps,
  EditProps,
  CreateProps,
} from "react-admin";

export const DepartmentList = (props: ListProps) => (
  <List {...props}>
    <Datagrid rowClick="edit">
      <TextField source="id" />
      <TextField source="name" />
      <TextField source="ministry" />
      <TextField source="hei" />
      <TextField source="cityYear" />
      <ReferenceField
        source="responsibleUser.id"
        reference="users"
        label="Responsible User"
      >
        <TextField source="name" />
      </ReferenceField>
      <ArrayField source="headUsers" label="Department Heads">
        <SingleFieldList linkType={false}>
          <ChipField source="name" />
        </SingleFieldList>
      </ArrayField>
      <ArrayField source="disciplines" label="Disciplines">
        <SingleFieldList linkType={false}>
          <ChipField source="name" />
        </SingleFieldList>
      </ArrayField>
    </Datagrid>
  </List>
);

export const DepartmentEdit = (props: EditProps) => (
  <Edit {...props}>
    <SimpleForm>
      <TextInput disabled source="id" />
      <TextInput source="name" validate={[required()]} />
      <TextInput source="ministry" required />
      <TextInput source="hei" required />
      <TextInput source="cityYear" required />
      <ReferenceInput
        source="responsibleUser.id"
        reference="users"
        label="Responsible User"
      >
        <SelectInput validate={[required()]} optionText="name" />
      </ReferenceInput>
      <ReferenceArrayInput
        source="headUsers"
        reference="users"
        label="Department Heads"
      >
        <SelectArrayInput
          optionText="name"
          format={(value) =>
            Array.isArray(value) ? value.map((user: User) => user.id) : []
          }
          parse={(value) => value.map((id: number) => ({ id }))}
        />
      </ReferenceArrayInput>
      <ReferenceArrayInput
        source="disciplines"
        reference="disciplines"
        label="Disciplines"
      >
        <SelectArrayInput
          optionText="name"
          format={(value) =>
            Array.isArray(value)
              ? value.map((discipline: Discipline) => discipline.id)
              : []
          }
          parse={(value) => value.map((id: number) => ({ id }))}
        />
      </ReferenceArrayInput>
    </SimpleForm>
  </Edit>
);

export const DepartmentCreate = (props: CreateProps) => (
  <Create {...props}>
    <SimpleForm>
      <TextInput source="name" validate={[required()]} />
      <TextInput source="ministry" required />
      <TextInput source="hei" required />
      <TextInput source="cityYear" required />
      <ReferenceInput
        source="responsibleUser.id"
        reference="users"
        label="Responsible User"
      >
        <SelectInput validate={[required()]} optionText="name" />
      </ReferenceInput>
      <ReferenceArrayInput
        source="headUsers"
        reference="users"
        label="Department Heads"
      >
        <SelectArrayInput
          optionText="name"
          format={(value) =>
            Array.isArray(value) ? value.map((user: User) => user.id) : []
          }
          parse={(value) => value.map((id: number) => ({ id }))}
        />
      </ReferenceArrayInput>
      <ReferenceArrayInput
        source="disciplines"
        reference="disciplines"
        label="Disciplines"
      >
        <SelectArrayInput
          optionText="name"
          format={(value) =>
            Array.isArray(value)
              ? value.map((discipline: Discipline) => discipline.id)
              : []
          }
          parse={(value) => value.map((id: number) => ({ id }))}
        />
      </ReferenceArrayInput>
    </SimpleForm>
  </Create>
);
