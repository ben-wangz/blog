import { viteBundler } from '@vuepress/bundler-vite'
import { defineUserConfig } from "vuepress";
import theme from "./theme.js";

export default defineUserConfig({
  base: "/",

  lang: "en-US",
  title: "GeekCity",
  description: "A blog for ben.wangz",
  dest: "${sourceDir}/../build/dist",

  theme,

  bundler: viteBundler({
    viteOptions: {},
    vuePluginOptions: {},
  }),

  // Enable it with pwa
  // shouldPrefetch: false,
});
