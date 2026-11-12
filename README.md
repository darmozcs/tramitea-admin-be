# tramitea-admin-be

API del panel de administración de [Tramitea](https://github.com/darmozcs/tramitea)
(`apps/admin`): ABM de trámites, categorías e instituciones, trámites
sugeridos por los ciudadanos y métricas del dashboard.

Spring Boot 3.3 · Java 21 · Postgres · JWT RS256 de darmoz-auth.

## Base de datos

Usa **la misma base y las mismas tablas `tr_*` que
[tramitea-be](https://github.com/darmozcs/tramitea-be)**: lo que se edita acá
se ve en la web al instante.

- **El esquema es de tramitea-be.** Sus migraciones Flyway lo crean y lo
  cambian; este servicio no tiene Flyway y Hibernate solo valida
  (`ddl-auto: validate`).
- Si tramitea-be agrega una migración que toca tablas que se usan acá, hay que
  actualizar las entidades y la copia de las migraciones en
  `src/test/resources/db/migration` (los tests arman el esquema con ellas).
- `tr_users` solo se cuenta (`GET /stats`): el admin no edita ciudadanos.

## Autenticación

El login se delega en darmoz-auth, con la Application "Tramitea" (el mismo
`API_ID` que la web). **Solo entran los usuarios que tienen el rol
`ADMIN_ROLE`** (por defecto `ADMIN`) en esa Application.

- `POST /auth/login`: llama a `POST /auth/login` de darmoz-auth. Si el usuario
  no tiene el rol, responde `403 not-admin` y revoca la sesión que darmoz-auth
  acaba de abrir.
- `POST /auth/refresh`: igual, con `/auth/refresh`. Los roles se recalculan, así
  que si a alguien le sacan el rol queda afuera en el próximo refresh, como
  mucho a los 12 minutos.
- `POST /auth/logout`: revoca el refresh token (y el access token si viene el
  Bearer).
- El resto de los endpoints verifica el `accessToken` acá mismo, sin llamar a
  darmoz-auth:
  - la firma con la llave pública (`DARMOZ_AUTH_PUBLIC_KEY_PATH`);
  - `iss=darmoz-auth`;
  - que `applicationId` sea `DARMOZ_AUTH_APPLICATION_ID`;
  - que `roles` tenga el rol admin.

  Sin token o con uno inválido responde 401; con un token válido sin el rol, 403.

El rol se crea y se asigna en darmoz-auth (`/admin/api/roles` y
`PUT /admin/api/users/{id}/roles`, con un usuario SUPER).

## Endpoints

Base:
- Testing (Traefik, mismo dominio que el front): `https://darmozsc.duckdns.org/tramitea-admin-api`.
- Red Docker interna: `http://tramitea-admin-be:8080/tramitea-admin-api`.

**Swagger UI**: `/tramitea-admin-api/swagger-ui.html` · OpenAPI JSON: `/tramitea-admin-api/v3/api-docs`
(se apagan con `API_DOCS_ENABLED=false`). Los tests dejan una copia en
`target/openapi.json`.

| Pantalla | Endpoints |
|---|---|
| Login | `POST /auth/login`, `/auth/refresh`, `/auth/logout` |
| Dashboard | `GET /stats` |
| Trámites | `GET /procedures` (mismos filtros que tramitea-be), `GET/PUT/DELETE /procedures/{id}`, `POST /procedures` |
| Categorías | `GET /categories?search=&isActive=&page=&pageSize=` (paginado), `GET /categories/catalog?search=&isActive=` (todas, para combos), `GET/PUT/DELETE /categories/{id}`, `POST /categories` |
| Instituciones | `GET /institutions?search=&page=&pageSize=` (paginado), `GET /institutions/catalog?search=` (todas, para combos), `GET/PUT/DELETE /institutions/{id}`, `POST /institutions` |
| Sugeridos | `GET /suggestions?page=&pageSize=` |

Reglas:
- Las respuestas tienen la misma forma que las de tramitea-be (`Procedure`,
  `Category`, `Institution`, `PaginatedResponse`), así el front reusa sus tipos.
- Requisitos, pasos y FAQs se mandan junto con el trámite y se reemplazan en
  cada guardado.
- Borrar una categoría o institución en uso da `409 in-use`. Borrar un trámite
  borra también sus comentarios, votos y las tareas guardadas de los
  ciudadanos.

## Desarrollo

```bash
./mvnw test   # unitarios + *IT con Testcontainers (necesita Docker)
```

Variables: ver `src/main/resources/application.yml` y `deploy/.env.example`.
Deploy: `deploy/RUNBOOK.md`.
