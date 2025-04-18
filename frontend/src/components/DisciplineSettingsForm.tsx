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
} from "@mui/material";
import SettingsIcon from "@mui/icons-material/Settings";
import RefreshIcon from "@mui/icons-material/Refresh";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { toast } from "react-toastify";
import { useRouter } from "next/navigation";

const schema = z.object({
  name: z.string().min(1, "Назва обов'язкова"),
  year: z.number(),
});

type FormData = z.infer<typeof schema>;

export default function DisciplineSettingsForm({
  id,
  initialName,
  initialYear,
}: {
  id: number;
  initialName: string;
  initialYear: number;
}) {
  const [open, setOpen] = useState(false);
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
