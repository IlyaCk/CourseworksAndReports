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
import { Discipline } from "@/types/dto";

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
  discipline,
}: {
  discipline: Discipline;
}) {
  const [open, setOpen] = useState(false);
  const [confirmation, setConfirmation] = useState("");
  const [loadingUpdate, setLoadingUpdate] = useState(false);
  const router = useRouter();
  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      name: discipline.name,
      year: discipline.year,
      nameFormat: discipline.nameFormat,
      pageNumberLocation: discipline.pageNumberLocation,
      visibility: discipline.visibility,
    },
  });
  const onSubmit = async (data: FormData) => {
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/manager/disciplines/${discipline.id}`,
        {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
          },
          credentials: "include",
          body: JSON.stringify({ ...data, id: discipline.id }),
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
        `${process.env.NEXT_PUBLIC_API_URL}/manager/disciplines/${discipline.id}`,
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

  const onUpdate = async () => {
    setLoadingUpdate(true);
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/manager/disciplines/${discipline.id}/update`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
          },
          credentials: "include",
        }
      );
      if (response.ok) {
        toast.success("Процес оновлення запущено!");
        router.refresh();
      } else {
        toast.error("Помилка оновлення дисципліни");
      }
    } catch (error) {
      toast.error("Помилка оновлення дисципліни " + error);
    } finally {
      setLoadingUpdate(false);
    }
  };

  const [showPublicConfirm, setShowPublicConfirm] = useState(false);
  const [pendingVisibility, setPendingVisibility] = useState<
    "PUBLIC" | "PRIVATE" | null
  >(null);

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
        disabled={discipline.updating || loadingUpdate}
        onClick={onUpdate}
      >
        {loadingUpdate ? "Оновлення..." : "Оновити дані"}
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
                label="Формат імені студента на титулці"
                value={watch("nameFormat")}
                defaultValue={discipline.nameFormat}
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
                defaultValue={discipline.pageNumberLocation}
                value={watch("pageNumberLocation")}
              >
                <MenuItem value="TOP">Зверху</MenuItem>
                <MenuItem value="BOTTOM">Знизу</MenuItem>
                <MenuItem value="ANY">Неважливо</MenuItem>
              </Select>
            </FormControl>
            <FormControl fullWidth margin="normal">
              <InputLabel>Видимість</InputLabel>
              <Select
                label="Видимість"
                value={watch("visibility")}
                onChange={(e) => {
                  const newValue = e.target.value as "PUBLIC" | "PRIVATE";
                  if (discipline.visibility === "PUBLIC") {
                    return;
                  }
                  if (newValue === "PUBLIC") {
                    setPendingVisibility(newValue);
                    setShowPublicConfirm(true);
                  } else {
                    const event = {
                      ...e,
                      target: {
                        ...e.target,
                        name: "visibility",
                        value: newValue,
                      },
                    };
                    register("visibility").onChange(event);
                  }
                }}
                disabled={discipline.visibility === "PUBLIC"}
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
          <DialogActions sx={{ px: 3, pb: 3 }}>
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
          <Dialog
            open={showPublicConfirm}
            onClose={() => setShowPublicConfirm(false)}
          >
            <DialogTitle>Підтвердження зміни видимості</DialogTitle>
            <DialogContent>
              <Typography>
                Ви збираєтесь зробити дисципліну публічною. Усі повні версії
                робіт та звіти стануть доступними кожному, хто має посилання. Це{" "}
                <strong>незворотна дія</strong>. Ви впевнені?
              </Typography>
            </DialogContent>
            <DialogActions>
              <Button
                onClick={() => setShowPublicConfirm(false)}
                color="secondary"
              >
                Скасувати
              </Button>
              <Button
                onClick={() => {
                  const syntheticEvent = {
                    target: {
                      name: "visibility",
                      value: pendingVisibility,
                    },
                  };
                  register("visibility").onChange(syntheticEvent);
                  setShowPublicConfirm(false);
                }}
                color="primary"
                variant="contained"
              >
                Так, зробити публічною
              </Button>
            </DialogActions>
          </Dialog>
        </form>
      </Dialog>
    </>
  );
}
