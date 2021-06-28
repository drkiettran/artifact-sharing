# STIX 2.1 Artifact Sharing

This project implements a proof-of-concept that enables keeping and sharing a STIX 2.1
artifact object that may contains malware and/or dangerous code.

See OASIS STIX 2.1 specification here, `https://docs.oasis-open.org/cti/stix/v2.1/stix-v2.1.html`

Artifact is a type of the STIX Cyber-observable Object or SCO. The description of an `artifact` object
can be read from here `https://docs.oasis-open.org/cti/stix/v2.1/os/stix-v2.1-os.html#_4jegwl6ojbes`.

## Basic Description

- It is a Vertx application
- It is a web-based application: Mutual TLS

You can:
- Post content (artifact)
- Get content (artifact)

## Test cases
### Post artifact

```shell

curl --location --request POST 'https://localhost:9090/artifact' \
--header 'Content-Type: application/json' \
--data-raw '{
  "type": "artifact",
  "spec_version": "2.1",
  "id": "artifact--6f437177-6e48-5cf8-9d9e-872a2bddd641",
  "mime_type": "application/zip",
  "payload_bin": "ZX7HIBWPQA99NSUhEUgAAADI== ...",
  "encryption_algorithm": "mime-type-indicated",
  "decryption_key": "My voice is my passport"
}'
```

```json
{
    "status_code": 200,
    "reason": "OK"
}
```

#### Storing local encrypted copy

Encrypted content is stored locally as JSON document:

```json
{
  "id" : "artifact--6f437177-6e48-5cf8-9d9e-872a2bddd641",
  "encrypted" : "44MHQYOkFzmg3/cIAfb9AXZLODyF7kMBZmon43izWt0O+eaHoQKSgaEo1fgx5fpc9+ZTUz3s2ypPsyvgUZgV+3/rJdR5LjODYTS1x4hBBCWCW3Nt3/9KB8B3tAL6Ld8ffmapkW0LyB+MyXbYcHMzZcRF1YTTZPppEnh6m34r2rghGxrfN7mm98GM8Z6ilSfhQyZpM7TE2GiKpsIrESRjO4g8MDb4iJHi6gt777Ss5osN69V4B/BprW9P+XJfYBOfGF5Qr2I4/EeQS6Cm8bCevffk47VHWW2nGMpYezsadR9E4UZynos97+Bgv9NUYyLvjhZie1hgt4ExGnbM/cPxK4AYJse64R73KcEISLje6NJBOetRrTN628FgMhN9hXz8WvnZaIzqnfIslRQXCkVqEg=="
}
```

### Get artifact

```shell
curl --location --request GET 'https://localhost:9090/artifact' \
--header 'Content-Type: application/json' \
--data-raw '{
  "type": "artifact",
  "spec_version": "2.1",
  "id": "artifact--6f437177-6e48-5cf8-9d9e-872a2bddd641"
}'
```



```json
{
    "encrypted_key": "ZbRRSIKl75l/xXTrZRFgcIX8GXzuuMvjiFfpyMdESI6oEUqNBoVQWEXHuSLJWUmFu0caf3OC5zGnGwLcDdRQE4FLDB5KYdiqHYxaQRL5soakXtoeunM8/PWSoIhTX8FwScm9A32Q1GcoK/hsuPK9vdceYO12iALBPaePzBq1/plQSbncKJ/v7HE5dAxLIYmtC4URTWVMCK6PIVtGU1MNBUMdC3wn9a4vx1cbTSak3vKNEhr5xXN1etUpJvYlLeiMEznajpzn3FS8+Vh80soebrHBtaJV0p3vFm2B9HQfZM0wOGsy3llqT7/036SqKlgNm5scUBGZiIYsi0FBHf0dqw==",
    "id": "artifact--6f437177-6e48-5cf8-9d9e-872a2bddd641",
    "iv": "czjHhvL8OeBngt6Fm62QUQ==",
    "encrypted": "brDv2lhtoPaKV9GHgXGHCtTbUkPZOeaOLyh6qVq6msde7GLBwhOY/KVnBfCZzKtYjrFey5LQ9NBwQSXBFdmw1I1+m1jx/TkOyMXxB49eFcDARarR/sAD2np9jVad6OZZ2FXQggsWQVyEyJHmxyq/jL9ukrFYF1U5Dm91BPObrdyQm/sKnBmE8p7dfndEdBJHig1T8+ZWeLqN4GWifeZ+8SopaingIk4k6PRipEta77+uVIi1N4mJu1Nj5aNyHjXE/VTrgGwoxgCJSjsti+uZDwgpn7+PEzZdCZZ9TGvBChLXvvPO6xcjnjC0YD1Ea7ZaSdfqjIRKH+HUQrxQBz/NqzE5eovx9vkh1EUv0uvVWWwpKCHwhXDq2nct9aV1Iss/371+ecum66u3ZrfZjVYW0w==",
    "signature": "iyIabLQwYfUUYzi2U7RSXOKAjsRwEjrKhE1t0/VJ2RaJrUdkYp4U0C52fofSGRZLtX0uCgwoy4oAQuaXNWF101p8N8nckX5kUxHLg8nu8jBzxNOyihuRYhrydwd7dR5sNiQtOPrMzdKItCRUqFEgJ/rgFKWuub5cYfoOT2V3UvNFUv1PigRFWH2D50cu23yLLzmpURakpiOoyjbhbgE6THse5g9BBvcCRezDuIX1+hX10BXq1fwPcsO/EjfWu5hXKDgQ5JQto7vu5qqKAl32t/HMNPVyksKiQrh3fAiLH1G0YV20tOvzkLjqBAYwfpH9NZxujM1Yu6p8J8jub6hhaw==",
    "length": 288
}

```

### Other information

image:https://img.shields.io/badge/vert.x-4.0.2-purple.svg[link="https://vertx.io"]

This application was generated using http://start.vertx.io

## Building

To launch your tests:
```
./mvnw clean test
```

Code coverage from jacoco plugin is stored in the `target/site` directory.

To package your application:
```
./mvnw clean package
```

To run your application:
```
./mvnw clean compile exec:java
```

To run as a standalone:
```
java -jar target/ddb-1.0.0-SNAPSHOT-fat.jar -conf conf/config.json
```

## Help

* https://vertx.io/docs/[Vert.x Documentation]
* https://stackoverflow.com/questions/tagged/vert.x?sort=newest&pageSize=15[Vert.x Stack Overflow]
* https://groups.google.com/forum/?fromgroups#!forum/vertx[Vert.x User Group]
* https://gitter.im/eclipse-vertx/vertx-users[Vert.x Gitter]


