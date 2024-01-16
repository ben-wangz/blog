import { navbar } from "vuepress-theme-hope";

export default navbar([
  "/",
  {
    text: "Articles",
    icon: "laptop-code",
    prefix: "/articles/",
    link: "/articles/README.md",
  },
]);
