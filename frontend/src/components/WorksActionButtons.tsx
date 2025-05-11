"use client";
import { Discipline } from "@/types/dto";
import { Button } from "@mui/material";
import DownloadIcon from "@mui/icons-material/Download";
import RateReviewIcon from "@mui/icons-material/RateReview";
import NextLink from "next/link";
import { useState } from "react";
import UploadReportsModal from "./UploadReportsModal";
import AddIcon from "@mui/icons-material/Add";

const WorksActionButtons = ({ discipline }: { discipline: Discipline }) => {
  const [openModal, setOpenModal] = useState(false);
  return (
    <>
      <Button
        variant="outlined"
        component={NextLink}
        href={`/manager/disciplines/${discipline.id}/export`}
        sx={{ position: "absolute", right: 16, top: 16 }}
        startIcon={<DownloadIcon />}
        disabled={discipline.updating}
      >
        Експортувати
      </Button>
      <Button
        variant="outlined"
        component={NextLink}
        href={`/manager/disciplines/${discipline.id}/review`}
        sx={{ position: "absolute", right: 200, top: 16 }}
        startIcon={<RateReviewIcon />}
        disabled={discipline.updating}
      >
        Забрати на перевірку
      </Button>
      <UploadReportsModal
        open={openModal}
        onClose={() => setOpenModal(false)}
        disciplineId={discipline.id}
      />
      <Button
        onClick={() => setOpenModal(true)}
        sx={{ position: "absolute", right: 442, top: 16 }}
        startIcon={<AddIcon />}
        variant="outlined"
      >
        Додати звіти перевірки
      </Button>
    </>
  );
};

export default WorksActionButtons;
