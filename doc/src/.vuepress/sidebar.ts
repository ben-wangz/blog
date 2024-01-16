import { sidebar } from "vuepress-theme-hope";

export default sidebar({
  "/": [
    "",
    {
      text: "Articles",
      icon: "laptop-code",
      prefix: "posts/",
      children: "structure",
    },
    "intro",
  ],
});
