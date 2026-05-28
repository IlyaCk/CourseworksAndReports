import { Layout, useSidebarState } from "react-admin";
import AdminAppBar from "./AdminAppBar";
import { useEffect } from "react";

const AdminLayout = ({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) => {
  // TO FIX: mobile sidebar is open ALL TIME
  const [, setOpen] = useSidebarState();
  useEffect(() => {
    setOpen(true);
  });

  return (
    <Layout
      appBar={AdminAppBar}
      sx={{
        "& .RaLayout-appFrame": {
          marginTop: 0,
        },
        "& .RaSidebar-docked": {
          height: "calc(100vh - 64px)",
        },
        minHeight: "calc(100vh - 64px)",
      }}
    >
      {children}
    </Layout>
  );
};

export default AdminLayout;
