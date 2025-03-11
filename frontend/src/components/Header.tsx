"use client";
import AppBar from "@mui/material/AppBar";
import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import Button from "@mui/material/Button";
import Avatar from "@mui/material/Avatar";
import Link from "@mui/material/Link";
import NextLink from "next/link";
import { User } from "@/types/dto";
import { useRouter } from "next/navigation";

const Header = ({ user }: { user: User | undefined }) => {
  const router = useRouter();

  async function handleLogOut() {
    await fetch(`${process.env.NEXT_PUBLIC_API_URL}/logout`, {
      method: "POST",
      credentials: "include",
    });
    router.push("/");
    router.refresh();
  }

  return (
    <AppBar position="static">
      <Toolbar className="flex justify-between">
        <Link component={NextLink} href="/" color="inherit" underline="none">
          <Typography variant="h6">Home</Typography>
        </Link>
        {user ? (
          <div className="flex items-center gap-2">
            <Avatar src={user.picture} alt={user.name} />
            <Typography variant="body1">Welcome, {user.email}</Typography>
            <Button color="inherit" onClick={handleLogOut}>
              Logout
            </Button>
          </div>
        ) : (
          <NextLink href={`${process.env.NEXT_PUBLIC_API_URL}/public/login`}>
            <Button color="inherit">Login</Button>
          </NextLink>
        )}
      </Toolbar>
    </AppBar>
  );
};

export default Header;
