import { Role } from "@/types/dto";
import React from "react";
import {
  List,
  Datagrid,
  TextField,
  EmailField,
  SingleFieldList,
  ChipField,
  Edit,
  Create,
  SimpleForm,
  TextInput,
  ReferenceArrayInput,
  SelectArrayInput,
  required,
  ListProps,
  ArrayField,
  EditProps,
  CreateProps,
} from "react-admin";

export const UserList = (props: ListProps) => (
  <List {...props}>
    <Datagrid rowClick="edit">
      <TextField source="id" />
      <TextField source="name" />
      <EmailField source="email" />
      <ArrayField source="roles">
        <SingleFieldList linkType={false}>
          <ChipField source="name" />
        </SingleFieldList>
      </ArrayField>
    </Datagrid>
  </List>
);

export const UserEdit = (props: EditProps) => (
  <Edit {...props}>
    <SimpleForm>
      <TextInput disabled source="id" />
      <TextInput source="name" validate={[required()]} />
      <TextInput source="email" validate={[required()]} />
      <ReferenceArrayInput source="roles" reference="roles">
        <SelectArrayInput
          optionText="name"
          format={(value) =>
            Array.isArray(value) ? value.map((role: Role) => role.id) : []
          }
          parse={(value) => value.map((id: number) => ({ id }))}
        />
      </ReferenceArrayInput>
    </SimpleForm>
  </Edit>
);

export const UserCreate = (props: CreateProps) => (
  <Create {...props}>
    <SimpleForm>
      <TextInput source="name" validate={[required()]} />
      <TextInput source="email" validate={[required()]} />
      <ReferenceArrayInput source="roles" reference="roles">
        <SelectArrayInput
          optionText="name"
          format={(value) =>
            Array.isArray(value) ? value.map((role: Role) => role.id) : []
          }
          parse={(value) => value.map((id: number) => ({ id }))}
        />
      </ReferenceArrayInput>
    </SimpleForm>
  </Create>
);
