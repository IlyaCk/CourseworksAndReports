import { User } from "@/types/dto";
import React from "react";
import {
  List,
  Datagrid,
  TextField,
  SingleFieldList,
  ChipField,
  Edit,
  Create,
  SimpleForm,
  TextInput,
  ReferenceArrayInput,
  SelectArrayInput,
  required,
  NumberInput,
  ListProps,
  EditProps,
  CreateProps,
  ArrayField,
} from "react-admin";

export const DisciplineList = (props: ListProps) => (
  <List {...props}>
    <Datagrid rowClick="edit">
      <TextField source="id" />
      <TextField source="name" />
      <TextField source="year" />
      <TextField source="topicDistributionLink" label="Topic Distribution" />
      <ArrayField source="users" label="Users">
        <SingleFieldList linkType={false}>
          <ChipField source="name" />
        </SingleFieldList>
      </ArrayField>
    </Datagrid>
  </List>
);

export const DisciplineEdit = (props: EditProps) => (
  <Edit {...props}>
    <SimpleForm>
      <TextInput disabled source="id" />
      <TextInput source="name" validate={[required()]} />
      <NumberInput source="year" validate={[required()]} />
      <TextInput source="topicDistributionLink" label="Topic Distribution" />
      <ReferenceArrayInput source="users" reference="users" label="Users">
        <SelectArrayInput
          optionText="name"
          format={(value) => value?.map((user: User) => user.id)}
          parse={(value) => value.map((id: number) => ({ id }))}
        />
      </ReferenceArrayInput>
    </SimpleForm>
  </Edit>
);

export const DisciplineCreate = (props: CreateProps) => (
  <Create {...props}>
    <SimpleForm>
      <TextInput source="name" validate={[required()]} />
      <NumberInput source="year" validate={[required()]} />
      <TextInput source="topicDistributionLink" label="Topic Distribution" />
      <ReferenceArrayInput source="users" reference="users" label="Users">
        <SelectArrayInput
          optionText="name"
          format={(value) => value?.map((user: User) => user.id)}
          parse={(value) => value.map((id: number) => ({ id }))}
        />
      </ReferenceArrayInput>
    </SimpleForm>
  </Create>
);
