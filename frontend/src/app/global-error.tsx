"use client";

import { Typography, Container, Box, Paper } from "@mui/material";

export default function GlobalError({ error }: { error: Error }) {
  return (
    <html>
      <body>
        <Container maxWidth="sm">
          <Box
            minHeight="100vh"
            display="flex"
            justifyContent="center"
            alignItems="center"
          >
            <Paper elevation={0} sx={{ p: 4, textAlign: "center" }}>
              <Typography variant="h4" color="textPrimary" gutterBottom>
                Щось пішло не так. Перезавантажте сторінку.
              </Typography>
              <Typography variant="body1" color="error" gutterBottom>
                {error.message}
              </Typography>
            </Paper>
          </Box>
        </Container>
      </body>
    </html>
  );
}
