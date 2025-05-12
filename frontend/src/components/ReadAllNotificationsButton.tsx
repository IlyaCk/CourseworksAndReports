"use client";

import { Button } from "@mui/material";
import { useRouter } from "next/navigation";
import { useState } from "react";

export default function ReadAllNotificationsButton() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  return (
    <Button
      variant="outlined"
      color="primary"
      loading={loading}
      onClick={async () => {
        setLoading(true);
        await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/notifications/mark-all-as-read`,
          {
            method: "POST",
            credentials: "include",
          }
        );
        router.refresh();
        setLoading(false);
      }}
    >
      Позначити все як прочитане
    </Button>
  );
}
