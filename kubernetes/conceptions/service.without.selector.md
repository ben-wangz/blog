## service without selector

### usage

1. can be used to import services out of the cluster into the cluster.
2. with this approach, components in the cluster can communicate with the services outside in the same way as the
   services in the cluster
3. decoupling the dependencies(services) and the components makes it easier to be compatible to most environments

### normal service

1. example
    * ```yaml
      apiVersion: v1
      kind: Service
      metadata:
        name: my-service
      spec:
        selector:
          app: MyApp
        ports:
          - protocol: TCP
            port: 80
            targetPort: 9376
      ```
2. the example service will transport requests to the pods contains the label "app: MyApp" which is defined by
   the `selector`

### service without selector

1. example
    * ```yaml
      apiVersion: v1
      kind: Service
      metadata:
        name: my-service
      spec:
        ports:
          - protocol: TCP
            port: 80
            targetPort: 9376
      ---
      apiVersion: v1
      kind: Endpoints
      metadata:
        name: my-service
      subsets:
        - addresses:
            - ip: 192.0.2.42
          ports:
            - port: 9376
      ```
2. the name of `Service` and `Endpoints` should keep the same
3. the `targetPort` of the service should point to one of the port in `Endpoints`
4. the endpoint IPs must not be
    * `loopback` (127.0.0.0/8 for IPv4, ::1/128 for IPv6)
    * `link-local` (169.254.0.0/16 and 224.0.0.0/24 for IPv4, fe80::/64 for IPv6).
    * the cluster IPs of other Kubernetes Services
        + because kube-proxy doesn't support virtual IPs as a destination

### what's more

1. in fact, k8s will create `Endpoints` automatically for the `Service` with `selector`
2. example
    * ```yaml
      apiVersion: v1
      kind: Endpoints
      metadata:
        name: my-service
      subsets:
        - addresses:
            - ip: 10.244.0.54
              nodeName: kind-control-plane
              targetRef:
                kind: Pod
                name: my-deployment-d7b5f5967-2jcqp
          ports:
            - name: http
              port: 8000
              protocol: TCP
      ```
3. for more details: https://kubernetes.io/docs/concepts/services-networking/service
