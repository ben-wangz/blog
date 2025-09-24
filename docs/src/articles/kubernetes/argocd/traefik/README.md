# Traefik

## introduction

Traefik is a modern HTTP reverse proxy and load balancer designed specifically to simplify the deployment of microservices and containerized applications. Compared with traditional reverse proxy tools (such as Nginx and Apache), it offers stronger automation capabilities and cloud-native features.

## prerequisites

* a domain name controlled by aliyun, in this example, it's `dashboard.traefik.dev.geekcity.tech`

## installation

1. configure aliyun ram
    * create a user, in this example, it's `traefik-dns`
    * create a policy, in this example, it's `traefik-dns`
        + ```json
          {
            "Version": "1",
            "Statement": [
              {
                "Effect": "Allow",
                "Action": [
                  "alidns:AddDomainRecord",
                  "alidns:DeleteDomainRecord"
                ],
                "Resource": "acs:alidns:*:*:domain/geekcity.tech"
              },
              {
                "Effect": "Allow",
                "Action": [
                  "alidns:DescribeDomains",
                  "alidns:DescribeDomainRecords"
                ],
                "Resource": "acs:alidns:*:*:domain/*"
              }
            ]
          }
          ```
    * bind the policy `traefik-dns` to the user `traefik-dns`
2. create a secret named `traefik-aliyun-dns-credentials` which store the access key and access secret of the user `traefik-dns`
    * ```shell
      #export ACCESS_KEY_ID=access_key_id_of_traefik_dns
      #export ACCESS_KEY_SECRET=access_key_secret_of_traefik_dns
      kubectl -n traefik create secret generic traefik-aliyun-dns-credentials \
        --from-literal=access-key="$ACCESS_KEY_ID" \
        --from-literal=secret-key="$ACCESS_KEY_SECRET"
      ```
3. install or update Traefik
    * (optional, only for k3s,) remove traefik installed in k3s by default
        + ```shell
          kubectl delete -f /var/lib/rancher/k3s/server/manifests/traefik.yaml
          ```
    * prepare `traefik.app.yaml`
        + ```yaml
          <!-- @include: traefik.app.yaml -->
          ```
        + NOTES
            * service.type=LoadBalancer, make sure the cluster has a load balancer controller
            * if service.type=NodePort, make sure the nodes of the cluster has `ExternalIP`, because The ExternalIP addresses of the nodes in the cluster will be propagated to the ingress status.
            * reference: [traefik-docs-ingress-endpoint-publishd-service](https://doc.traefik.io/traefik/reference/install-configuration/providers/kubernetes/kubernetes-ingress/#ingressendpointpublishedservice)
    * apply to k8s
        + ```shell
          kubectl -n argocd apply -f traefik.app.yaml
          ```
    * sync the application
        + ```shell
          argocd app sync argocd/traefik \
              && argocd app wait argocd/traefik
          ```

## check traefik dashboard

* ```shell
  curl -L https://dashboard.traefik.dev.geekcity.tech
  ```

## advanced topics

1. [secure with middleware](secure-with-middleware.md)
