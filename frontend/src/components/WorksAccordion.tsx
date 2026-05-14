"use client";

import {
  Accordion,
  AccordionSummary,
  Typography,
  AccordionDetails,
} from "@mui/material";
import { DisciplineDTO } from "@/types/dto";
import { GridExpandMoreIcon } from "@mui/x-data-grid";
import WorksTable from "./WorksTable";

export default function WorksAccordion({
  discipline,
}: {
  discipline: DisciplineDTO;
}) {
  return (
    <Accordion sx={{ mt: 2 }}>
      <AccordionSummary expandIcon={<GridExpandMoreIcon />}>
        <Typography fontWeight="bold">
          {discipline.name} — {discipline.year}
        </Typography>
      </AccordionSummary>
      <AccordionDetails>
        <WorksTable sortedWorks={discipline.works} />
      </AccordionDetails>
    </Accordion>
  );
}
