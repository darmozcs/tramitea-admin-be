-- Tablas prefijadas con tr_ para convivir en la base compartida (nexora) con
-- auth_*, sa_*, etc. El historial de Flyway tambien es propio
-- (tr_flyway_schema_history, ver application.yml).

CREATE TABLE tr_categories (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    icon VARCHAR(100) NOT NULL,
    display_order INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE tr_institutions (
    id UUID PRIMARY KEY,
    acronym VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    parent_entity VARCHAR(255),
    official_website VARCHAR(500) NOT NULL,
    contact_phone VARCHAR(100),
    contact_email VARCHAR(255),
    logo_url VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE tr_procedures (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    application_method VARCHAR(30) NOT NULL,
    authentication_level VARCHAR(30) NOT NULL,
    procedure_group VARCHAR(30) NOT NULL,
    description TEXT NOT NULL,
    -- name + description sin tildes y en minusculas: busqueda insensible a
    -- acentos sin depender de la extension unaccent.
    search_text TEXT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    category_id UUID NOT NULL REFERENCES tr_categories(id),
    institution_id UUID NOT NULL REFERENCES tr_institutions(id),
    appointment_required BOOLEAN NOT NULL,
    is_free BOOLEAN NOT NULL,
    cost_amount NUMERIC(12, 2),
    cost_currency VARCHAR(10),
    cost_comments VARCHAR(1000),
    validity_value VARCHAR(50),
    validity_unit VARCHAR(50),
    validity_comments VARCHAR(1000),
    estimated_delivery_time VARCHAR(255),
    view_count BIGINT NOT NULL DEFAULT 0,
    slug_es VARCHAR(255) NOT NULL UNIQUE,
    slug_en VARCHAR(255) NOT NULL UNIQUE,
    -- Orden "por relevancia" (el orden editorial del catalogo).
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_tr_procedures_category_id ON tr_procedures(category_id);
CREATE INDEX idx_tr_procedures_institution_id ON tr_procedures(institution_id);

CREATE TABLE tr_requirements (
    id UUID PRIMARY KEY,
    procedure_id UUID NOT NULL REFERENCES tr_procedures(id) ON DELETE CASCADE,
    title VARCHAR(500) NOT NULL,
    description VARCHAR(2000),
    is_required BOOLEAN NOT NULL,
    is_digitally_verifiable BOOLEAN NOT NULL,
    display_order INT NOT NULL
);

CREATE INDEX idx_tr_requirements_procedure_id ON tr_requirements(procedure_id);

CREATE TABLE tr_procedure_steps (
    id UUID PRIMARY KEY,
    procedure_id UUID NOT NULL REFERENCES tr_procedures(id) ON DELETE CASCADE,
    step_number INT NOT NULL,
    title VARCHAR(500) NOT NULL,
    instructions TEXT NOT NULL,
    channel_url VARCHAR(500),
    estimated_time VARCHAR(255)
);

CREATE INDEX idx_tr_procedure_steps_procedure_id ON tr_procedure_steps(procedure_id);

CREATE TABLE tr_procedure_step_comments (
    step_id UUID NOT NULL REFERENCES tr_procedure_steps(id) ON DELETE CASCADE,
    position INT NOT NULL,
    level VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    PRIMARY KEY (step_id, position)
);

CREATE TABLE tr_procedure_faqs (
    id UUID PRIMARY KEY,
    procedure_id UUID NOT NULL REFERENCES tr_procedures(id) ON DELETE CASCADE,
    question VARCHAR(1000) NOT NULL,
    answer TEXT NOT NULL,
    display_order INT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_tr_procedure_faqs_procedure_id ON tr_procedure_faqs(procedure_id);

-- Cuenta + perfil ciudadano. id = sub del token de sesion. El email se
-- guarda en minusculas y es el identificador de login.
CREATE TABLE tr_users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    national_id VARCHAR(20),
    national_id_verified BOOLEAN NOT NULL DEFAULT FALSE,
    phone VARCHAR(50),
    department VARCHAR(2),
    locality VARCHAR(100),
    notify_document_expiry BOOLEAN NOT NULL DEFAULT TRUE,
    notify_appointment_reminders BOOLEAN NOT NULL DEFAULT TRUE,
    notify_news_and_updates BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- author_* es un snapshot: los comentarios sembrados no tienen un usuario
-- real detras, y al borrar una cuenta se anonimizan en vez de eliminarse
-- (para no romper los hilos de respuestas de otros usuarios).
CREATE TABLE tr_community_comments (
    id UUID PRIMARY KEY,
    procedure_id UUID NOT NULL REFERENCES tr_procedures(id) ON DELETE CASCADE,
    parent_id UUID REFERENCES tr_community_comments(id) ON DELETE CASCADE,
    author_user_id UUID,
    author_display_name VARCHAR(255) NOT NULL,
    author_avatar_url VARCHAR(500),
    author_verified BOOLEAN NOT NULL DEFAULT FALSE,
    content VARCHAR(2000) NOT NULL,
    upvotes INT NOT NULL DEFAULT 0,
    downvotes INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_tr_community_comments_procedure_id ON tr_community_comments(procedure_id);
CREATE INDEX idx_tr_community_comments_author_user_id ON tr_community_comments(author_user_id);

-- Un voto por usuario y objetivo; votar de nuevo cambia el valor.
CREATE TABLE tr_votes (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    procedure_id UUID NOT NULL REFERENCES tr_procedures(id) ON DELETE CASCADE,
    target_type VARCHAR(30) NOT NULL,
    target_id UUID NOT NULL,
    vote_value VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_tr_votes_user_target UNIQUE (user_id, target_type, target_id)
);

CREATE INDEX idx_tr_votes_procedure_id ON tr_votes(procedure_id);

CREATE TABLE tr_saved_tasks (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    procedure_id UUID NOT NULL REFERENCES tr_procedures(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL,
    notes VARCHAR(2000),
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_tr_saved_tasks_user_procedure UNIQUE (user_id, procedure_id)
);

CREATE TABLE tr_procedure_suggestions (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    institution_id UUID NOT NULL REFERENCES tr_institutions(id),
    details VARCHAR(5000) NOT NULL,
    contact_email VARCHAR(255),
    -- Null si la sugerencia fue anonima.
    user_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
