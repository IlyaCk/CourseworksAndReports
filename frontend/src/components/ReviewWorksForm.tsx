"use client";

import { useState } from "react";
import {
  DataGrid,
  GridRowParams,
  GridRowSelectionModel,
} from "@mui/x-data-grid";
import {
  Button,
  CircularProgress,
  Link,
  Chip,
  Box,
  Typography,
} from "@mui/material";
import { Work } from "@/types/dto";
import { toast } from "react-toastify";
import { getMatchColor, getMatchLabel } from "@/utils/tableFuncs";
import { usePathname, useRouter } from "next/navigation";

export default function ReviewWorksForm({ works }: { works: Work[] }) {
  const [selectionModel, setSelectionModel] = useState<GridRowSelectionModel>({
    type: "include",
    ids: new Set(),
  });
  const [loading, setLoading] = useState(false);
  const router = useRouter();
  const pathname = usePathname();
  const shouldShowReviewerColumn = works.some(
    (work) => work.type === "QUALIFICATION_WORK"
  );

  const reviewWorks = async () => {
    try {
      setLoading(true);
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/manager/works/review`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          credentials: "include",
          body: JSON.stringify({
            ids: Array.from(selectionModel.ids),
          }),
        }
      );

      if (response.ok) {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.href = url;
        const contentDisposition = response.headers.get("Content-Disposition");
        let filename = "works-export.zip";

        if (contentDisposition) {
          const match = contentDisposition.match(/filename="?(.+?)"?$/);
          if (match && match[1]) {
            filename = match[1];
          }
        }
        link.setAttribute("download", filename);
        document.body.appendChild(link);
        link.click();
        link.remove();
        toast.success("Підтверджено успішно");
        setLoading(false);
        router.push(pathname.replace("/review", ""));
      } else {
        toast.error("Помилка при зборі робіт");
      }
    } catch (err) {
      toast.error("Помилка запиту: " + err);
    }
  };

  return (
    <>
      <DataGrid
        rows={works.map((work) => ({
          id: work.id,
          student: work.student.name,
          theme: work.theme || "—",
          supervisor: work.supervisor?.name || "—",
          studentGroup: work?.studentGroup || "—",
          reviewer: work.reviewer?.name || "—",
          fullTextLink: work.fullTextLink,
          shortTextLink: work.shortTextLink,
          submissionLink: work.googleSubmissionLink,
          isCorrectStudent: work.isCorrectStudent,
          isCorrectSupervisor: work.isCorrectSupervisor,
          isCorrectTheme: work.isCorrectTheme,
          fullReportLink: work.plagiarismReport?.fullReportLink,
          shortReportLink: work.plagiarismReport?.shortReportLink,
          plagiarismCheckStatus: work.plagiarismCheckStatus,
        }))}
        columns={[
          { field: "student", headerName: "Студент", flex: 1 },
          { field: "theme", headerName: "Тема", flex: 2 },
          { field: "supervisor", headerName: "Керівник", flex: 1 },
          { field: "reviewer", headerName: "Рецензент", flex: 1 },
          { field: "studentGroup", headerName: "Група", flex: 0.5 },
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
        pageSizeOptions={[25, 50, 100]}
        initialState={{
          pagination: { paginationModel: { pageSize: 100, page: 0 } },
          columns: {
            columnVisibilityModel: {
              reviewer: shouldShowReviewerColumn,
            },
          },
        }}
        checkboxSelection
        rowSelectionModel={selectionModel}
        onRowSelectionModelChange={(newSelection) =>
          setSelectionModel(newSelection)
        }
        getRowId={(row) => row.id}
        isRowSelectable={(params: GridRowParams) =>
          params.row.plagiarismCheckStatus === "NOT_CHECKED"
        }
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
          "& .row-in-progress": {
            backgroundColor: "#fff8e1",
          },
          "& .row-checked": {
            backgroundColor: "#e8f5e9",
          },
        }}
      />

      <Box sx={{ display: "flex", gap: 2, mt: 2 }}>
        <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
          <Box
            sx={{
              width: 16,
              height: 16,
              bgcolor: "#fff8e1",
              border: "1px solid #ccc",
            }}
          />
          <Typography variant="body2">Робота на перевірці</Typography>
        </Box>
        <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
          <Box
            sx={{
              width: 16,
              height: 16,
              bgcolor: "#e8f5e9",
              border: "1px solid #ccc",
            }}
          />
          <Typography variant="body2">Робота перевірена</Typography>
        </Box>
      </Box>

      <div style={{ marginTop: 20 }}>
        <Button
          variant="contained"
          onClick={reviewWorks}
          disabled={selectionModel.ids.size === 0 || loading}
          sx={{ width: 300 }}
        >
          {loading ? (
            <CircularProgress
              size={24}
              sx={{ justifySelf: "center", alignSelf: "center" }}
            />
          ) : (
            "Забрати вибрані роботи на перевірку"
          )}
        </Button>
      </div>
    </>
  );
}
