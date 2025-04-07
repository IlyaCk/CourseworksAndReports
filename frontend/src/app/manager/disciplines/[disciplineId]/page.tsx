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
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
} from "@mui/material";
import { notFound } from "next/navigation";

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
    <Container maxWidth="lg">
      <Stack spacing={4} mt={4}>
        <Card sx={{ boxShadow: 3 }}>
          <CardContent>
            <Typography variant="h4" fontWeight="bold">
              {discipline.name} ({discipline.year})
            </Typography>
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
          </CardContent>
        </Card>

        {discipline.works.length > 0 && (
          <Card sx={{ boxShadow: 3 }}>
            <CardContent>
              <Typography variant="h5" fontWeight="bold" mb={2}>
                Роботи студентів
              </Typography>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Студент</TableCell>
                    <TableCell>Тема</TableCell>
                    <TableCell>Керівник</TableCell>
                    <TableCell>Файл</TableCell>
                    <TableCell>Здача (Submission)</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {sortedWorks.map((work) => (
                    <TableRow key={work.id}>
                      <TableCell>{work.student.name}</TableCell>
                      <TableCell>{work.theme || "—"}</TableCell>
                      <TableCell>{work.supervisor?.name || "—"}</TableCell>
                      <TableCell>
                        {work.classroomLink ? (
                          <Link
                            href={work.classroomLink}
                            target="_blank"
                            rel="noopener noreferrer"
                          >
                            Переглянути
                          </Link>
                        ) : (
                          "—"
                        )}
                      </TableCell>
                      <TableCell>
                        {work.googleSubmissionLink ? (
                          <Link
                            href={work.googleSubmissionLink}
                            target="_blank"
                            rel="noopener noreferrer"
                          >
                            Здача
                          </Link>
                        ) : (
                          "—"
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
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
