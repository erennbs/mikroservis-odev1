# Keycloak ile Kimlik Doğrulama & Yetkilendirme (OAuth2 / OIDC)

Bu projede kimlik doğrulama **Keycloak** ile yapılır. Tüm servisler
(**gateway-server** + iş servisleri) **OAuth2 Resource Server** olarak çalışır ve
gelen isteklerdeki JWT'yi Keycloak'ın `etiya` realm'ine karşı doğrular.

## Mimari

```
                 (1) token al
   İstemci  ───────────────────────────►  Keycloak (http://localhost:8090)
      │                                     realm: etiya
      │  (2) Authorization: Bearer <JWT>
      ▼
  gateway-server (8000)  ── JWT doğrula (401 ise reddet) ──┐
      │  Authorization header downstream'e AYNEN iletilir  │
      ▼                                                     │
  product / order / payment / notification service         │
      └── JWT'yi TEKRAR doğrula + ROL bazlı yetkilendirme ──┘
```

- **Kimlik doğrulama (authentication):** Hem gateway hem her servis JWT'nin
  imzasını, süresini ve `iss` (issuer) değerini Keycloak'a karşı doğrular.
- **Yetkilendirme (authorization):** Keycloak realm rolleri (`USER`, `ADMIN`)
  JWT'nin `realm_access.roles` claim'inden okunur ve `ROLE_` öneki eklenerek
  Spring Security yetkilerine çevrilir (`KeycloakRealmRoleConverter`).
  - `GET` (okuma) → `USER` veya `ADMIN`
  - `POST` / `PUT` / `DELETE` (yazma) → yalnızca `ADMIN`

## Hazır gelen realm (otomatik import)

`infra/keycloak/realm-export.json`, Keycloak açılışında `--import-realm` ile
otomatik yüklenir:

| Öğe          | Değer                                              |
|--------------|----------------------------------------------------|
| Realm        | `etiya`                                             |
| Issuer URI   | `http://localhost:8090/realms/etiya`               |
| Roller       | `USER`, `ADMIN`                                     |
| Client       | `etrade-client` (public, direct access grants açık)|
| Kullanıcı 1  | `admin` / `admin123`  → roller: `ADMIN`, `USER`     |
| Kullanıcı 2  | `user`  / `user123`   → rol: `USER`                 |

Keycloak admin konsolu: <http://localhost:8090> (kullanıcı `admin`, şifre `admin`).

## Çalıştırma

### 1) Altyapıyı ayağa kaldır (Keycloak dahil)

```powershell
cd infra
podman compose -f podman-compose.yml up -d
```

Keycloak'ın hazır olmasını bekleyin (ilk açılışta realm import ~30-60 sn sürebilir):

```powershell
# realm'in yüklendiğini doğrula (200 dönmeli):
curl http://localhost:8090/realms/etiya/.well-known/openid-configuration
```

### 2) Servis konfigürasyonunu yayınla (ÖNEMLİ)

`issuer-uri` ayarları `configs/<servis>/application.yml` içine eklendi. Bu proje
config-server'ı **Git backend** ile kullandığından, değişikliklerin servislere
ulaşması için **commit + push** gerekir:

```powershell
git add configs pom.xml */pom.xml */src infra
git commit -m "add keycloak"
git push
```

> Push yapmadan lokal denemek isterseniz config-server'ı `native` profille
> çalıştırıp `spring.cloud.config.server.native.search-locations=file:./configs/{application}`
> ayarını kullanabilirsiniz (bkz. `config-server/src/main/resources/application.yml`).

### 3) Servisleri başlat

Config-server → eureka-server → gateway + iş servisleri sırasıyla.
**Not:** Servisler açılışta Keycloak'ın OIDC metadata'sını çeker; bu yüzden
servisleri başlatmadan önce Keycloak ayakta olmalıdır.

## Kullanım (token alıp istek atma)

### Token al (password / direct access grant)

```powershell
# ADMIN token
$admin = (Invoke-RestMethod -Method Post `
  -Uri http://localhost:8090/realms/etiya/protocol/openid-connect/token `
  -Body @{ client_id='etrade-client'; grant_type='password';
           username='admin'; password='admin123' }).access_token

# USER token
$user = (Invoke-RestMethod -Method Post `
  -Uri http://localhost:8090/realms/etiya/protocol/openid-connect/token `
  -Body @{ client_id='etrade-client'; grant_type='password';
           username='user'; password='user123' }).access_token
```

### İstek at (gateway üzerinden, port 8000)

```powershell
# GET -> USER de ADMIN de erişebilir (200)
Invoke-RestMethod -Uri http://localhost:8000/api/products `
  -Headers @{ Authorization = "Bearer $user" }

# POST -> yalnızca ADMIN (201). USER token'ı ile 403 döner.
Invoke-RestMethod -Method Post -Uri http://localhost:8000/api/products `
  -Headers @{ Authorization = "Bearer $admin" } `
  -ContentType 'application/json' `
  -Body '{ "name": "Kalem", "unitPrice": 10, "stock": 100 }'

# Token'sız istek -> 401 Unauthorized
Invoke-RestMethod -Uri http://localhost:8000/api/products
```

## Beklenen davranış (özet)

| İstek                                   | Token yok | USER | ADMIN |
|-----------------------------------------|:---------:|:----:|:-----:|
| `GET  /api/{products,orders,...}`       |    401    | 200  |  200  |
| `POST/PUT/DELETE /api/{...}`            |    401    | 403  |  200  |
| `GET  /actuator/health`                 |    200    | 200  |  200  |

## Uygulama detayları (kod)

- **Bağımlılık:** her serviste `spring-boot-starter-oauth2-resource-server`.
- **Servlet servisleri** (product/order/payment/notification):
  `security/SecurityConfig.java` + `security/KeycloakRealmRoleConverter.java`.
- **Gateway** (WebFlux, reactive): `security/SecurityConfig.java`
  (`@EnableWebFluxSecurity`, `ReactiveJwtAuthenticationConverterAdapter`).
- **Konfigürasyon:** `configs/<servis>/application.yml` içinde
  `spring.security.oauth2.resourceserver.jwt.issuer-uri`
  (varsayılan `http://localhost:8090/realms/etiya`, prod'da `KEYCLOAK_ISSUER_URI`
  ortam değişkeni ile override edilir).
