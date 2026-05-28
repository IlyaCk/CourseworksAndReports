"use client";

import { Notification } from "@/types/dto";
import { Box, Divider, List, ListItem, ListItemText } from "@mui/material";
import NextLink from "next/link";
import { useRouter } from "next/navigation";

export default function NotificationsList({
  notifications,
}: {
  notifications: Notification[];
}) {
  const router = useRouter();
  return (
    <List>
      {notifications.length === 0 && (
        <ListItem>
          <ListItemText primary="Немає сповіщень" />
        </ListItem>
      )}
      {notifications.map((notification) => (
        <Box key={notification.id}>
          <ListItem
            component={NextLink}
            href={notification.targetUrl}
            sx={{
              backgroundColor:
                notification.type === "WORK_ABORTED"
                  ? "#f76a6a"
                  : notification.read
                  ? "white"
                  : "#f0f4ff",
            }}
            onClick={async () => {
              await fetch(
                `${process.env.NEXT_PUBLIC_API_URL}/notifications/${notification.id}/mark-as-read`,
                { method: "POST", credentials: "include" }
              );
              router.refresh();
            }}
          >
            <ListItemText
              primary={notification.message}
              secondary={new Date(notification.createdAt).toLocaleString()}
            />
            <Divider component={"li"} />
          </ListItem>
          <Divider component={"li"} />
        </Box>
      ))}
    </List>
  );
}
