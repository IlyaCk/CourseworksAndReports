import { Box, Button, Container, Typography } from "@mui/material";
import { Metadata } from "next";
import NextLink from "next/link";

export const metadata: Metadata = {
  title: "Not Found",
};

export default function NotFoundPage() {
  return (
    <Container maxWidth="md">
      <Box
        display="flex"
        flexDirection="column"
        justifyContent="center"
        alignItems="center"
        height="calc(100vh - 64px)"
        textAlign="center"
      >
        <Typography variant="h1" fontWeight="bold" color="primary">
          404
        </Typography>
        <Typography variant="h4" fontWeight="medium" mt={2}>
          Сторінку не знайдено
        </Typography>
        <Typography variant="body1" color="textSecondary" mt={1} mb={3}>
          Здається, такої сторінки не існує. Перевірте адресу або поверніться на
          головну.
        </Typography>
        <Button
          component={NextLink}
          href="/"
          variant="contained"
          size="large"
          sx={{ mt: 2 }}
        >
          На головну
        </Button>
      </Box>
    </Container>
  );
}
