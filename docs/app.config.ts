export default defineAppConfig({
  docus: {
    title: 'GeekCity',
    description: 'blog of ben.wangz',
    image: 'https://avatars.githubusercontent.com/u/9364314?s=400&u=606dda5bb4b2b65a0b0dd8e9228653467be3281b&v=4',
    socials: {
      github: 'ben-wangz/blog',
      nuxt: {
        label: 'geekcity',
        icon: 'simple-icons:nuxtdotjs',
        href: 'https://blog.geekcity.tech'
      }
    },
    github: {
      dir: '/',
      branch: 'main',
      repo: 'blog',
      owner: 'ben.wangz',
      edit: true
    },
    aside: {
      level: 0,
      collapsed: false,
      exclude: []
    },
    main: {
      padded: true,
      fluid: true
    },
    header: {
      logo: true,
      showLinkIcon: true,
      exclude: [],
      fluid: true
    },
    footer: {
      credits: {
        icon: 'IconDocus',
        text: 'Powered by ben.wangz',
        href: 'https://github.com/ben-wangz',
      },
      textLinks: [
        {
          text: '浙ICP备2021024222号',
          href: 'https://beian.miit.gov.cn/',
          target: '_blank',
          rel: 'noopener'
        }
      ]
    }
  }
})
