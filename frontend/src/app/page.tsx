import { Collection } from "@/types/dto";
import {
  Link,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Button,
  Typography,
  Box,
} from "@mui/material";
import { cookies } from "next/headers";
import NextLink from "next/link";

export default async function Home() {
  const cookieStore = await cookies();
  const response = await fetch(
    `${process.env.NEXT_PUBLIC_API_URL}/collections/`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Cookie: `JSESSIONID=${cookieStore.get("JSESSIONID")?.value}`,
      },
    }
  );
  const collections: Collection[] = response.ok ? await response.json() : [];

  return (
    <Box className="p-5 flex items-center justify-center flex-col">
      <Box display="flex" justifyContent="center" my={5} gap={5}>
        <Typography variant="h4">Collections</Typography>
        <Button variant="contained">Add +</Button>
      </Box>
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Collection Name</TableCell>
              <TableCell>Course ID</TableCell>
              <TableCell>Coursework ID</TableCell>
              <TableCell>Updated At</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {collections?.map((collection, index) => (
              <TableRow key={index}>
                <TableCell>
                  <Link
                    component={NextLink}
                    href={`/collections/${collection.id}`}
                  >
                    {collection.name}
                  </Link>
                </TableCell>
                <TableCell>{collection.id}</TableCell>
                <TableCell>{collection.taskId}</TableCell>
                <TableCell>
                  {new Date(collection.updatedAt).toLocaleString()}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
}
