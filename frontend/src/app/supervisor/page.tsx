import { DisciplineDTO } from "@/types/dto";
import { Metadata } from "next";
import { cookies } from "next/headers";
import WorksAccordion from "@/components/WorksAccordion";
import { Typography } from "@mui/material";

export const metadata: Metadata = {
  title: "Сторінка викладача",
};

export default async function StudentPage() {
  const cookieStore = await cookies();
  const response = await fetch(
    `${process.env.NEXT_PUBLIC_API_URL}/supervisor/`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Cookie: `JSESSIONID=${cookieStore.get("JSESSIONID")?.value}`,
      },
    }
  );

  const disciplines: DisciplineDTO[] = response.ok
    ? await response.json()
    : null;

  return (
    <div className="m-4">
      {disciplines && disciplines.length > 0 ? (
        disciplines.map((discipline, index) => (
          <WorksAccordion discipline={discipline} key={index} />
        ))
      ) : (
        <Typography textAlign={"center"} variant="h6" color="textSecondary">
          У вас ще немає робіт
        </Typography>
      )}
    </div>
  );
}
