import { User, Work } from "@/types/dto";
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
  BooleanInput,
  DateTimeInput,
} from "react-admin";

const typeChoices = [
  { id: "COURSEWORK", name: "Курсова робота" },
  { id: "QUALIFICATION_WORK", name: "Кваліфікаційна робота" },
];

const visibilityChoices = [
  { id: "PRIVATE", name: "Приватна" },
  { id: "PUBLIC", name: "Публічна" },
];

const nameFormatChoices = [
  { id: "ALL", name: "Будь-який варіант" },
  { id: "SURNAME_NAME", name: "Прізвище Ім'я" },
  { id: "SURNAME_I", name: "Прізвище І." },
  { id: "SURNAME_IB", name: "Прізвище І. Б." },
  { id: "SURNAME_NAME_PATRONYMIC", name: "Прізвище Ім'я По-Батькові" },
];

const pageNumberLocationChoices = [
  { id: "TOP", name: "Вгорі" },
  { id: "BOTTOM", name: "Внизу" },
  { id: "ANY", name: "Неважливо" },
];

export const DisciplineList = (props: ListProps) => (
  <List {...props}>
    <Datagrid rowClick="edit">
      <TextField source="id" />
      <TextField source="name" />
      <TextField source="year" />
      <TextField source="type" />
      <TextField source="visibility" />
      <TextField source="googleClassId" label="Google Class ID" />
      <TextField source="googleAssignmentId" label="Google Assignment ID" />
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
      <ArrayField source="works" label="Works">
        <SingleFieldList linkType={false}>
          <ChipField source="id" />
        </SingleFieldList>
      </ArrayField>
    </Datagrid>
  </List>
);

const DisciplineFormFields = () => (
  <>
    <TextInput source="name" validate={[required()]} fullWidth />
    <NumberInput source="year" validate={[required()]} />

    <TextInput
      source="topicDistributionLink"
      label="Topic Distribution"
      fullWidth
    />
    <TextInput source="googleClassId" fullWidth />
    <TextInput source="googleClassLink" fullWidth />
    <TextInput source="googleAssignmentId" fullWidth />
    <TextInput source="googleAssignmentLink" fullWidth />
    <TextInput source="googleDriveFolderLink" fullWidth />

    <TextInput source="fileNameTemplate" fullWidth disabled />

    <SelectInput
      source="type"
      label="Type"
      choices={typeChoices}
      validate={[required()]}
    />
    <SelectInput
      source="visibility"
      label="Visibility"
      choices={visibilityChoices}
      validate={[required()]}
    />
    <SelectInput
      source="nameFormat"
      label="Name Format"
      choices={nameFormatChoices}
    />
    <SelectInput
      source="pageNumberLocation"
      label="Page Number Location"
      choices={pageNumberLocationChoices}
    />

    <DateTimeInput source="updateDate" disabled />
    <BooleanInput source="isUpdating" disabled />

    <ReferenceArrayInput source="students" reference="users" label="Students">
      <SelectArrayInput
        format={(value) =>
          Array.isArray(value) ? value.map((user: User) => user.id) : []
        }
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
        format={(value) =>
          Array.isArray(value) ? value.map((user: User) => user.id) : []
        }
        parse={(value) => value.map((id: number) => ({ id }))}
        optionText="name"
      />
    </ReferenceArrayInput>
    <ReferenceArrayInput source="works" reference="works" label="Works">
      <SelectArrayInput
        format={(value) =>
          Array.isArray(value) ? value.map((work: Work) => work.id) : []
        }
        parse={(value) => value.map((id: number) => ({ id }))}
        optionText="id"
      />
    </ReferenceArrayInput>
  </>
);

export const DisciplineEdit = (props: EditProps) => (
  <Edit {...props}>
    <SimpleForm>
      <TextInput disabled source="id" />
      <DisciplineFormFields />
    </SimpleForm>
  </Edit>
);

export const DisciplineCreate = (props: CreateProps) => (
  <Create {...props}>
    <SimpleForm>
      <DisciplineFormFields />
    </SimpleForm>
  </Create>
);
