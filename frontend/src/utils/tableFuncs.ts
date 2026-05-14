export const getMatchLabel = (value: string): string => {
  switch (value) {
    case "HIGH":
      return "Висока";
    case "MEDIUM":
      return "Середня";
    case "LOW":
      return "Низька";
    case "NOT_MATCHED":
    default:
      return "Немає";
  }
};

export const getMatchColor = (
  value: string
): "success" | "warning" | "default" | "error" => {
  switch (value) {
    case "HIGH":
      return "success";
    case "MEDIUM":
      return "warning";
    case "LOW":
      return "default";
    case "NOT_MATCHED":
    default:
      return "error";
  }
};
