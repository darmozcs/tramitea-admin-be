-- Ubicaciones: países y provincias (departamentos en Uruguay, estados en
-- Brasil, provincias en España). Un trámite, una institución, un usuario y
-- una sugerencia apuntan a una ubicación: un país (alcance nacional) o una
-- provincia de ese país.
--
-- Visibilidad: una ubicación se ve si está activa y, si es provincia, si su
-- país también lo está. Los trámites de ubicaciones no visibles no salen en
-- la API pública. Uruguay arranca activo; Brasil y España, desactivados (sus
-- provincias quedan activas, así que al activar el país se ven todas).

CREATE TABLE tr_locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(20) NOT NULL CHECK (type IN ('COUNTRY', 'PROVINCE')),
    parent_id UUID REFERENCES tr_locations(id),
    -- ISO 3166-1 alfa-2 para países, ISO 3166-2 para provincias.
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    -- Solo dos niveles: los países no tienen padre y las provincias sí.
    CHECK ((type = 'COUNTRY') = (parent_id IS NULL))
);

CREATE INDEX ix_tr_locations_parent ON tr_locations (parent_id);

INSERT INTO tr_locations (type, code, name, is_active) VALUES
    ('COUNTRY', 'UY', 'Uruguay', TRUE),
    ('COUNTRY', 'BR', 'Brasil', FALSE),
    ('COUNTRY', 'ES', 'España', FALSE);

INSERT INTO tr_locations (type, parent_id, code, name)
SELECT 'PROVINCE', c.id, p.code, p.name
FROM (VALUES
    ('UY', 'UY-AR', 'Artigas'), ('UY', 'UY-CA', 'Canelones'), ('UY', 'UY-CL', 'Cerro Largo'),
    ('UY', 'UY-CO', 'Colonia'), ('UY', 'UY-DU', 'Durazno'), ('UY', 'UY-FS', 'Flores'),
    ('UY', 'UY-FD', 'Florida'), ('UY', 'UY-LA', 'Lavalleja'), ('UY', 'UY-MA', 'Maldonado'),
    ('UY', 'UY-MO', 'Montevideo'), ('UY', 'UY-PA', 'Paysandú'), ('UY', 'UY-RN', 'Río Negro'),
    ('UY', 'UY-RV', 'Rivera'), ('UY', 'UY-RO', 'Rocha'), ('UY', 'UY-SA', 'Salto'),
    ('UY', 'UY-SJ', 'San José'), ('UY', 'UY-SO', 'Soriano'), ('UY', 'UY-TA', 'Tacuarembó'),
    ('UY', 'UY-TT', 'Treinta y Tres'),

    ('BR', 'BR-AC', 'Acre'), ('BR', 'BR-AL', 'Alagoas'), ('BR', 'BR-AP', 'Amapá'),
    ('BR', 'BR-AM', 'Amazonas'), ('BR', 'BR-BA', 'Bahia'), ('BR', 'BR-CE', 'Ceará'),
    ('BR', 'BR-DF', 'Distrito Federal'), ('BR', 'BR-ES', 'Espírito Santo'), ('BR', 'BR-GO', 'Goiás'),
    ('BR', 'BR-MA', 'Maranhão'), ('BR', 'BR-MT', 'Mato Grosso'), ('BR', 'BR-MS', 'Mato Grosso do Sul'),
    ('BR', 'BR-MG', 'Minas Gerais'), ('BR', 'BR-PA', 'Pará'), ('BR', 'BR-PB', 'Paraíba'),
    ('BR', 'BR-PR', 'Paraná'), ('BR', 'BR-PE', 'Pernambuco'), ('BR', 'BR-PI', 'Piauí'),
    ('BR', 'BR-RJ', 'Rio de Janeiro'), ('BR', 'BR-RN', 'Rio Grande do Norte'),
    ('BR', 'BR-RS', 'Rio Grande do Sul'), ('BR', 'BR-RO', 'Rondônia'), ('BR', 'BR-RR', 'Roraima'),
    ('BR', 'BR-SC', 'Santa Catarina'), ('BR', 'BR-SP', 'São Paulo'), ('BR', 'BR-SE', 'Sergipe'),
    ('BR', 'BR-TO', 'Tocantins'),

    ('ES', 'ES-C', 'A Coruña'), ('ES', 'ES-VI', 'Álava'), ('ES', 'ES-AB', 'Albacete'),
    ('ES', 'ES-A', 'Alicante'), ('ES', 'ES-AL', 'Almería'), ('ES', 'ES-O', 'Asturias'),
    ('ES', 'ES-AV', 'Ávila'), ('ES', 'ES-BA', 'Badajoz'), ('ES', 'ES-PM', 'Illes Balears'),
    ('ES', 'ES-B', 'Barcelona'), ('ES', 'ES-BI', 'Bizkaia'), ('ES', 'ES-BU', 'Burgos'),
    ('ES', 'ES-CC', 'Cáceres'), ('ES', 'ES-CA', 'Cádiz'), ('ES', 'ES-S', 'Cantabria'),
    ('ES', 'ES-CS', 'Castellón'), ('ES', 'ES-CR', 'Ciudad Real'), ('ES', 'ES-CO', 'Córdoba'),
    ('ES', 'ES-CU', 'Cuenca'), ('ES', 'ES-SS', 'Gipuzkoa'), ('ES', 'ES-GI', 'Girona'),
    ('ES', 'ES-GR', 'Granada'), ('ES', 'ES-GU', 'Guadalajara'), ('ES', 'ES-H', 'Huelva'),
    ('ES', 'ES-HU', 'Huesca'), ('ES', 'ES-J', 'Jaén'), ('ES', 'ES-LO', 'La Rioja'),
    ('ES', 'ES-GC', 'Las Palmas'), ('ES', 'ES-LE', 'León'), ('ES', 'ES-L', 'Lleida'),
    ('ES', 'ES-LU', 'Lugo'), ('ES', 'ES-M', 'Madrid'), ('ES', 'ES-MA', 'Málaga'),
    ('ES', 'ES-MU', 'Murcia'), ('ES', 'ES-NA', 'Navarra'), ('ES', 'ES-OR', 'Ourense'),
    ('ES', 'ES-P', 'Palencia'), ('ES', 'ES-PO', 'Pontevedra'), ('ES', 'ES-SA', 'Salamanca'),
    ('ES', 'ES-TF', 'Santa Cruz de Tenerife'), ('ES', 'ES-SG', 'Segovia'), ('ES', 'ES-SE', 'Sevilla'),
    ('ES', 'ES-SO', 'Soria'), ('ES', 'ES-T', 'Tarragona'), ('ES', 'ES-TE', 'Teruel'),
    ('ES', 'ES-TO', 'Toledo'), ('ES', 'ES-V', 'Valencia'), ('ES', 'ES-VA', 'Valladolid'),
    ('ES', 'ES-ZA', 'Zamora'), ('ES', 'ES-Z', 'Zaragoza'), ('ES', 'ES-CE', 'Ceuta'),
    ('ES', 'ES-ML', 'Melilla')
) AS p (country, code, name)
JOIN tr_locations c ON c.code = p.country;

