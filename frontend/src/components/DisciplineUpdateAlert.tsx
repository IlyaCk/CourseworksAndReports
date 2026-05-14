"use client";

import { useEffect, useRef, useState } from "react";
import { Alert, CircularProgress } from "@mui/material";
import { useRouter } from "next/navigation";

type Props = {
  disciplineId: number;
};

export default function DisciplineUpdateAlert({ disciplineId }: Props) {
  const [waiting, setWaiting] = useState(true);
  const router = useRouter();

  const cancelRef = useRef(false);

  useEffect(() => {
    cancelRef.current = false;

    const waitForUpdate = async () => {
      const res = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/manager/disciplines/${disciplineId}/wait-update`,
        {
          method: "GET",
          credentials: "include",
        }
      );
      const result = await res.json();

      if (!cancelRef.current && result) {
        setWaiting(false);
        router.refresh();
      } else if (!cancelRef.current) {
        setTimeout(waitForUpdate, 2000);
      }
    };

    waitForUpdate();

    return () => {
      cancelRef.current = true;
    };
  }, [disciplineId, router]);

  if (!waiting) return null;

  return (
    <Alert severity="info" icon={<CircularProgress size={16} />} sx={{ mt: 2 }}>
      Оновлення робіт триває… будь ласка, зачекайте
    </Alert>
  );
}
