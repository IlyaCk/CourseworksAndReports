import { Department } from "@/types/dto";
import { cookies } from "next/headers";
import {
  Card,
  CardContent,
  CardActionArea,
  Typography,
  Container,
  Box,
} from "@mui/material";
import NextLink from "next/link";
import { Metadata } from "next";

export const metadata: Metadata = {
  title: "Головна сторінка",
};

export default async function Home() {
  const cookieStore = await cookies();
  const response = await fetch(
    `${process.env.NEXT_PUBLIC_API_URL}/departments/`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Cookie: `JSESSIONID=${cookieStore.get("JSESSIONID")?.value}`,
      },
    }
  );

  const departments: Department[] = response.ok ? await response.json() : [];

  return (
    <Container maxWidth="md">
      <Box sx={{ textAlign: "center", my: 4 }}>
        <Typography variant="h4" fontWeight="bold">
          СИСТЕМА ОРГАНІЗАЦІЇ ОБЛІКУ КУРСОВИХ РОБІТ ТА ЗВІТІВ ЇХ ПЕРЕВІРОК НА
          ТЕКСТОВІ ЗАПОЗИЧЕННЯ
        </Typography>
      </Box>
      <Box sx={{ textAlign: "center", my: 4 }}>
        <Typography variant="h4" fontWeight="bold">
          Список кафедр:
        </Typography>
      </Box>

      <Box display="flex" flexDirection="column" gap={2}>
        {departments.map((department) => (
          <Card
            key={department.id}
            sx={{
              boxShadow: 3,
              transition: "0.3s",
              "&:hover": { boxShadow: 6 },
            }}
          >
            <CardActionArea
              component={NextLink}
              href={`/departments/${department.id}`}
            >
              <CardContent>
                <Typography variant="h6">{department.name}</Typography>
              </CardContent>
            </CardActionArea>
          </Card>
        ))}
      </Box>
    </Container>
  );
}
