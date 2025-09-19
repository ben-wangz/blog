# Traefik

## introduction

Traefik is a modern HTTP reverse proxy and load balancer designed specifically to simplify the deployment of microservices and containerized applications. Compared with traditional reverse proxy tools (such as Nginx and Apache), it offers stronger automation capabilities and cloud-native features.

## installation

* Traefik is installed by default in k3s cluster
* Traefik can be installed by argocd with the configuration below
    + ```yaml
      <!-- @include: traefik.application.yaml -->
    ```
* the dashboard of traefik is recommended to be setup by CR
    + ```yaml
      apiVersion: traefik.containo.us/v1alpha1
      kind: IngressRoute
      metadata:
        name: traefik-dashboard
        namespace: traefik
      spec:
        entryPoints:
          - web
        routes:
          - match: Host(`dashboard.traefik.dev.geekcity.tech`)
            kind: Rule
            services:
              - name: api@internal
                port: 80
      ```
