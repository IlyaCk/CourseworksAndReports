"use client";

import React, { useState, FC, useRef, useEffect } from "react";
import { DndProvider, useDrag, useDrop } from "react-dnd";
import { HTML5Backend } from "react-dnd-html5-backend";
import { Box, Chip, Typography, Stack, Paper } from "@mui/material";
import { Template } from "@/types/dto";

type BlockType = Template;

interface DraggableBlock {
  type: "block";
  blockType: BlockType;
}

const allBlocks: Record<BlockType, string> = {
  DISCIPLINE: "Назва дисципліни",
  GROUP: "Група студента",
  STUDENT: "Прізвище та ініціали",
  TYPE: "Тип файлу",
};

const Block: FC<{ blockType: BlockType; isDisabled?: boolean }> = ({
  blockType,
  isDisabled = false,
}) => {
  const [{ isDragging }, drag] = useDrag({
    type: "block",
    item: { blockType, type: "block" } as DraggableBlock,
    collect: (monitor) => ({
      isDragging: monitor.isDragging(),
    }),
    canDrag: !isDisabled,
  });
  const chipref = useRef<HTMLDivElement>(null);
  drag(chipref);

  return (
    <Chip
      ref={chipref}
      label={allBlocks[blockType]}
      sx={{
        opacity: isDragging || isDisabled ? 0.5 : 1,
        cursor: isDisabled ? "default" : "grab",
        m: 0.5,
      }}
    />
  );
};

const DroppableArea: FC<{
  template: BlockType[];
  onDrop: (blockType: BlockType) => void;
  onRemove: (index: number) => void;
}> = ({ template, onDrop, onRemove }) => {
  const [, drop] = useDrop({
    accept: "block",
    drop: (item: DraggableBlock) => onDrop(item.blockType),
  });
  const paperRef = useRef<HTMLDivElement>(null);
  drop(paperRef);

  return (
    <Paper
      ref={paperRef}
      variant="outlined"
      sx={{
        p: 2,
        minHeight: 80,
        display: "flex",
        alignItems: "center",
        flexWrap: "wrap",
      }}
    >
      {template.length === 0 ? (
        <Typography variant="body2" color="text.secondary">
          Перетягніть блоки сюди для створення шаблону
        </Typography>
      ) : (
        template.map((blockType, index) => (
          <Chip
            key={index}
            label={allBlocks[blockType]}
            onDelete={() => onRemove(index)}
            sx={{ m: 0.5 }}
          />
        ))
      )}
    </Paper>
  );
};

interface FilenameTemplateBuilderProps {
  value: BlockType[];
  onChange: (template: BlockType[]) => void;
}

const FilenameTemplateBuilder: FC<FilenameTemplateBuilderProps> = ({
  value,
  onChange,
}) => {
  const [template, setTemplate] = useState<BlockType[]>(value || []);

  useEffect(() => {
    onChange(template);
  }, [template, onChange]);

  const handleDrop = (blockType: BlockType) => {
    if (!template.includes(blockType)) {
      const newTemplate = [...template, blockType];
      setTemplate(newTemplate);
    }
  };

  const handleRemove = (index: number) => {
    const newTemplate = [...template];
    newTemplate.splice(index, 1);
    setTemplate(newTemplate);
  };

  const usedBlocks = new Set(template);

  return (
    <DndProvider backend={HTML5Backend}>
      <Stack spacing={2}>
        <Typography variant="h6">Шаблон імені файлу</Typography>
        <Box sx={{ display: "flex", flexWrap: "wrap" }}>
          {Object.keys(allBlocks).map((key) => {
            const blockType = key as BlockType;
            return (
              <Block
                key={blockType}
                blockType={blockType}
                isDisabled={usedBlocks.has(blockType)}
              />
            );
          })}
        </Box>

        <DroppableArea
          template={template}
          onDrop={handleDrop}
          onRemove={handleRemove}
        />
        <Typography variant="subtitle1" sx={{ pt: 2 }}>
          Поточний шаблон:{" "}
          <strong>
            {template.map((block) => `{${block}}`).join("_") || "[порожньо]"}
          </strong>
        </Typography>
      </Stack>
    </DndProvider>
  );
};

export default FilenameTemplateBuilder;
