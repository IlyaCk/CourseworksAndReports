import { Department } from "@/types/dto";
import { Metadata } from "next";
import { cookies } from "next/headers";
import {
  Container,
  Card,
  CardContent,
  Stack,
  Typography,
  Link,
  Button,
} from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import NextLink from "next/link";
import { notFound } from "next/navigation";

export const metadata: Metadata = {
  title: "Керування кафедрою",
};

export default async function ManagerPage() {
  const cookieStore = await cookies();
  const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/manager/`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Cookie: `JSESSIONID=${cookieStore.get("JSESSIONID")?.value}`,
    },
  });

  const department: Department = response.ok ? await response.json() : null;

  if (!department) return notFound();

  return (
    <Container maxWidth="md">
      <Stack spacing={4} mt={4}>
        <Card sx={{ boxShadow: 3 }}>
          <CardContent>
            <Typography variant="h4" fontWeight="bold">
              {department.name}
            </Typography>
            <Typography variant="body1" mt={1}>
              Відповідальна особа: {department.responsibleUser.name} (
              {department.responsibleUser.email})
            </Typography>
          </CardContent>
        </Card>
        {department.headUsers.length > 0 && (
          <Card sx={{ boxShadow: 3 }}>
            <CardContent>
              <Typography variant="h5" fontWeight="bold" mb={2}>
                Керівники кафедри
              </Typography>
              <Stack spacing={1}>
                {department.headUsers.map((head) => (
                  <Typography key={head.id} variant="body1">
                    {head.name} ({head.email})
                  </Typography>
                ))}
              </Stack>
            </CardContent>
          </Card>
        )}
        <Card sx={{ boxShadow: 3 }}>
          <CardContent>
            <Stack
              direction="row"
              justifyContent="space-between"
              alignItems="center"
              mb={2}
            >
              <Typography variant="h5" fontWeight="bold">
                Дисципліни
              </Typography>
              <Button
                startIcon={<AddIcon />}
                variant="contained"
                color="primary"
                component={NextLink}
                href="/manager/new"
              >
                Додати дисципліну
              </Button>
            </Stack>

            {department.disciplines.length === 0 ? (
              <Typography color="textSecondary">Немає дисциплін.</Typography>
            ) : (
              <Stack spacing={2}>
                {department.disciplines.map((discipline) => (
                  <Link
                    key={discipline.id}
                    component={NextLink}
                    href={`/departments/${department.id}/disciplines/${discipline.id}`}
                    underline="none"
                  >
                    <Card
                      sx={{
                        bgcolor: "grey.300",
                        boxShadow: 2,
                        "&:hover": { bgcolor: "grey.400" },
                      }}
                    >
                      <CardContent>
                        <Typography variant="h6">
                          {discipline.name} ({discipline.year})
                        </Typography>
                      </CardContent>
                    </Card>
                  </Link>
                ))}
              </Stack>
            )}
          </CardContent>
        </Card>
      </Stack>
    </Container>
  );
}
