"use client";
import AppBar from "@mui/material/AppBar";
import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import Button from "@mui/material/Button";
import Avatar from "@mui/material/Avatar";
import Link from "@mui/material/Link";
import NextLink from "next/link";
import { Principal } from "@/types/dto";
// import { useRouter } from "next/navigation";
import { Box } from "@mui/material";

const Header = ({ user }: { user: Principal | undefined }) => {
  // const router = useRouter();

  // async function handleLogOut() {
  //   await fetch(`${process.env.NEXT_PUBLIC_API_URL}/auth/logout`, {
  //     method: "POST",
  //     credentials: "include",
  //   });
  //   router.push("/");
  //   router.refresh();
  // }

  return (
    <AppBar position="static">
      <Toolbar className="flex gap-5 justify-between">
        <Link component={NextLink} href="/" color="inherit" underline="none">
          <Typography variant="h6">Головна</Typography>
        </Link>
        {user ? (
          <Box className="w-full flex justify-between items-center">
            <Box className="flex gap-5">
              {user.authorities.find((el) => el.authority === "ROLE_ADMIN") && (
                <Link
                  component={NextLink}
                  href="/admin"
                  color="inherit"
                  underline="none"
                >
                  <Typography variant="h6">Адмінка</Typography>
                </Link>
              )}
              {user.authorities.find(
                (el) => el.authority === "ROLE_STUDENT"
              ) && (
                <Link
                  component={NextLink}
                  href="/student"
                  color="inherit"
                  underline="none"
                >
                  <Typography variant="h6">Мої роботи</Typography>
                </Link>
              )}
              {user.authorities.find(
                (el) => el.authority === "ROLE_MANAGER"
              ) && (
                <Link
                  component={NextLink}
                  href="/manager"
                  color="inherit"
                  underline="none"
                >
                  <Typography variant="h6">Керування дисциплінами</Typography>
                </Link>
              )}
              {user.authorities.find(
                (el) => el.authority === "ROLE_SUPERVISOR"
              ) && (
                <Link
                  component={NextLink}
                  href="/supervisor"
                  color="inherit"
                  underline="none"
                >
                  <Typography variant="h6">Поточні роботи</Typography>
                </Link>
              )}
            </Box>
            <Box className="flex items-center gap-2">
              <Avatar
                src={user.attributes.picture}
                alt={user.attributes.name}
              />
              <Typography variant="body1">{user.attributes.email}</Typography>
              <Button color="inherit">
                <NextLink
                  href={`${process.env.NEXT_PUBLIC_API_URL}/auth/logout`}
                >
                  Вийти
                </NextLink>
              </Button>
            </Box>
          </Box>
        ) : (
          <NextLink href={`${process.env.NEXT_PUBLIC_API_URL}/auth/login`}>
            <Button color="inherit">Увійти</Button>
          </NextLink>
        )}
      </Toolbar>
    </AppBar>
  );
};

export default Header;
