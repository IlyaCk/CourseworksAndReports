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
  FormControlLabel,
  Box,
  RadioGroup,
  Radio,
  ListItemText,
  CircularProgress,
} from "@mui/material";
import { toast } from "react-toastify";
import { Course, CourseMaterialSet, CourseWork } from "@/types/classroom";
import { useRouter } from "next/navigation";
import { Discipline } from "@/types/dto";

const currentYear = new Date().getFullYear();
const disciplineSchema = z.object({
  name: z.string().min(1, "Назва обов'язкова"),
  year: z.number(),
  type: z.enum(["COURSEWORK", "QUALIFICATION_WORK"]),
  topicDistributionLink: z
    .string()
    .min(1, "Оберіть Google Таблицю або введіть посилання")
    .regex(
      /^https:\/\/docs\.google\.com\/spreadsheets\/d\/[a-zA-Z0-9-_]+/,
      "Оберіть Google Таблицю або введіть посилання"
    ),
  googleClassLink: z
    .string()
    .min(1, "Оберіть Google Клас або введіть посилання"),
  googleAssignmentLink: z
    .string()
    .min(1, "Оберіть завдання або введіть посилання"),
  topicDistributionMode: z.enum(["MANUAL", "LIST"]),
  googleClassMode: z.enum(["MANUAL", "LIST"]),
  assignmentMode: z.enum(["MANUAL", "LIST"]),
  nameFormat: z.enum([
    "ALL",
    "SURNAME_NAME",
    "SURNAME_I",
    "SURNAME_IB",
    "SURNAME_NAME_PATRONYMIC",
  ]),
  pageNumberLocation: z.enum(["TOP", "BOTTOM", "ANY"]),
});

type DisciplineFormData = z.infer<typeof disciplineSchema>;

interface Props {
  googleClassrooms: Course[];
}

