-- Ubicación GLOBAL: un admin con esta ubicación en tr_users administra todos
-- los países y provincias. Es solo un alcance de admin: no se asigna a
-- trámites, instituciones ni sugerencias, y la API pública no la muestra.

-- Los CHECK de V4 no tienen nombre explícito: se borran por definición.
DO $$
DECLARE c record;
BEGIN
    FOR c IN SELECT conname FROM pg_constraint
             WHERE conrelid = 'tr_locations'::regclass AND contype = 'c'
    LOOP
        EXECUTE format('ALTER TABLE tr_locations DROP CONSTRAINT %I', c.conname);
    END LOOP;
END $$;

ALTER TABLE tr_locations
    ADD CONSTRAINT ck_tr_locations_type CHECK (type IN ('GLOBAL', 'COUNTRY', 'PROVINCE')),
    -- Solo las provincias tienen padre (su país); GLOBAL y los países no.
    ADD CONSTRAINT ck_tr_locations_parent CHECK ((type = 'PROVINCE') = (parent_id IS NOT NULL));

-- Una sola ubicación GLOBAL.
CREATE UNIQUE INDEX ux_tr_locations_global ON tr_locations (type) WHERE type = 'GLOBAL';

INSERT INTO tr_locations (type, code, name, is_active) VALUES ('GLOBAL', 'GLOBAL', 'Global', TRUE);
