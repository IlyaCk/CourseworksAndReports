import { Geist, Geist_Mono } from "next/font/google";
import { AppRouterCacheProvider } from "@mui/material-nextjs/v15-appRouter";
import { CssBaseline } from "@mui/material";
import "./globals.css";
import Header from "@/components/Header";
import { verifySession } from "@/utils/dal";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const user = await verifySession();
  return (
    <html lang="uk">
      <AppRouterCacheProvider>
        <CssBaseline />
        <body
          className={`${geistSans.variable} ${geistMono.variable} antialiased`}
        >
          <Header user={user} />
          {children}
        </body>
      </AppRouterCacheProvider>
    </html>
  );
}
