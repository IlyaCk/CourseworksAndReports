"use client";
import { Discipline } from "@/types/dto";
import { Button } from "@mui/material";
import { useRouter } from "next/navigation";
import DownloadIcon from "@mui/icons-material/Download";

const WorksActionButtons = ({ discipline }: { discipline: Discipline }) => {
  const router = useRouter();

  return (
    <Button
      variant="outlined"
      onClick={() =>
        router.push(`/manager/disciplines/${discipline.id}/export`)
      }
      sx={{ position: "absolute", right: 16, top: 16 }}
      startIcon={<DownloadIcon />}
    >
      Експортувати
    </Button>
  );
};

export default WorksActionButtons;
