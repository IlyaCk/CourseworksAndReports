"use client";
import { Discipline } from "@/types/dto";
import { Button } from "@mui/material";
import { useRouter } from "next/navigation";
import DownloadIcon from "@mui/icons-material/Download";
import RateReviewIcon from "@mui/icons-material/RateReview";

const WorksActionButtons = ({ discipline }: { discipline: Discipline }) => {
  const router = useRouter();

  return (
    <>
      <Button
        variant="outlined"
        onClick={() =>
          router.push(`/manager/disciplines/${discipline.id}/export`)
        }
        sx={{ position: "absolute", right: 16, top: 16 }}
        startIcon={<DownloadIcon />}
        disabled={discipline.updating}
      >
        Експортувати
      </Button>
      <Button
        variant="outlined"
        onClick={() =>
          router.push(`/manager/disciplines/${discipline.id}/review`)
        }
        sx={{ position: "absolute", right: 200, top: 16 }}
        startIcon={<RateReviewIcon />}
        disabled={discipline.updating}
      >
        Забрати на перевірку
      </Button>
    </>
  );
};

export default WorksActionButtons;
