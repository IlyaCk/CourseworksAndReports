"use client";

import { useEffect } from "react";
import { toast } from "react-toastify";
import { useSearchParams, usePathname, useRouter } from "next/navigation";

export default function LoginNotification() {
  const searchParams = useSearchParams();
  const pathname = usePathname();
  const router = useRouter();

  useEffect(() => {
    const login = searchParams.get("login");
    const logout = searchParams.get("logout");

    if (login === "success") {
      toast.success("Успішний вхід!");
    } else if (login === "insufficient_scopes") {
      toast.error("Недостатньо прав! Будь ласка, надайте всі дозволи.");
    }

    if (logout === "success") {
      toast.info("Ви вийшли з акаунту.");
    }

    if (login || logout) {
      const newUrl = pathname;
      router.replace(newUrl, { scroll: false });
    }
  }, [searchParams, pathname, router]);

  return null;
}
