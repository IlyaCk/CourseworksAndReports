"use client";

import { useState } from "react";
import {
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  IconButton,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Box,
  Paper,
  Typography,
} from "@mui/material";
import DeleteIcon from "@mui/icons-material/Delete";
import SettingsIcon from "@mui/icons-material/Settings";
import RefreshIcon from "@mui/icons-material/Refresh";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { toast } from "react-toastify";
import { useRouter } from "next/navigation";
import { NameFormat, PageNumberLocation, Visibility } from "@/types/dto";

const schema = z.object({
  name: z.string().min(1, "Назва обов'язкова"),
  year: z.number(),
  nameFormat: z.enum([
    "ALL",
    "SURNAME_NAME",
    "SURNAME_I",
    "SURNAME_IB",
    "SURNAME_NAME_PATRONYMIC",
  ]),
  pageNumberLocation: z.enum(["TOP", "BOTTOM", "ANY"]),
  visibility: z.enum(["PUBLIC", "PRIVATE"]),
});

type FormData = z.infer<typeof schema>;

export default function DisciplineSettingsForm({
  id,
  initialName,
  initialYear,
  initialNameFormat,
  initialPageNumberLocation,
  initialVisibility,
}: {
  id: number;
  initialName: string;
  initialYear: number;
  initialNameFormat: NameFormat;
  initialPageNumberLocation: PageNumberLocation;
  initialVisibility: Visibility;
}) {
  const [open, setOpen] = useState(false);
  const [confirmation, setConfirmation] = useState("");
  const router = useRouter();
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      name: initialName,
      year: initialYear,
    },
  });
  const onSubmit = async (data: FormData) => {
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/manager/disciplines/${id}`,
        {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
          },
          credentials: "include",
          body: JSON.stringify({ ...data, id: id }),
        }
      );

      if (response.ok) {
        toast.success("Дисципліна оновлена успішно");
        setOpen(false);
        router.refresh();
      } else {
        toast.error("Помилка оновлення дисципліни");
      }
    } catch (err) {
      toast.error("Помилка запиту: " + err);
    }
  };

  const onDelete = async () => {
    if (confirmation !== "ПІДТВЕРДЖУЮ") {
      toast.error('Введіть "ПІДТВЕРДЖУЮ"');
      return;
    }
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/manager/disciplines/${id}`,
        {
          method: "DELETE",
          credentials: "include",
        }
      );

      if (response.ok) {
        toast.success("Дисципліна видалена");
        setOpen(false);
        router.push("/manager");
        router.refresh();
      } else {
        toast.error("Помилка видалення");
      }
    } catch (err) {
      toast.error("Помилка запиту: " + err);
    }
  };

  const handleOpen = () => setOpen(true);

  const handleCancel = () => {
    reset();
    setOpen(false);
  };

  return (
    <>
      <IconButton
        aria-label="settings"
        sx={{ position: "absolute", right: 16, top: 16 }}
        onClick={handleOpen}
      >
        <SettingsIcon />
      </IconButton>

      <Button
        variant="outlined"
        sx={{ position: "absolute", right: 64, top: 16 }}
        startIcon={<RefreshIcon />}
        disabled
      >
        Оновити дані
      </Button>

      <Dialog open={open} onClose={handleCancel} fullWidth maxWidth="sm">
        <form onSubmit={handleSubmit(onSubmit)}>
          <DialogTitle>Налаштування дисципліни</DialogTitle>
          <DialogContent
            sx={{ display: "flex", flexDirection: "column", gap: 2, mt: 1 }}
          >
            <TextField
              margin="dense"
              label="Назва дисципліни"
              fullWidth
              {...register("name")}
              error={!!errors.name}
              helperText={errors.name?.message}
            />
            <TextField
              label="Рік"
              fullWidth
              type="number"
              {...register("year", { valueAsNumber: true })}
              error={!!errors.year}
              helperText={errors.year?.message}
            />
            <FormControl fullWidth margin="normal">
              <InputLabel>Формат імені студента на титулці</InputLabel>
              <Select
                {...register("nameFormat")}
                defaultValue={initialNameFormat}
                label="Формат імені студента на титулці"
              >
                <MenuItem value="ALL">Будь-який варіант</MenuItem>
                <MenuItem value="SURNAME_NAME">Прізвище Ім’я</MenuItem>
                <MenuItem value="SURNAME_I">Прізвище І.</MenuItem>
                <MenuItem value="SURNAME_IB">Прізвище І.Б.</MenuItem>
                <MenuItem value="SURNAME_NAME_PATRONYMIC">
                  Прізвище Ім’я По-Батькові
                </MenuItem>
              </Select>
            </FormControl>
            <FormControl fullWidth margin="normal">
              <InputLabel>Розташування номерів сторінок</InputLabel>
              <Select
                {...register("pageNumberLocation")}
                label="Розташування номерів сторінок"
                defaultValue={initialPageNumberLocation}
              >
                <MenuItem value="TOP">Зверху</MenuItem>
                <MenuItem value="BOTTOM">Знизу</MenuItem>
                <MenuItem value="ANY">Неважливо</MenuItem>
              </Select>
            </FormControl>
            <FormControl fullWidth margin="normal">
              <InputLabel>Видимість</InputLabel>
              <Select
                {...register("visibility")}
                error={!!errors.visibility}
                label="Видимість"
                defaultValue={initialVisibility}
              >
                <MenuItem value="PUBLIC">Публічна</MenuItem>
                <MenuItem value="PRIVATE">Приватна</MenuItem>
              </Select>
            </FormControl>
            <Box>
              <Paper
                elevation={0}
                sx={{ padding: 2, border: "1px dashed red" }}
              >
                <Typography
                  variant="subtitle2"
                  color="red"
                  sx={{ mb: 2, textAlign: "center" }}
                  gutterBottom
                >
                  Щоб видалити дисципліну, введіть &quot;ПІДТВЕРДЖУЮ&quot; і
                  натисніть Видалити
                </Typography>
                <Box
                  sx={{
                    display: "flex",
                    gap: 2,
                    justifyContent: "space-between",
                    height: "40px",
                  }}
                >
                  <TextField
                    value={confirmation}
                    onChange={(e) => setConfirmation(e.target.value)}
                    size="small"
                    placeholder="ПІДТВЕРДЖУЮ"
                  />
                  <Button
                    variant="outlined"
                    startIcon={<DeleteIcon />}
                    color="error"
                    onClick={onDelete}
                  >
                    Видалити
                  </Button>
                </Box>
              </Paper>
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleCancel} color="secondary">
              Скасувати
            </Button>
            <Button
              type="submit"
              variant="contained"
              color="primary"
              disabled={isSubmitting}
            >
              Зберегти
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </>
  );
}
