# self-signed-certs

## extract certificate from server

* ```shell
  SERVER_NAME=minio-console.dev.geekcity.tech
  SERVER_IP=${SERVER_IP:-${SERVER_NAME}}
  SERVER_PORT=${SERVER_PORT:-443}
  openssl s_client \
      -servername ${SERVER_NAME} \
      -connect ${SERVER_IP}:${SERVER_PORT} \
      </dev/null 2>/dev/null \
      | sed -n '/^-----BEGIN CERT/,/^-----END CERT/p' > ${SERVER_NAME}-${SERVER_PORT}.crt
  ```

## print certificate as text

* ```shell
  openssl x509 -in ${SERVER_NAME}-${SERVER_PORT}.crt -text -noout
  ```

## import certificate to trust store

1. for ubuntu
    * ```shell
      sudo cp ${SERVER_NAME}-${SERVER_PORT}.crt /usr/local/share/ca-certificates/${SERVER_NAME}-${SERVER_PORT}.crt
      ```
    * ```shell
      sudo update-ca-certificates
      ```
2. for fedora
    * ```shell
      sudo cp ${SERVER_NAME}-${SERVER_PORT}.crt /etc/pki/ca-trust/source/anchors/${SERVER_NAME}-${SERVER_PORT}.crt
      ```
    * ```shell
      sudo update-ca-trust extract
      ```
3. tests
    * ```shell
      curl https://${SERVER_NAME}:${SERVER_PORT}
      ```
