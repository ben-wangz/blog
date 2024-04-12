# clash

## server

1. mkdir to store config file
    * ```shell
      mkdir -p clash
      ```
    * remember to put your own config.yaml into the `$(pwd)/clash/` directory
2. start server 
    * ```shell
      podman run --rm \
          --name clash \
          -p 7890:7890 \
          -p 9090:9090 \
          -v $(pwd)/clash:/root/.config/clash \
          -d docker.io/dreamacro/clash:v1.18.0
      ```
3. start clash dashboard(pure static web)
    * ```shell
      podman run --rm \
          --name clash-dashboard \
          -p 8080:80 \
          -d docker.io/haishanh/yacd:master
      ```
4. open browser and visit `http://localhost:8080`
5. proxy settings with command line
    * ```shell
      export https_proxy=http://127.0.0.1:7890 http_proxy=http://127.0.0.1:7890 all_proxy=socks5://127.0.0.1:7890
      ```

## references

* https://github.com/ben-wangz/clash
* https://github.com/ben-wangz/yacd
