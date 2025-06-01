# Asymmetric Encryption

## Generate keys

- generate private key

```sh
openssl genpkey -algorithm RSA -out private_key.pem -pkeyopt rsa_keygen_bits:2048
```

- generate public key using above private key

```sh
openssl rsa -pubout -in private_key.pem -out public_key.pem
```

- convert private key to base 64 (to further encrypt and store into config server)

```sh
openssl pkcs8 -topk8 -nocrypt -in private_key.pem -outform DER | base64 -w 0
```

- put above base64 private key into [config-repo](./config-repo/)/[application.yml](./config-repo/application.yml)

- and put public_key into
  - handler > [resources](./handler/src/main/resources/keys/pass_encryption_public_key.pem)
  - ui > [public](./ui/public/public_key.pem)

## initialize local in [config-repo](./config-repo/)

```sh
git init
git branch -m master
git add .
git commit -m "init"
```
