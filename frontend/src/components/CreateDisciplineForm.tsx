"use client";

import { useState, useEffect } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  Container,
  Typography,
  TextField,
  Button,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  FormHelperText,
} from "@mui/material";
import { toast } from "react-toastify";
import { Course, CourseWork } from "@/types/classroom";
import { useRouter } from "next/navigation";

const currentYear = new Date().getFullYear();
const disciplineSchema = z.object({
  name: z.string().min(1, "Назва обов'язкова"),
  year: z.number(),
  type: z.enum(["COURSEWORK", "QUALIFICATION_WORK"]),
  topicDistributionLink: z
    .string()
    .min(1, "Посилання обов'язкове")
    .regex(
      /^https:\/\/docs\.google\.com\/spreadsheets\/d\/[a-zA-Z0-9-_]+/,
      "Некоректне посилання на Google Таблицю"
    ),
  googleClassId: z.string().min(1, "Оберіть Google Клас"),
  googleAssignmentId: z.string().min(1, "Оберіть завдання"),
});

type DisciplineFormData = z.infer<typeof disciplineSchema>;

interface Props {
  googleClassrooms: Course[];
}

export default function CreateDisciplineForm({ googleClassrooms }: Props) {
  const router = useRouter();
  const [assignments, setAssignments] = useState<CourseWork[]>([]);
  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors },
  } = useForm<DisciplineFormData>({
    resolver: zodResolver(disciplineSchema),
    defaultValues: {
      name: "",
      year: currentYear,
      type: "COURSEWORK",
      topicDistributionLink: "",
      googleClassId: "",
      googleAssignmentId: "",
    },
  });

  const selectedClass = watch("googleClassId");

  useEffect(() => {
    if (!selectedClass) return;

    const fetchAssignments = async () => {
      try {
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/manager/${selectedClass}/courseworks`,
          {
            credentials: "include",
          }
        );
        if (response.ok) {
          const data: CourseWork[] = await response.json();
          setAssignments(data);
        } else {
          toast.error("Помилка завантаження завдань " + response.statusText);
        }
      } catch (error) {
        toast.error("Помилка завантаження завдань " + error);
      }
    };

    fetchAssignments();
  }, [selectedClass]);

  const onSubmit = async (data: DisciplineFormData) => {
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/manager/disciplines`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          credentials: "include",
          body: JSON.stringify(data),
        }
      );
      if (response.ok) {
        toast.success("Дисципліна створена успішно!");
        router.push("/manager");
      } else {
        toast.error("Помилка створення дисципліни");
      }
    } catch (error) {
      toast.error("Помилка створення дисципліни " + error);
    }
  };

  return (
    <Container maxWidth="md" sx={{ mt: 4 }}>
      <Typography variant="h4" gutterBottom>
        Створення дисципліни
      </Typography>

      <form onSubmit={handleSubmit(onSubmit)}>
        <TextField
          label="Назва дисципліни"
          fullWidth
          {...register("name")}
          margin="normal"
          error={!!errors.name}
          helperText={errors.name?.message}
        />

        <TextField
          label="Рік"
          type="number"
          fullWidth
          {...register("year", { valueAsNumber: true })}
          margin="normal"
          error={!!errors.year}
          helperText={errors.year?.message}
        />

        <FormControl fullWidth margin="normal">
          <InputLabel id="work-type">Тип роботи</InputLabel>
          <Select
            label="Тип роботи"
            labelId="work-type"
            defaultValue={"COURSEWORK"}
            {...register("type")}
          >
            <MenuItem value="COURSEWORK">Курсова робота</MenuItem>
            <MenuItem value="QUALIFICATION_WORK">
              Кваліфікаційна робота
            </MenuItem>
          </Select>
        </FormControl>

        <TextField
          label="Посилання на Google Таблицю"
          fullWidth
          {...register("topicDistributionLink")}
          margin="normal"
          error={!!errors.topicDistributionLink}
          helperText={errors.topicDistributionLink?.message}
        />

        <FormControl fullWidth margin="normal" error={!!errors.googleClassId}>
          <InputLabel id="classroom-input">Google Клас</InputLabel>
          <Select
            labelId="classroom-input"
            label="Google Клас"
            {...register("googleClassId")}
            onChange={(e) => setValue("googleClassId", e.target.value)}
            defaultValue={""}
          >
            {googleClassrooms.map((classroom) => (
              <MenuItem key={classroom.id} value={classroom.id}>
                {classroom.name}
              </MenuItem>
            ))}
          </Select>
          <FormHelperText>{errors.googleClassId?.message}</FormHelperText>
        </FormControl>

        <FormControl
          fullWidth
          margin="normal"
          disabled={!selectedClass}
          error={!!errors.googleAssignmentId}
        >
          <InputLabel id="task-id">Завдання</InputLabel>
          <Select
            labelId="task-id"
            label="Завдання"
            {...register("googleAssignmentId")}
            onChange={(e) => setValue("googleAssignmentId", e.target.value)}
            defaultValue={""}
          >
            {assignments.map((assignment) => (
              <MenuItem key={assignment.id} value={assignment.id}>
                {assignment.title}
              </MenuItem>
            ))}
          </Select>
          <FormHelperText>{errors.googleAssignmentId?.message}</FormHelperText>
        </FormControl>

        <Button
          variant="contained"
          color="primary"
          type="submit"
          sx={{ mt: 2 }}
        >
          Створити
        </Button>
      </form>
    </Container>
  );
}
