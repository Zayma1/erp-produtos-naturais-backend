DO $$
DECLARE report text;
BEGIN
  SELECT string_agg(normalized || ': ' || ids, E'\n') INTO report FROM (
    SELECT lower(btrim(email)) normalized, string_agg(id::text, ', ' ORDER BY id) ids
    FROM usuario GROUP BY lower(btrim(email)) HAVING count(*) > 1
  ) collisions;
  IF report IS NOT NULL THEN RAISE EXCEPTION 'Reconcile normalized email collisions before authentication migration: %', report; END IF;
END $$;
ALTER TABLE usuario ADD COLUMN cargo_legado varchar(255);
UPDATE usuario SET cargo_legado=cargo, cargo='FUNC', email=lower(btrim(email)), senha_hash='!INVALIDATED!';
ALTER TABLE usuario ADD CONSTRAINT usuario_cargo_check CHECK(cargo IN ('ADMIN','GER','FUNC'));
ALTER TABLE usuario ADD COLUMN alterar_senha boolean NOT NULL DEFAULT true;
ALTER TABLE usuario ADD COLUMN revisado boolean NOT NULL DEFAULT false;
ALTER TABLE usuario ADD COLUMN auth_version bigint NOT NULL DEFAULT 0;
CREATE UNIQUE INDEX usuario_normalized_email ON usuario(lower(btrim(email)));
CREATE TABLE usuario_loja(id_usuario uuid NOT NULL REFERENCES usuario(id), id_loja uuid NOT NULL REFERENCES loja(id), PRIMARY KEY(id_usuario,id_loja));
CREATE TABLE auth_attempt(email varchar(254) PRIMARY KEY, started_at timestamp NOT NULL, failures integer NOT NULL);
CREATE TABLE spring_session (
 primary_id char(36) PRIMARY KEY, session_id char(36) NOT NULL, creation_time bigint NOT NULL,
 last_access_time bigint NOT NULL, max_inactive_interval integer NOT NULL, expiry_time bigint NOT NULL, principal_name varchar(255)
);
CREATE UNIQUE INDEX spring_session_ix1 ON spring_session(session_id);
CREATE INDEX spring_session_ix2 ON spring_session(expiry_time);
CREATE INDEX spring_session_ix3 ON spring_session(principal_name);
CREATE TABLE spring_session_attributes (
 session_primary_id char(36) NOT NULL REFERENCES spring_session(primary_id) ON DELETE CASCADE,
 attribute_name varchar(200) NOT NULL, attribute_bytes bytea NOT NULL, PRIMARY KEY(session_primary_id,attribute_name)
);
ALTER TABLE compra ADD COLUMN id_modificador uuid REFERENCES usuario(id);
ALTER TABLE recebimento_compra ADD COLUMN id_modificador uuid REFERENCES usuario(id);
ALTER TABLE recebimento_compra ADD COLUMN id_confirmador uuid REFERENCES usuario(id);
ALTER TABLE recebimento_idempotencia ADD COLUMN actor_id uuid REFERENCES usuario(id);
