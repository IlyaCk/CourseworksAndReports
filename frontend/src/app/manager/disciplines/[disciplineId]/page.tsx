import { Discipline } from "@/types/dto";
import { Metadata } from "next";
import { cookies } from "next/headers";
import {
  Container,
  Card,
  CardContent,
  Stack,
  Typography,
  Link,
  Box,
} from "@mui/material";
import { notFound } from "next/navigation";
import WorksTable from "@/components/WorksTable";
import DisciplineSettingsForm from "@/components/DisciplineSettingsForm";
import DisciplineUpdateAlert from "@/components/DisciplineUpdateAlert";
import WorksActionButtons from "@/components/WorksActionButtons";

export const metadata: Metadata = {
  title: "Дисципліна",
};

export default async function DisciplinePage({
  params,
}: {
  params: Promise<{ disciplineId: string }>;
}) {
  const { disciplineId } = await params;
  const cookieStore = await cookies();
  const response = await fetch(
    `${process.env.NEXT_PUBLIC_API_URL}/disciplines/${disciplineId}`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Cookie: `JSESSIONID=${cookieStore.get("JSESSIONID")?.value}`,
      },
    }
  );

  const discipline: Discipline = response.ok ? await response.json() : null;
  if (!discipline) return notFound();

  const sortedWorks = [...discipline.works].sort((a, b) =>
    a.student.name.localeCompare(b.student.name, "uk")
  );

  return (
    <Container sx={{ my: 4 }} maxWidth={false}>
      <Stack spacing={4}>
        {discipline.updating && (
          <DisciplineUpdateAlert disciplineId={discipline.id} />
        )}
        <Card sx={{ boxShadow: 3 }}>
          <CardContent sx={{ position: "relative" }}>
            <Typography variant="h4" fontWeight="bold" sx={{ width: 500 }}>
              {discipline.name} ({discipline.year})
            </Typography>
            <DisciplineSettingsForm discipline={discipline} />
            <Typography variant="subtitle1" mt={1}>
              Тип:{" "}
              {discipline.type === "COURSEWORK"
                ? "Курсова робота"
                : "Кваліфікаційна робота"}
            </Typography>
            <Typography variant="body1" mt={1}>
              Посилання на Google Таблицю розподілу тем:{" "}
              <Link
                href={discipline.topicDistributionLink}
                target="_blank"
                rel="noopener noreferrer"
              >
                {discipline.topicDistributionLink}
              </Link>
            </Typography>
            <Typography variant="body1" mt={1}>
              Посилання на Google Classroom:{" "}
              <Link
                href={discipline.googleClassLink}
                target="_blank"
                rel="noopener noreferrer"
              >
                {discipline.googleClassLink}
              </Link>
            </Typography>
            <Typography variant="body1" mt={1}>
              Посилання на Завдання:{" "}
              <Link
                href={discipline.googleAssignmentLink}
                target="_blank"
                rel="noopener noreferrer"
              >
                {discipline.googleAssignmentLink}
              </Link>
            </Typography>
            <Typography variant="body1" mt={1}>
              Посилання на папку з роботами:{" "}
              <Link
                href={discipline.googleDriveFolderLink}
                target="_blank"
                rel="noopener noreferrer"
              >
                {discipline.googleDriveFolderLink}
              </Link>
            </Typography>
            <Typography sx={{ position: "absolute", top: 22, right: 256 }}>
              <strong>Оновлено:</strong>{" "}
              {discipline.updateDate
                ? new Date(discipline.updateDate).toLocaleString()
                : "Немає даних"}
            </Typography>
          </CardContent>
        </Card>

        {discipline.works.length > 0 && (
          <Card sx={{ boxShadow: 3, position: "relative" }}>
            <CardContent>
              <Typography variant="h5" fontWeight="bold" mb={2}>
                Роботи студентів
              </Typography>
              <WorksActionButtons discipline={discipline} />
              <WorksTable sortedWorks={sortedWorks} />
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
            </CardContent>
          </Card>
        )}

        {discipline.students.length > 0 && (
          <Card sx={{ boxShadow: 3 }}>
            <CardContent>
              <Typography variant="h5" fontWeight="bold" mb={2}>
                Студенти
              </Typography>
              <Stack spacing={1}>
                {discipline.students.map((student) => (
                  <Typography key={student.id} variant="body1">
                    {student.name} ({student.email})
                  </Typography>
                ))}
              </Stack>
            </CardContent>
          </Card>
        )}

        {discipline.supervisors.length > 0 && (
          <Card sx={{ boxShadow: 3 }}>
            <CardContent>
              <Typography variant="h5" fontWeight="bold" mb={2}>
                Керівники
              </Typography>
              <Stack spacing={1}>
                {discipline.supervisors.map((supervisor) => (
                  <Typography key={supervisor.id} variant="body1">
                    {supervisor.name} ({supervisor.email})
                  </Typography>
                ))}
              </Stack>
            </CardContent>
          </Card>
        )}
      </Stack>
    </Container>
  );
}
