"use client";

import { useState } from "react";
import { DataGrid, GridRowSelectionModel } from "@mui/x-data-grid";
import {
  Button,
  FormControlLabel,
  Checkbox,
  CircularProgress,
} from "@mui/material";
import { Work } from "@/types/dto";
import { toast } from "react-toastify";
import DownloadIcon from "@mui/icons-material/Download";

export default function ExportWorksForm({ works }: { works: Work[] }) {
  const [selectionModel, setSelectionModel] = useState<GridRowSelectionModel>({
    type: "include",
    ids: new Set(),
  });
  const [includeFull, setIncludeFull] = useState(true);
  const [includeShort, setIncludeShort] = useState(false);
  const [loading, setLoading] = useState(false);

  const downloadFiles = async () => {
    try {
      setLoading(true);
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/manager/works`,
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
        }))}
        columns={[
          { field: "studentGroup", headerName: "Група", flex: 1 },
          { field: "student", headerName: "Студент", flex: 1.5 },
          { field: "theme", headerName: "Тема", flex: 2 },
          { field: "supervisor", headerName: "Керівник", flex: 1.5 },
        ]}
        checkboxSelection
        rowSelectionModel={selectionModel}
        onRowSelectionModelChange={(newSelection) =>
          setSelectionModel(newSelection)
        }
        getRowId={(row) => row.id}
      />

      <div style={{ marginTop: 20 }}>
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
        <Button
          variant="contained"
          onClick={downloadFiles}
          disabled={
            selectionModel.ids.size === 0 ||
            loading ||
            (!includeFull && !includeShort)
          }
          sx={{ width: 150 }}
          startIcon={<DownloadIcon />}
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
      </div>
    </>
  );
}