export default function CreateDisciplineForm({ googleClassrooms }: Props) {
  const router = useRouter();
  const [assignments, setAssignments] = useState<CourseWork[]>([]);
  const [materials, setMaterials] = useState<CourseMaterialSet[]>([]);
  const {
    register,
    handleSubmit,
    watch,
    setValue,
    trigger,
    formState: { errors, isSubmitting },
  } = useForm<DisciplineFormData>({
    resolver: zodResolver(disciplineSchema),
    defaultValues: {
      name: "",
      year: currentYear,
      type: "COURSEWORK",
      topicDistributionLink: "",
      googleClassLink: "",
      googleAssignmentLink: "",
      topicDistributionMode: "LIST",
      googleClassMode: "LIST",
      assignmentMode: "LIST",
    },
  });

  const selectedClass = watch("googleClassLink");
  const [assignmentLoading, setAssignmentLoading] = useState(false);
  const [materialLoading, setMaterialLoading] = useState(false);

  useEffect(() => {
    if (!selectedClass) return;

    const fetchAssignments = async () => {
      try {
        setAssignmentLoading(true);
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/manager/courseworks`,
          {
            method: "POST",
            credentials: "include",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify({ courseLink: selectedClass }),
          }
        );
        if (response.ok) {
          const data: CourseWork[] = await response.json();
          setAssignments(data);
        } else {
          toast.error("Помилка завантаження завдань " + response.statusText);
        }
        setAssignmentLoading(false);
      } catch (error) {
        toast.error("Помилка завантаження завдань " + error);
      }
      setAssignmentLoading(false);
    };

    fetchAssignments();
  }, [selectedClass]);

  useEffect(() => {
    if (!selectedClass) return;

    const fetchMaterials = async () => {
      try {
        setMaterialLoading(true);
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/manager/materials`,
          {
            method: "POST",
            credentials: "include",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify({ courseLink: selectedClass }),
          }
        );
        if (response.ok) {
          const data: CourseMaterialSet[] = await response.json();
          setMaterials(data);
        } else {
          toast.error("Помилка завантаження матеріалів " + response.statusText);
        }
      } catch (error) {
        toast.error("Помилка завантаження матеріалів " + error);
      }
      setMaterialLoading(false);
    };

    fetchMaterials();
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
        const newDiscipline: Discipline = await response.json();
        toast.success("Дисципліна створена успішно!");
        router.push(`/manager/disciplines/${newDiscipline.id}`);
      } else {
        toast.error("Помилка створення дисципліни");
      }
    } catch (error) {
      toast.error("Помилка створення дисципліни " + error);
    }
  };

  return (
    <Container maxWidth="md" sx={{ my: 4 }}>
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

        <FormControl fullWidth margin="normal">
          <InputLabel>Формат імені студента на титулці</InputLabel>
          <Select
            {...register("nameFormat")}
            defaultValue={"ALL"}
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
            defaultValue={"ANY"}
            label="Розташування номерів сторінок"
          >
            <MenuItem value="TOP">Зверху</MenuItem>
            <MenuItem value="BOTTOM">Знизу</MenuItem>
            <MenuItem value="ANY">Неважливо</MenuItem>
          </Select>
        </FormControl>

        <Box mt={4}>
          <Typography variant="h6" gutterBottom>
            Google Клас
          </Typography>
          <FormControl component="fieldset" fullWidth>
            <RadioGroup
              value={watch("googleClassMode")}
              onChange={(e) =>
                setValue("googleClassMode", e.target.value as "MANUAL" | "LIST")
              }
            >
              <FormControlLabel
                value="LIST"
                control={<Radio />}
                label="Обрати з доступних"
              />
              <FormControlLabel
                value="MANUAL"
                control={<Radio />}
                label="Ввести вручну"
              />
            </RadioGroup>

            {watch("googleClassMode") === "LIST" ? (
              <FormControl
                fullWidth
                margin="normal"
                disabled={false}
                error={!!errors.googleClassLink}
              >
                <InputLabel id="classroom-label">Google Клас</InputLabel>
                <Select
                  label="Google Клас"
                  labelId="classroom-label"
                  {...register("googleClassLink")}
                  defaultValue=""
                  onChange={(e) => {
                    setValue("googleAssignmentLink", "");
                    trigger("googleAssignmentLink");
                    setValue("topicDistributionLink", "");
                    trigger("topicDistributionLink");
                    setAssignments([]);
                    setMaterials([]);
                    setValue("googleClassLink", e.target.value);
                    trigger("googleClassLink");
                  }}
                >
                  {googleClassrooms.map((classroom) => (
                    <MenuItem
                      key={classroom.id}
                      value={classroom.alternateLink}
                    >
                      <ListItemText
                        primary={classroom.name}
                        secondary={classroom.section}
                      />
                    </MenuItem>
                  ))}
                </Select>
                <FormHelperText>
                  {errors.googleClassLink?.message}
                </FormHelperText>
              </FormControl>
            ) : (
              <TextField
                label="Посилання на Google Клас"
                fullWidth
                {...register("googleClassLink")}
                margin="normal"
                error={!!errors.googleClassLink}
                helperText={errors.googleClassLink?.message}
                onBlur={(e) => {
                  setValue("googleAssignmentLink", "");
                  trigger("googleAssignmentLink");
                  setValue("topicDistributionLink", "");
                  trigger("topicDistributionLink");
                  setAssignments([]);
                  setMaterials([]);
                  setValue("googleClassLink", e.target.value);
                  trigger("googleClassLink");
                }}
                onChange={(e) => {
                  e.preventDefault();
                }}
              />
            )}
          </FormControl>
        </Box>
        <Box mt={4}>
          <Typography variant="h6" gutterBottom>
            Завдання з роботами
          </Typography>
          <FormControl component="fieldset" fullWidth>
            <RadioGroup
              value={watch("assignmentMode")}
              onChange={(e) =>
                setValue("assignmentMode", e.target.value as "MANUAL" | "LIST")
              }
            >
              <FormControlLabel
                value="LIST"
                control={<Radio />}
                label="Обрати з доступних"
              />
              <FormControlLabel
                value="MANUAL"
                control={<Radio />}
                label="Ввести вручну"
              />
            </RadioGroup>

            {watch("assignmentMode") === "LIST" ? (
              <FormControl
                fullWidth
                margin="normal"
                disabled={!selectedClass}
                error={!!errors.googleAssignmentLink}
              >
                <InputLabel id="assignment-label">Завдання</InputLabel>
                <Select
                  key={selectedClass}
                  label="Завдання"
                  labelId="assignment-label"
                  {...register("googleAssignmentLink")}
                  onChange={(e) => {
                    setValue("googleAssignmentLink", e.target.value);
                    trigger("googleAssignmentLink");
                  }}
                  defaultValue=""
                >
                  {!assignmentLoading ? (
                    assignments.map((assignment) => (
                      <MenuItem
                        key={assignment.id}
                        value={assignment.alternateLink}
                      >
                        {assignment.title}
                      </MenuItem>
                    ))
                  ) : (
                    <Box sx={{ display: "flex", justifyContent: "center" }}>
                      <CircularProgress
                        size={24}
                        sx={{ justifySelf: "center", alignSelf: "center" }}
                      />
                    </Box>
                  )}
                </Select>
                <FormHelperText>
                  {errors.googleAssignmentLink?.message}
                </FormHelperText>
              </FormControl>
            ) : (
              <TextField
                label="Посилання на завдання"
                fullWidth
                {...register("googleAssignmentLink")}
                margin="normal"
                error={!!errors.googleAssignmentLink}
                helperText={errors.googleAssignmentLink?.message}
                onChange={(e) => {
                  setValue("googleAssignmentLink", e.target.value);
                  trigger("googleAssignmentLink");
                }}
              />
            )}
          </FormControl>
        </Box>
        <Box mt={4}>
          <Typography variant="h6" gutterBottom>
            Таблиця з темами
          </Typography>
          <FormControl component="fieldset" fullWidth>
            <RadioGroup
              value={watch("topicDistributionMode")}
              onChange={(e) =>
                setValue(
                  "topicDistributionMode",
                  e.target.value as "MANUAL" | "LIST"
                )
              }
            >
              <FormControlLabel
                value="LIST"
                control={<Radio />}
                label="Обрати з доступних"
              />
              <FormControlLabel
                value="MANUAL"
                control={<Radio />}
                label="Ввести вручну"
              />
            </RadioGroup>

            {watch("topicDistributionMode") === "LIST" ? (
              <FormControl
                fullWidth
                margin="normal"
                disabled={!selectedClass}
                error={!!errors.topicDistributionLink}
              >
                <InputLabel id="topic-label">Таблиця розподілу тем</InputLabel>
                <Select
                  key={selectedClass}
                  label="Таблиця розподілу тем"
                  {...register("topicDistributionLink")}
                  defaultValue={""}
                  error={!!errors.topicDistributionLink}
                  onChange={(e) => {
                    setValue("topicDistributionLink", e.target.value);
                    trigger("topicDistributionLink");
                  }}
                >
                  {!materialLoading ? (
                    materials
                      .filter(
                        (materials) =>
                          materials.materials[0].driveFile!.driveFile
                            .alternateLink
                      )
                      .map((material, index) => (
                        <MenuItem
                          key={index}
                          value={
                            material.materials[0].driveFile!.driveFile
                              .alternateLink
                          }
                        >
                          {material.title}
                        </MenuItem>
                      ))
                  ) : (
                    <Box sx={{ display: "flex", justifyContent: "center" }}>
                      <CircularProgress
                        size={24}
                        sx={{ justifySelf: "center", alignSelf: "center" }}
                      />
                    </Box>
                  )}
                </Select>
                <FormHelperText>
                  {errors.topicDistributionLink?.message}
                </FormHelperText>
              </FormControl>
            ) : (
              <TextField
                label="Посилання на таблицю"
                fullWidth
                {...register("topicDistributionLink")}
                margin="normal"
                error={!!errors.topicDistributionLink}
                helperText={errors.topicDistributionLink?.message}
                onChange={(e) => {
                  setValue("topicDistributionLink", e.target.value);
                  trigger("topicDistributionLink");
                }}
              />
            )}
          </FormControl>
        </Box>
        <Button
          variant="contained"
          color="primary"
          type="submit"
          sx={{ mt: 2 }}
          loading={isSubmitting}
        >
          Створити
        </Button>
      </form>
    </Container>
  );
}
