import { Metadata, NextPage } from "next";
import dynamic from "next/dynamic";
const AdminApp = dynamic(() => import("@/components/admin/AdminApp"), {
  ssr: !!false,
});

export const metadata: Metadata = {
  title: "Адмін",
};

const Admin: NextPage = () => {
  return <AdminApp />;
};

export default Admin;
