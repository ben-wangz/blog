import { defineUserConfig } from "vuepress";
import theme from "./theme.js";

export default defineUserConfig({
  base: "/",

  lang: "en-US",
  title: "GeekCity",
  description: "A blog for ben.wangz",

  theme,

  // Enable it with pwa
  // shouldPrefetch: false,
});
