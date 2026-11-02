# Runbook de deploy — tramitea-admin-be

## Ambientes

| Ambiente | Rama | Dónde | Cómo se deploya |
|---|---|---|---|
| **Testing** | `develop` | Server de Darmoz (`darmoz@192.168.1.23`), contenedor `tramitea-admin-be` | `.github/workflows/deploy-testing.yml`: test → imagen en GHCR → `docker compose up` con el runner self-hosted |
| **Producción** | `main` | Todavía no existe (Railway) | — |

## Testing (server de Darmoz)

Mismo Postgres (`nexora`) y mismas credenciales que tramitea-be, redes
`proxy` (Traefik y darmoz-auth) y `data` (Postgres). Traefik la publica en
`https://darmozsc.duckdns.org/tramitea-admin-api`, con rate limit.

### Primer deploy

1. `mkdir -p /opt/infra/services/tramitea-admin-be`
2. Copiar `deploy/compose.yaml` → `/opt/infra/services/tramitea-admin-be/compose.yaml`.
3. Crear `/opt/infra/services/tramitea-admin-be/.env` a partir de
   `deploy/.env.example` y `chmod 600`. Las `SPRING_DATASOURCE_*` y
   `DARMOZ_AUTH_APPLICATION_ID` son las mismas del `.env` de tramitea-be.
4. Runner self-hosted `~/actions-runner-tramitea-admin-be`:
   ```bash
   mkdir ~/actions-runner-tramitea-admin-be && cd ~/actions-runner-tramitea-admin-be
   tar xzf ~/actions-runner-tramitea-be/actions-runner-linux-x64-*.tar.gz   # o bajar el último release
   ./config.sh --url https://github.com/darmozcs/tramitea-admin-be --token <token de registro> \
       --name darmoz-server-tramitea-admin-be --unattended
   sudo ./svc.sh install darmoz && sudo ./svc.sh start
   ```
   El token sale de GitHub → Settings → Actions → Runners → New self-hosted
   runner. Dura una hora y no se guarda en ningún lado.
5. `git push` a `develop` → CI: test → build & push a GHCR → deploy al server.
6. Si `docker compose pull` da `denied`: hacer público el paquete en
   `github.com/darmozcs?tab=packages` → `tramitea-admin-be` → Package settings.
7. Verificar:
   ```bash
   curl -s https://darmozsc.duckdns.org/tramitea-admin-api/actuator/health
   docker logs tramitea-admin-be --tail 50
   ```

### Rol admin en darmoz-auth

El rol (`ADMIN_ROLE`, por defecto `ADMIN`) tiene que existir en la Application
"Tramitea" y estar asignado a los administradores. Se hace con la API de admin
de darmoz-auth, logueado como un usuario SUPER (ver Swagger de darmoz-auth,
`/auth/swagger-ui.html`):
- `POST /admin/api/roles`
- `PUT /admin/api/users/{id}/roles` (reemplaza **todos** los roles: incluir
  también los que ya tenía, como `USER`).

## Rollback (testing)

Fijar `APP_IMAGE` en el `.env` del server a un tag `:<sha>` anterior (cada
build de CI publica `latest` y `${{ github.sha }}`), luego:

```bash
cd /opt/infra/services/tramitea-admin-be && docker compose up -d
```
