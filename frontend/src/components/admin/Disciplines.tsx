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
  SelectInput,
} from "react-admin";

export const DisciplineList = (props: ListProps) => (
  <List {...props}>
    <Datagrid rowClick="edit">
      <TextField source="id" />
      <TextField source="name" />
      <TextField source="year" />
      <TextField
        sx={{ wordBreak: "break-all" }}
        source="topicDistributionLink"
        label="Topic Distribution"
      />
      <TextField source="googleClassId" label="Google Class ID" />
      <TextField source="googleAssignmentId" label="Google Assignment ID" />
      <TextField source="type" label="Type" />
      <ArrayField source="students" label="Students">
        <SingleFieldList linkType={false}>
          <ChipField source="name" />
        </SingleFieldList>
      </ArrayField>
      <ArrayField source="supervisors" label="Supervisors">
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
      <TextInput source="googleClassId" label="Google Class ID" />
      <TextInput source="googleAssignmentId" label="Google Assignment ID" />
      <SelectInput
        source="type"
        label="Type"
        choices={[
          { id: "COURSEWORK", name: "Курсова робота" },
          { id: "QUALIFICATION_WORK", name: "Кваліфікаційна робота" },
        ]}
        validate={[required()]}
      />
      <ReferenceArrayInput source="students" reference="users" label="Students">
        <SelectArrayInput
          format={(value) => value?.map((user: User) => user.id)}
          parse={(value) => value.map((id: number) => ({ id }))}
          optionText="name"
        />
      </ReferenceArrayInput>
      <ReferenceArrayInput
        source="supervisors"
        reference="users"
        label="Supervisors"
      >
        <SelectArrayInput
          format={(value) => value?.map((user: User) => user.id)}
          parse={(value) => value.map((id: number) => ({ id }))}
          optionText="name"
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
      <TextInput source="googleClassId" label="Google Class ID" />
      <TextInput source="googleAssignmentId" label="Google Assignment ID" />
      <SelectInput
        source="type"
        label="Type"
        choices={[
          { id: "COURSEWORK", name: "Курсова робота" },
          { id: "QUALIFICATION_WORK", name: "Кваліфікаційна робота" },
        ]}
        validate={[required()]}
      />
      <ReferenceArrayInput source="students" reference="users" label="Students">
        <SelectArrayInput
          format={(value) => value?.map((user: User) => user.id)}
          parse={(value) => value.map((id: number) => ({ id }))}
          optionText="name"
        />
      </ReferenceArrayInput>
      <ReferenceArrayInput
        source="supervisors"
        reference="users"
        label="Supervisors"
      >
        <SelectArrayInput
          format={(value) => value?.map((user: User) => user.id)}
          parse={(value) => value.map((id: number) => ({ id }))}
          optionText="name"
        />
      </ReferenceArrayInput>
    </SimpleForm>
  </Create>
);
