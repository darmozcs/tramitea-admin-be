-- Relación con darmoz-auth (modo AUTH_MODE=darmoz, ambiente de testing).
-- tr_users.id sigue siendo el id propio de Tramitea, al que apuntan tareas,
-- votos y comentarios. id_auth_user guarda el id del usuario en auth_users de
-- darmoz-auth (claim sub de su token). Es una relación lógica, sin FK: en
-- producción (Railway) auth_users no existe en esta base, y además son
-- servicios separados. En modo local la columna queda en NULL.

ALTER TABLE tr_users ADD COLUMN id_auth_user UUID;

CREATE UNIQUE INDEX uq_tr_users_id_auth_user
    ON tr_users (id_auth_user)
    WHERE id_auth_user IS NOT NULL;

-- Las cuentas de darmoz-auth no tienen password local, y sus nombres se
-- completan después (el token de darmoz-auth no los trae).
ALTER TABLE tr_users ALTER COLUMN password_hash DROP NOT NULL;
ALTER TABLE tr_users ALTER COLUMN first_name SET DEFAULT '';
ALTER TABLE tr_users ALTER COLUMN last_name SET DEFAULT '';
