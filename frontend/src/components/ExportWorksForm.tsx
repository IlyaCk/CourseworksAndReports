"use client";

import { useState } from "react";
import { DataGrid, GridRowSelectionModel } from "@mui/x-data-grid";
import {
  Button,
  FormControlLabel,
  Checkbox,
  CircularProgress,
  Box,
  Typography,
  Link,
} from "@mui/material";
import { Work } from "@/types/dto";
import { toast } from "react-toastify";
import DownloadIcon from "@mui/icons-material/Download";

export default function ExportWorksForm({ works }: { works: Work[] }) {
  const [selectionModel, setSelectionModel] = useState<GridRowSelectionModel>({
    type: "include",
    ids: new Set(),
  });
  const [includeFull, setIncludeFull] = useState(false);
  const [includeShort, setIncludeShort] = useState(false);
  const [includeFullReport, setIncludeFullReport] = useState(false);
  const [includeShortReport, setIncludeShortReport] = useState(false);
  const [loading, setLoading] = useState(false);
  const shouldShowReviewerColumn = works.some(
    (work) => work.type === "QUALIFICATION_WORK"
  );

  const downloadFiles = async () => {
    try {
      setLoading(true);
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/manager/works/export`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          credentials: "include",
          body: JSON.stringify({
            ids: Array.from(selectionModel.ids),
            includeFull,
            includeShort,
            includeFullReport,
            includeShortReport,
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
        toast.success("Експортовано успішно");
        setLoading(false);
      } else {
        toast.error("Помилка експорту робіт");
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
          plagiarismCheckStatus: work.plagiarismCheckStatus,
          fullTextLink: work.fullTextLink,
          shortTextLink: work.shortTextLink,
          fullReportLink: work.plagiarismReport?.fullReportLink,
          shortReportLink: work.plagiarismReport?.shortReportLink,
        }))}
        columns={[
          { field: "student", headerName: "Студент", flex: 1.5 },
          { field: "theme", headerName: "Тема", flex: 2 },
          { field: "supervisor", headerName: "Керівник", flex: 1.5 },
          { field: "reviewer", headerName: "Рецензент", flex: 1.5 },
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
        ]}
        checkboxSelection
        rowSelectionModel={selectionModel}
        onRowSelectionModelChange={(newSelection) =>
          setSelectionModel(newSelection)
        }
        getRowId={(row) => row.id}
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
        pageSizeOptions={[5, 10, 25, 50, 100]}
        initialState={{
          pagination: { paginationModel: { pageSize: 100, page: 0 } },
          columns: {
            columnVisibilityModel: {
              reviewer: shouldShowReviewerColumn,
            },
          },
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

      <Typography
        variant="h6"
        fontWeight="bold"
        sx={{ width: 500, mt: 6, mb: 2 }}
      >
        Оберіть що хочете експортувати:
      </Typography>
      <Box>
        <FormControlLabel
          control={
            <Checkbox
              checked={includeFull}
              onChange={(e) => setIncludeFull(e.target.checked)}
            />
          }
          label="Повна версія"
        />
        <FormControlLabel
          control={
            <Checkbox
              checked={includeShort}
              onChange={(e) => setIncludeShort(e.target.checked)}
            />
          }
          label="Без додатків"
        />
        <FormControlLabel
          control={
            <Checkbox
              checked={includeFullReport}
              onChange={(e) => setIncludeFullReport(e.target.checked)}
            />
          }
          label="Повний звіт"
        />
        <FormControlLabel
          control={
            <Checkbox
              checked={includeShortReport}
              onChange={(e) => setIncludeShortReport(e.target.checked)}
            />
          }
          label="Короткий звіт"
        />
        <Button
          variant="contained"
          onClick={downloadFiles}
          disabled={
            selectionModel.ids.size === 0 ||
            loading ||
            (!includeFull && !includeShort)
          }
          sx={{ width: 150 }}
          startIcon={!loading ? <DownloadIcon /> : <></>}
        >
          {loading ? (
            <CircularProgress
              size={24}
              sx={{ justifySelf: "center", alignSelf: "center" }}
            />
          ) : (
            "Завантажити"
          )}
        </Button>
      </Box>
    </>
  );
}
