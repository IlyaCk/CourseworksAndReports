import { NextRequest, NextResponse } from "next/server";
import { verifySession } from "@/utils/dal";
import { cookies } from "next/headers";

export default async function middleware(req: NextRequest) {
  const path = req.nextUrl.pathname;
  const isPublicRoute = path === "/" || path.startsWith("/departments");
  const user = await verifySession();

  if (isPublicRoute) {
    return NextResponse.next();
  }

  if (!user && path !== "/unauthorized") {
    const cookieStore = await cookies();
    cookieStore.delete("JSESSIONID");
    cookieStore.delete("XSRF_TOKEN");

    // return NextResponse.redirect(
    //   `${process.env.NEXT_PUBLIC_API_URL}/auth/login`
    // );

    return NextResponse.redirect(new URL("/unauthorized", req.url));
  }

  const roleBasedRoutes: { [key: string]: string } = {
    "/admin": "ROLE_ADMIN",
    "/student": "ROLE_STUDENT",
    "/manager": "ROLE_MANAGER",
    "/supervisor": "ROLE_SUPERVISOR",
  };

  const requiredRole =
    roleBasedRoutes[
      Object.keys(roleBasedRoutes).find((route) => path.startsWith(route))!
    ];

  if (
    requiredRole &&
    !user?.authorities.find((el) => el.authority === requiredRole)
  ) {
    return NextResponse.redirect(new URL("/forbidden", req.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    "/((?!api|_next/static|_next/image|favicon.ico|sitemap.xml|robots.txt).*)",
  ],
};
