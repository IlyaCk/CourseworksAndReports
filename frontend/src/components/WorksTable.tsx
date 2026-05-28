"use client";
import { Work } from "@/types/dto";
import { getMatchColor, getMatchLabel } from "@/utils/tableFuncs";
import { Link, Chip } from "@mui/material";
import { DataGrid } from "@mui/x-data-grid";
import { useRouter } from "next/navigation";

export default function WorksTable({ sortedWorks }: { sortedWorks: Work[] }) {
  const router = useRouter();
  const shouldShowReviewerColumn = sortedWorks.some(
    (work) => work.type === "QUALIFICATION_WORK"
  );

  return (
    <>
      <DataGrid
        rows={sortedWorks.map((work) => ({
          id: work.id,
          student: work.student.name,
          type: work.type,
          theme: work.theme || "—",
          supervisor: work.supervisor?.name || "—",
          reviewer: work.reviewer?.name || "—",
          studentGroup: work?.studentGroup || "—",
          fullTextLink: work.fullTextLink,
          shortTextLink: work.shortTextLink,
          submissionLink: work.googleSubmissionLink,
          isCorrectStudent: work.isCorrectStudent,
          isCorrectSupervisor: work.isCorrectSupervisor,
          isCorrectTheme: work.isCorrectTheme,
          plagiarismCheckStatus: work.plagiarismCheckStatus,
          fullReportLink: work.plagiarismReport?.fullReportLink,
          shortReportLink: work.plagiarismReport?.shortReportLink,
        }))}
        columns={[
          { field: "student", headerName: "Студент", flex: 1 },
          { field: "theme", headerName: "Тема", flex: 2 },
          { field: "supervisor", headerName: "Керівник", flex: 1 },
          { field: "reviewer", headerName: "Рецензент", flex: 1 },
          { field: "studentGroup", headerName: "Група", flex: 1 },
          {
            field: "fullTextLink",
            headerName: "Робота (повна)",
            flex: 1,
            renderCell: (params) =>
              params.value ? (
                <Link
                  href={params.value}
                  target="_blank"
                  rel="noopener noreferrer"
                  onClick={(e) => e.stopPropagation()}
                >
                  Переглянути
                </Link>
              ) : (
                "—"
              ),
          },
          {
            field: "shortTextLink",
            headerName: "Робота (без додатків)",
            flex: 1,
            renderCell: (params) =>
              params.value ? (
                <Link
                  href={params.value}
                  target="_blank"
                  rel="noopener noreferrer"
                  onClick={(e) => e.stopPropagation()}
                >
                  Переглянути
                </Link>
              ) : (
                "—"
              ),
          },
          {
            field: "fullReportLink",
            headerName: "Звіт (повний)",
            flex: 1,
            renderCell: (params) =>
              params.value ? (
                <Link
                  href={params.value}
                  target="_blank"
                  rel="noopener noreferrer"
                  onClick={(e) => e.stopPropagation()}
                >
                  Переглянути
                </Link>
              ) : (
                "—"
              ),
          },
          {
            field: "shortReportLink",
            headerName: "Звіт (короткий)",
            flex: 1,
            renderCell: (params) =>
              params.value ? (
                <Link
                  href={params.value}
                  target="_blank"
                  rel="noopener noreferrer"
                  onClick={(e) => e.stopPropagation()}
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
                  onClick={(e) => e.stopPropagation()}
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
                sx={{ width: 70 }}
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
                sx={{ width: 70 }}
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
                sx={{ width: 70 }}
              />
            ),
          },
        ]}
        pageSizeOptions={[5, 10, 25, 50, 100]}
        initialState={{
          pagination: { paginationModel: { pageSize: 100, page: 0 } },
          columns: {
            columnVisibilityModel: {
              reviewer: shouldShowReviewerColumn,
            },
          },
        }}
        disableRowSelectionOnClick
        onRowClick={(params) => router.push(`/works/${params.row.id}`)}
        getRowClassName={(params) => {
          switch (params.row.plagiarismCheckStatus) {
            case "IN_PROGRESS":
              return "row-in-progress";
            case "CHECKED":
              return "row-checked";
            default:
              return "";
          }
        }}
        sx={{
          ".MuiDataGrid-cell:focus": {
            outline: "none",
          },
          "& .MuiDataGrid-row:hover": {
            cursor: "pointer",
          },
          "& .row-in-progress": {
            backgroundColor: "#fff8e1",
          },
          "& .row-checked": {
            backgroundColor: "#e8f5e9",
          },
        }}
      />
    </>
  );
}
