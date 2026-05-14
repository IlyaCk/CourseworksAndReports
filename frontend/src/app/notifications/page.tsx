import {
  Typography,
  Paper,
  Box,
} from "@mui/material";
import { Notification } from "@/types/dto";
import { cookies } from "next/headers";
import { Metadata } from "next";
import ReadAllNotificationsButton from "@/components/ReadAllNotificationsButton";
import NotificationsList from "@/components/NotificationsList";

export const metadata: Metadata = {
  title: "Сповіщення",
};

export default async function NotificationsPage() {
  let notifications: Notification[] = [];
  const cookieStore = await cookies();

  try {
    const res = await fetch(
      `${process.env.NEXT_PUBLIC_API_URL}/notifications/`,
      {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Cookie: `JSESSIONID=${cookieStore.get("JSESSIONID")?.value}`,
        },
      }
    );

    if (!res.ok) {
      throw new Error("Failed to fetch notifications");
    }

    notifications = await res.json();
  } catch (error) {
    console.error("Error fetching notifications:", error);
  }

  return (
    <Box className="p-5">
      <Typography
        variant="h4"
        gutterBottom
        sx={{ display: "flex", justifyContent: "space-between" }}
      >
        Сповіщення
        <ReadAllNotificationsButton />
      </Typography>

      <Paper>
        <NotificationsList notifications={notifications} />
      </Paper>
    </Box>
  );
}
