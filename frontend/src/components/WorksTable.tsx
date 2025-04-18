"use client";
import { Work } from "@/types/dto";
import { Link, Chip } from "@mui/material";
import { DataGrid } from "@mui/x-data-grid";

export default function WorksTable({ sortedWorks }: { sortedWorks: Work[] }) {
  return (
    <DataGrid
      rows={sortedWorks.map((work) => ({
        id: work.id,
        student: work.student.name,
        theme: work.theme || "—",
        supervisor: work.supervisor?.name || "—",
        classroomLink: work.classroomLink,
        submissionLink: work.googleSubmissionLink,
        isCorrectStudent: work.correctStudent,
        isCorrectSupervisor: work.correctSupervisor,
        isCorrectTheme: work.correctTheme,
      }))}
      columns={[
        { field: "student", headerName: "Студент", flex: 1 },
        { field: "theme", headerName: "Тема", flex: 2 },
        { field: "supervisor", headerName: "Керівник", flex: 1 },
        {
          field: "classroomLink",
          headerName: "Файл",
          flex: 1,
          renderCell: (params) =>
            params.value ? (
              <Link
                href={params.value}
                target="_blank"
                rel="noopener noreferrer"
              >
                Переглянути
              </Link>
            ) : (
              "—"
            ),
        },
        {
          field: "submissionLink",
          headerName: "Здача (Submission)",
          flex: 1,
          renderCell: (params) =>
            params.value ? (
              <Link
                href={params.value}
                target="_blank"
                rel="noopener noreferrer"
              >
                Здача
              </Link>
            ) : (
              "—"
            ),
        },
        {
          field: "isCorrectStudent",
          headerName: "Студент ✓",
          flex: 0.5,
          renderCell: (params) => (
            <Chip
              label={params.value ? "Так" : "Ні"}
              color={params.value ? "success" : "error"}
              size="small"
            />
          ),
        },
        {
          field: "isCorrectSupervisor",
          headerName: "Керівник ✓",
          flex: 0.5,
          renderCell: (params) => (
            <Chip
              label={params.value ? "Так" : "Ні"}
              color={params.value ? "success" : "error"}
              size="small"
            />
          ),
        },
        {
          field: "isCorrectTheme",
          headerName: "Тема ✓",
          flex: 0.5,
          renderCell: (params) => (
            <Chip
              label={params.value ? "Так" : "Ні"}
              color={params.value ? "success" : "error"}
              size="small"
            />
          ),
        },
      ]}
      pageSizeOptions={[5, 10, 25]}
      initialState={{
        pagination: { paginationModel: { pageSize: 10, page: 0 } },
      }}
      disableRowSelectionOnClick
    />
  );
}
