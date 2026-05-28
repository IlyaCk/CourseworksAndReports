import {
  List,
  Datagrid,
  TextField,
  ReferenceField,
  Edit,
  Create,
  SimpleForm,
  TextInput,
  ReferenceInput,
  SelectInput,
  required,
  DateTimeInput,
  ListProps,
  EditProps,
  CreateProps,
} from "react-admin";

export const WorkList = (props: ListProps) => (
  <List {...props}>
    <Datagrid rowClick="edit">
      <TextField source="id" />
      <TextField source="theme" />
      <TextField source="classroomLink" sx={{ wordBreak: "break-all" }} />
      <TextField source="fullTextLink" sx={{ wordBreak: "break-all" }} />
      <TextField source="studentGroup" />
      <ReferenceField
        source="student.id"
        reference="users"
        label="Student"
        link={false}
      >
        <TextField source="name" />
      </ReferenceField>
      <ReferenceField
        source="supervisor.id"
        reference="users"
        label="Supervisor"
        link={false}
      >
        <TextField source="name" />
      </ReferenceField>
      <ReferenceField
        source="reviewer.id"
        reference="users"
        label="Reviewer"
        link={false}
      >
        <TextField source="name" />
      </ReferenceField>
      <ReferenceField
        source="plagiarismReport.id"
        reference="plagiarism-reports"
        label="Plagiarism Report ID"
        link={false}
      >
        <TextField source="id" />
      </ReferenceField>
      <TextField source="state" />
      <TextField source="plagiarismCheckStatus" />
    </Datagrid>
  </List>
);

const enumChoices = (enumObj: Record<string, string>) =>
  Object.keys(enumObj).map((key) => ({ id: key, name: key }));

const WorkFormFields = () => (
  <>
    <TextInput source="theme" validate={[required()]} fullWidth />
    <TextInput source="classroomLink" fullWidth />
    <TextInput source="fullTextLink" fullWidth />
    <TextInput source="shortTextLink" fullWidth />
    <TextInput source="studentGroup" />
    <TextInput source="topicDistributionLink" />
    <DateTimeInput source="turnInDate" />
    <TextInput source="googleSubmissionLink" />
    <SelectInput
      source="isCorrectStudent"
      choices={enumChoices({
        NOT_MATCHED: "",
        LOW: "",
        MEDIUM: "",
        HIGH: "",
      })}
    />
    <SelectInput
      source="isCorrectSupervisor"
      choices={enumChoices({
        NOT_MATCHED: "",
        LOW: "",
        MEDIUM: "",
        HIGH: "",
      })}
    />
    <SelectInput
      source="isCorrectTheme"
      choices={enumChoices({
        NOT_MATCHED: "",
        LOW: "",
        MEDIUM: "",
        HIGH: "",
      })}
    />
    <SelectInput
      source="type"
      choices={enumChoices({ COURSEWORK: "", QUALIFICATION_WORK: "" })}
      validate={[required()]}
    />
    <SelectInput
      source="state"
      choices={enumChoices({
        NEW: "",
        UPDATE: "",
        ONLY_DATA_UPDATE: "",
        DEFAULT: "",
      })}
    />
    <SelectInput
      source="plagiarismCheckStatus"
      choices={enumChoices({ NOT_CHECKED: "", IN_PROGRESS: "", CHECKED: "" })}
    />
    <ReferenceInput source="student.id" reference="users" label="Student">
      <SelectInput optionText="name" />
    </ReferenceInput>
    <TextInput source="rawStudentName" />
    <ReferenceInput source="supervisor.id" reference="users" label="Supervisor">
      <SelectInput optionText="name" />
    </ReferenceInput>
    <TextInput source="rawSupervisorName" />
    <ReferenceInput source="reviewer.id" reference="users" label="Reviewer">
      <SelectInput optionText="name" />
    </ReferenceInput>
    <ReferenceInput
      source="plagiarismReport.id"
      reference="plagiarism-reports"
      label="Plagiarism Report ID"
    >
      <SelectInput optionText="id" />
    </ReferenceInput>
    <TextInput source="themeDifference" multiline fullWidth />
    <TextInput source="studentDifference" multiline fullWidth />
    <TextInput source="supervisorDifference" multiline fullWidth />
    <TextInput source="ministryDifference" multiline fullWidth />
    <TextInput source="heidifference" multiline fullWidth />
    <TextInput source="departmentDifference" multiline fullWidth />
    <TextInput source="groupDifference" multiline fullWidth />
    <TextInput source="cityYearDifference" multiline fullWidth />
  </>
);

export const WorkEdit = (props: EditProps) => (
  <Edit {...props}>
    <SimpleForm>
      <TextInput disabled source="id" />
      <WorkFormFields />
    </SimpleForm>
  </Edit>
);

export const WorkCreate = (props: CreateProps) => (
  <Create {...props}>
    <SimpleForm>
      <WorkFormFields />
    </SimpleForm>
  </Create>
);
