"use client";
import { Work } from "@/types/dto";
import { Link, Chip, Popover, Button, Box } from "@mui/material";
import { DataGrid } from "@mui/x-data-grid";
import { useState } from "react";

export default function WorksTable({ sortedWorks }: { sortedWorks: Work[] }) {
  const [anchorEl, setAnchorEl] = useState<HTMLButtonElement | null>(null);
  const [popoverContent, setPopoverContent] = useState<string>("");

  const handleOpenPopover = (
    event: React.MouseEvent<HTMLButtonElement>,
    content: string
  ) => {
    setAnchorEl(event.currentTarget);
    setPopoverContent(content);
  };

  const handleClosePopover = () => {
    setAnchorEl(null);
    setPopoverContent("");
  };

  const open = Boolean(anchorEl);
  const id = open ? "theme-popover" : undefined;

  return (
    <>
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
          isCorrectTheme: {
            boolValue: work.correctTheme,
            stringValue: work.themeDifference,
          },
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
              <Button
                variant="text"
                onClick={(e) => handleOpenPopover(e, params.value.stringValue)}
              >
                <Chip
                  label={params.value.boolValue ? "Так" : "Ні"}
                  color={params.value.boolValue ? "success" : "error"}
                  size="small"
                />
              </Button>
            ),
          },
        ]}
        pageSizeOptions={[5, 10, 25]}
        initialState={{
          pagination: { paginationModel: { pageSize: 10, page: 0 } },
        }}
        disableRowSelectionOnClick
      />

      <Popover
        id={id}
        open={open}
        anchorEl={anchorEl}
        onClose={handleClosePopover}
        anchorOrigin={{
          vertical: "bottom",
          horizontal: "left",
        }}
        PaperProps={{ style: { maxWidth: 400, padding: 12 } }}
      >
        <Box dangerouslySetInnerHTML={{ __html: popoverContent }} />
      </Popover>
    </>
  );
}