-- Trámites: hasta ahora eran todos de Uruguay y de alcance nacional.
ALTER TABLE tr_procedures ADD COLUMN location_id UUID REFERENCES tr_locations(id);
UPDATE tr_procedures SET location_id = (SELECT id FROM tr_locations WHERE code = 'UY');
ALTER TABLE tr_procedures ALTER COLUMN location_id SET NOT NULL;
CREATE INDEX ix_tr_procedures_location ON tr_procedures (location_id);

-- Instituciones: todas de Uruguay; las intendencias, de su departamento.
ALTER TABLE tr_institutions ADD COLUMN location_id UUID REFERENCES tr_locations(id);
UPDATE tr_institutions SET location_id = (SELECT id FROM tr_locations WHERE code = 'UY');
UPDATE tr_institutions SET location_id = (SELECT id FROM tr_locations WHERE code = 'UY-MO')
    WHERE acronym = 'IM';
ALTER TABLE tr_institutions ALTER COLUMN location_id SET NOT NULL;
CREATE INDEX ix_tr_institutions_location ON tr_institutions (location_id);

-- Usuarios (ciudadanos y admins): opcional. Sin ubicación no se puede
-- sugerir ni administrar. Se toma del departamento si ya lo habían cargado.
ALTER TABLE tr_users ADD COLUMN location_id UUID REFERENCES tr_locations(id);
UPDATE tr_users u SET location_id = l.id
    FROM tr_locations l
    WHERE u.department IS NOT NULL AND l.code = 'UY-' || u.department;

-- Sugerencias: ubicación, estado de revisión y el trámite que salió de ella.
ALTER TABLE tr_procedure_suggestions
    ADD COLUMN location_id UUID REFERENCES tr_locations(id),
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')),
    -- Id de darmoz-auth (sub del token) del admin que la revisó.
    ADD COLUMN reviewed_by UUID,
    ADD COLUMN reviewed_at TIMESTAMPTZ,
    ADD COLUMN rejection_reason VARCHAR(1000),
    ADD COLUMN procedure_id UUID REFERENCES tr_procedures(id) ON DELETE SET NULL;
UPDATE tr_procedure_suggestions SET location_id = (SELECT id FROM tr_locations WHERE code = 'UY');
ALTER TABLE tr_procedure_suggestions ALTER COLUMN location_id SET NOT NULL;
CREATE INDEX ix_tr_procedure_suggestions_location_status
    ON tr_procedure_suggestions (location_id, status);

-- user_id no tenía FK: se limpian los huérfanos antes de agregarla.
UPDATE tr_procedure_suggestions s SET user_id = NULL
    WHERE user_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM tr_users u WHERE u.id = s.user_id);
ALTER TABLE tr_procedure_suggestions
    ADD CONSTRAINT fk_tr_procedure_suggestions_user
    FOREIGN KEY (user_id) REFERENCES tr_users(id) ON DELETE SET NULL;
