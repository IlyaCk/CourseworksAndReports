import { Box, Button, Container, Typography } from "@mui/material";
import { Metadata } from "next";

export const metadata: Metadata = {
  title: "Unathorized",
};

export default function UnauthorizedPage() {
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
        <Typography variant="h1" fontWeight="bold" color="error">
          401
        </Typography>
        <Typography variant="h4" fontWeight="medium" mt={2}>
          Ви не авторизовані
        </Typography>
        <Typography variant="body1" color="textSecondary" mt={1} mb={3}>
          Будь ласка, увійдіть у систему, щоб отримати доступ до цієї сторінки.
        </Typography>
        <Button
          href={`${process.env.NEXT_PUBLIC_API_URL}/auth/login`}
          variant="contained"
          size="large"
          sx={{ mt: 2 }}
        >
          Увійти
        </Button>
      </Box>
    </Container>
  );
}
