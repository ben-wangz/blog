# s3fs with sidecar

## what

* mount s3 bucket with s3fs
* use sidecar to initialize s3fs
* share sub path of mounted fs to other containers

## assumptions

* s3
    + endpoint: http://minio.storage:9000
    + credentials can be found in the secret named `minio-credentials`
        * ```shell
          #MINIO_ROOT_PASSWORD=your_minio_root_password
          kubectl create secret generic minio-credentials \
              --from-literal=access-key=admin \
              --from-literal=access-secret=$MINIO_ROOT_PASSWORD
          ```
    + bucket: `bucket-to-mount`
        * tree of files
            + `/`
                - `file1`
                - `file2`
                - `project-foo`
                    * `file3`
                    * `file4`
                    * `dir-foo`
                        * `file7`
                - `project-bar`
                    * `file5`
                    * `file6`

## sharing `/project-foo` to other containers

1. prepare `s3fs-with-sidecar.yaml`
    * ```yaml
      <!-- @include: @src/articles/kubernetes/others/s3fs-with-sidecar.yaml -->
      ```
2. apply to k8s
    * ```shell
      kubectl apply -f s3fs-with-sidecar.yaml
      ```
3. check logs
    * ```shell
      kubectl logs -f deployment/s3fs-client-deployment -c volume-using-container
      ```
