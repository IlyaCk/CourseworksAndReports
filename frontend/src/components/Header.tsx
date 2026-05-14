"use client";
import AppBar from "@mui/material/AppBar";
import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import Button from "@mui/material/Button";
import Avatar from "@mui/material/Avatar";
import Link from "@mui/material/Link";
import NextLink from "next/link";
import { Notification, Principal } from "@/types/dto";
// import { useRouter } from "next/navigation";
import { Box } from "@mui/material";
import NotificationsIcon from "@mui/icons-material/Notifications";
import Badge from "@mui/material/Badge";
import { useEffect, useState } from "react";

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

  const [notificationsCount, setNotificationsCount] = useState(0);
  useEffect(() => {
    const fetchNotifications = async () => {
      const res = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL}/notifications/`,
        {
          credentials: "include",
        }
      );
      const data: Notification[] = await res.json();
      setNotificationsCount(data.filter((el) => !el.read).length);
    };

    if (user) {
      fetchNotifications();
    }
  }, [user]);

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
            <Box className="flex items-center gap-3">
              <Link
                component={NextLink}
                href="/notifications"
                color="inherit"
                underline="none"
              >
                <Badge badgeContent={notificationsCount} color="error">
                  <NotificationsIcon style={{ color: "white" }} />
                </Badge>
              </Link>

              <Avatar
                src={user.attributes.picture}
                alt={user.attributes.name}
              />
              <Typography variant="body1">{user.attributes.email}</Typography>
             <Button 
                color="inherit" 
                href={`${process.env.NEXT_PUBLIC_API_URL}/auth/logout`}
              >
                Вийти
              </Button>
            </Box>
          </Box>
        ) : (
          <Button 
            color="inherit" 
            href={`${process.env.NEXT_PUBLIC_API_URL}/auth/login`}
          >
            Увійти
          </Button>
        )}
      </Toolbar>
    </AppBar>
  );
};

export default Header;
