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
} from "@mui/material";
import { notFound } from "next/navigation";
import PublicWorksTable from "@/components/PublicWorksTable";

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
    <Container maxWidth={false}>
      <Stack spacing={4} mt={4}>
        <Card sx={{ boxShadow: 3 }}>
          <CardContent>
            <Typography variant="h4" fontWeight="bold">
              {discipline.name} ({discipline.year})
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
          </CardContent>
        </Card>
        {discipline.works.length > 0 && (
          <Card sx={{ boxShadow: 3 }}>
            <CardContent>
              <Typography variant="h5" fontWeight="bold" mb={2}>
                Роботи студентів
              </Typography>
              <PublicWorksTable sortedWorks={sortedWorks} />
            </CardContent>
          </Card>
        )}
      </Stack>
    </Container>
  );
}
