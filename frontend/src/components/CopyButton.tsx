"use client";
import React from "react";
import { Button } from "@mui/material";

interface CopyButtonProps {
  textToCopy: string;
}

const CopyButton = ({ textToCopy }: CopyButtonProps) => {
  const handleCopy = (text: string) => {
    navigator.clipboard.writeText(text);
  };

  return (
    <Button
      size="small"
      onClick={() => handleCopy(textToCopy)}
      sx={{ width: "120px" }}
    >
      скопіювати
    </Button>
  );
};

export default CopyButton;
