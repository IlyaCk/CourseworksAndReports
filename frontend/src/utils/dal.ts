import "server-only";
import { cookies } from "next/headers";
import { User } from "@/types/dto";

export const verifySession = async () => {
  const cookieStore = await cookies();

  async function getUser() {
    const authResponse = await fetch(
      `${process.env.NEXT_PUBLIC_API_URL}/public/me`,
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
    const user: User = await getUser();
    return user;
  }
};
