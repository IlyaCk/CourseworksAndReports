"use client";
import { Work } from "@/types/dto";
import { Link, Chip } from "@mui/material";
import { DataGrid } from "@mui/x-data-grid";
import { usePathname, useRouter } from "next/navigation";

export default function WorksTable({ sortedWorks }: { sortedWorks: Work[] }) {
  const pathname = usePathname();
  const router = useRouter();

  const getMatchLabel = (value: string): string => {
    switch (value) {
      case "HIGH":
        return "Висока";
      case "MEDIUM":
        return "Середня";
      case "LOW":
        return "Низька";
      case "NOT_MATCHED":
      default:
        return "Немає";
    }
  };

  const getMatchColor = (
    value: string
  ): "success" | "warning" | "default" | "error" => {
    switch (value) {
      case "HIGH":
        return "success";
      case "MEDIUM":
        return "warning";
      case "LOW":
        return "default";
      case "NOT_MATCHED":
      default:
        return "error";
    }
  };

  return (
    <>
      <DataGrid
        rows={sortedWorks.map((work) => ({
          id: work.id,
          student: work.student.name,
          theme: work.theme || "—",
          supervisor: work.supervisor?.name || "—",
          studentGroup: work?.studentGroup || "—",
          fullTextLink: work.fullTextLink,
          shortTextLink: work.shortTextLink,
          submissionLink: work.googleSubmissionLink,
          isCorrectStudent: work.isCorrectStudent,
          isCorrectSupervisor: work.isCorrectSupervisor,
          isCorrectTheme: work.isCorrectTheme,
        }))}
        columns={[
          { field: "student", headerName: "Студент", flex: 1 },
          { field: "theme", headerName: "Тема", flex: 2 },
          { field: "supervisor", headerName: "Керівник", flex: 1 },
          { field: "studentGroup", headerName: "Група", flex: 1 },
          {
            field: "fullTextLink",
            headerName: "Файл (повний)",
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
            field: "shortTextLink",
            headerName: "Файл (без додатків)",
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
                label={getMatchLabel(params.value)}
                color={getMatchColor(params.value)}
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
                label={getMatchLabel(params.value)}
                color={getMatchColor(params.value)}
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
                label={getMatchLabel(params.value)}
                color={getMatchColor(params.value)}
                size="small"
              />
            ),
          },
        ]}
        pageSizeOptions={[5, 10, 25, 100]}
        initialState={{
          pagination: { paginationModel: { pageSize: 10, page: 0 } },
        }}
        disableRowSelectionOnClick
        onRowClick={(params) =>
          router.push(pathname + `/works/${params.row.id}`)
        }
        sx={{
          ".MuiDataGrid-cell:focus": {
            outline: "none",
          },
          "& .MuiDataGrid-row:hover": {
            cursor: "pointer",
          },
        }}
      />
    </>
  );
}
