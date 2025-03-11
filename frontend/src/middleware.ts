import { NextRequest, NextResponse } from "next/server";
import { verifySession } from "@/utils/dal";
import { cookies } from "next/headers";

export default async function middleware(req: NextRequest) {
  const path = req.nextUrl.pathname;
  const isPublicRoute = path === "/";
  const user = await verifySession();

  if (isPublicRoute) {
    return NextResponse.next();
  }

  if (!user) {
    const cookieStore = await cookies();
    console.warn("User not authenticated");
    cookieStore.delete("JSESSIONID");
    cookieStore.delete("XSRF_TOKEN");

    return NextResponse.redirect(
      `${process.env.NEXT_PUBLIC_API_URL}/public/login`
    );
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    "/((?!api|_next/static|_next/image|icons|favicon.ico|public|.*\\.png$).*)",
  ],
};
