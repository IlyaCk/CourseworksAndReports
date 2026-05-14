import ReviewWorksForm from "@/components/ReviewWorksForm";
import { Discipline } from "@/types/dto";
import { Container, Typography } from "@mui/material";
import { Metadata } from "next";
import { cookies } from "next/headers";
import { notFound } from "next/navigation";

export const metadata: Metadata = {
  title: "Перевірка робіт",
};

export default async function ReviewPage({
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
      <Typography variant="h4" fontWeight="bold" sx={{ width: 500, mb: 2 }}>
        Оберіть роботи, які потрібно перевірити
      </Typography>
      <ReviewWorksForm works={sortedWorks} />
    </Container>
  );
}
