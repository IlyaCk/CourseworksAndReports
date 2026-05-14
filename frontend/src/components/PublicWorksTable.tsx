"use client";
import { Work } from "@/types/dto";
import { Link } from "@mui/material";
import { DataGrid } from "@mui/x-data-grid";

export default function PublicWorksTable({
  sortedWorks,
}: {
  sortedWorks: Work[];
}) {
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
            headerName: "Робота",
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
            headerName: "Звіт",
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
        ]}
        pageSizeOptions={[5, 10, 25, 100]}
        initialState={{
          pagination: { paginationModel: { pageSize: 10, page: 0 } },
          columns: {
            columnVisibilityModel: {
              reviewer: shouldShowReviewerColumn,
            },
          },
        }}
        disableRowSelectionOnClick
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
