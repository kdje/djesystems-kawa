import type { ReactNode } from "react";

type IconName = "home" | "card" | "retailers" | "bell" | "profile" | "shield" | "arrow" | "check";

type AppIconProps = {
  name: IconName;
  size?: number;
};

const paths: Record<IconName, ReactNode> = {
  home: <><path d="M3 10.5 12 3l9 7.5"/><path d="M5.5 9.5V21h13V9.5"/><path d="M9 21v-7h6v7"/></>,
  card: <><rect x="3" y="5" width="18" height="14" rx="3"/><path d="M3 9h18"/><path d="M7 15h4"/></>,
  retailers: <><path d="M4 10h16"/><path d="M5 10 7 4h10l2 6"/><path d="M6 10v10h12V10"/><path d="M9 20v-5h6v5"/></>,
  bell: <><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9"/><path d="M10 21h4"/></>,
  profile: <><circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0 1 16 0"/></>,
  shield: <><path d="M12 3 5 6v5c0 4.8 2.7 8 7 10 4.3-2 7-5.2 7-10V6l-7-3Z"/><path d="m9 12 2 2 4-4"/></>,
  arrow: <><path d="M5 12h14"/><path d="m14 7 5 5-5 5"/></>,
  check: <path d="m5 12 4 4L19 6"/>,
};

export function AppIcon({ name, size = 20 }: AppIconProps) {
  return (
    <svg
      viewBox="0 0 24 24"
      width={size}
      height={size}
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      {paths[name]}
    </svg>
  );
}
