import "server-only";
import { cookies } from "next/headers";
import { Principal } from "@/types/dto";

export const verifySession = async () => {
  const cookieStore = await cookies();

  async function getUser() {
    const authResponse = await fetch(
      `${process.env.NEXT_PUBLIC_API_URL}/auth/me`,
      {
        method: "GET",
        headers: {
          Cookie: `JSESSIONID=${cookieStore.get("JSESSIONID")?.value}`,
        },
      }
    );
    if (authResponse.ok) {
      return await authResponse?.json();
    }
  }

  if (
    cookieStore.has("JSESSIONID") &&
    cookieStore.get("JSESSIONID")?.value != ""
  ) {
    const user: Principal = await getUser();
    return user;
  }
};
