import { Metadata } from "next";
import CreateDisciplineForm from "@/components/CreateDisciplineForm";
import { cookies } from "next/headers";
import { Course } from "@/types/classroom";

export const metadata: Metadata = {
  title: "Створення дисципліни",
};

export default async function CreateDisciplinePage() {
  const cookieStore = await cookies();
  const response = await fetch(
    `${process.env.NEXT_PUBLIC_API_URL}/manager/classrooms`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Cookie: `JSESSIONID=${cookieStore.get("JSESSIONID")?.value}`,
      },
    }
  );

  const googleClassrooms: Course[] = response.ok ? await response.json() : null;

  return <CreateDisciplineForm googleClassrooms={googleClassrooms} />;
}
