"use client";

import React, { useState } from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  List,
  ListItem,
  ListItemText,
  Box,
  Input,
} from "@mui/material";
import { toast } from "react-toastify";
import { useRouter } from "next/navigation";

type UploadReportsModalProps = {
  open: boolean;
  onClose: () => void;
  disciplineId: number;
};

export default function UploadReportsModal({
  open,
  onClose,
  disciplineId,
}: UploadReportsModalProps) {
  const [files, setFiles] = useState<File[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFiles = Array.from(event.target.files || []);
    const isAllPdf = selectedFiles.every(
      (file) => file.type === "application/pdf"
    );

    if (!isAllPdf) {
      setError("Всі файли мають бути у форматі PDF.");
      setFiles([]);
    } else {
      setFiles(selectedFiles);
      setError(null);
    }
  };

  const handleSubmit = async () => {
    if (files.length > 0 && !error) {
      setLoading(true);
      const formData = new FormData();
      files.forEach((file) => formData.append("files", file));

      try {
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/manager/disciplines/${disciplineId}/plagiarism-reports`,
          {
            method: "POST",
            body: formData,
            credentials: "include",
          }
        );

        if (!response.ok) {
          toast.error("Помилка при завантаженні звітів");
        }

        toast.success("Звіти додано!");
        onClose();
        setFiles([]);
        router.refresh();
      } catch (err) {
        toast.error("Не вдалося завантажити файли." + err);
      } finally {
        setLoading(false);
      }
    }
  };

  const handleClose = () => {
    setFiles([]);
    setError(null);
    onClose();
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>Додати звіти перевірки</DialogTitle>
      <DialogContent>
        <Box my={2}>
          <Input
            type="file"
            inputProps={{ multiple: true, accept: "application/pdf" }}
            onChange={handleFileChange}
          />
        </Box>
        {files.length > 0 && (
          <List dense>
            {files.map((file, index) => (
              <ListItem key={index}>
                <ListItemText primary={file.name} />
              </ListItem>
            ))}
          </List>
        )}
        {error && (
          <Typography color="error" variant="body2">
            {error}
          </Typography>
        )}
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={handleClose} color="secondary">
          Скасувати
        </Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          color="primary"
          disabled={files.length === 0 || !!error}
          loading={loading}
        >
          Додати
        </Button>
      </DialogActions>
    </Dialog>
  );
}
