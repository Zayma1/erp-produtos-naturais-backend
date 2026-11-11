-- Stage 13 is a coordinated reset, not a mapping of old commercial records.
DO $$
DECLARE t text; populated boolean;
BEGIN
 FOREACH t IN ARRAY ARRAY['produto_sku','produto_loja','produto_preco_loja','estoque_produto','compra','venda','recebimento_compra','transferencia_estoque','ajuste_estoque','devolucao_cliente','devolucao_fornecedor','movimentacao_estoque'] LOOP
  EXECUTE format('select exists(select 1 from %I)',t) INTO populated;
  IF populated THEN RAISE EXCEPTION 'V8 exige base sem dados comerciais antigos (%). Recrie somente a base de desenvolvimento; não existe associação automática produto/SKU.',t; END IF;
 END LOOP;
END $$;
ALTER TABLE produto_sku ADD COLUMN id_unidade_medida uuid NOT NULL REFERENCES unidade_medida(id);
ALTER TABLE produto_sku ADD COLUMN tipo_venda varchar(20) NOT NULL CHECK(tipo_venda IN ('UNIDADE','PESO'));
ALTER TABLE produto_sku ALTER COLUMN descricao_apresentacao SET NOT NULL;
ALTER TABLE produto_sku ADD CONSTRAINT sku_apresentacao_obrigatoria CHECK(length(trim(descricao_apresentacao))>0);
ALTER TABLE produto DROP COLUMN id_unidade_medida;
ALTER TABLE produto DROP COLUMN tipo_venda;
DO $$ DECLARE c record; BEGIN
 FOR c IN SELECT conname FROM pg_constraint WHERE conrelid='estoque_produto'::regclass AND confrelid='produto'::regclass LOOP
 EXECUTE format('ALTER TABLE estoque_produto DROP CONSTRAINT %I',c.conname); END LOOP; END $$;
ALTER TABLE estoque_produto RENAME COLUMN id_produto TO id_produto_sku;
ALTER TABLE estoque_produto ADD FOREIGN KEY(id_produto_sku) REFERENCES produto_sku(id);
DO $$ DECLARE c record; BEGIN
 FOR c IN SELECT conname FROM pg_constraint WHERE conrelid='item_compra'::regclass AND confrelid='produto'::regclass LOOP
 EXECUTE format('ALTER TABLE item_compra DROP CONSTRAINT %I',c.conname); END LOOP; END $$;
ALTER TABLE item_compra RENAME COLUMN id_produto TO id_produto_sku;
ALTER TABLE item_compra ADD FOREIGN KEY(id_produto_sku) REFERENCES produto_sku(id);
ALTER TABLE item_compra ADD COLUMN sku_codigo_snapshot varchar(80) NOT NULL;
ALTER TABLE item_compra ADD COLUMN sku_apresentacao_snapshot varchar(255) NOT NULL;
ALTER TABLE item_compra ADD COLUMN IF NOT EXISTS unidade_sigla_snapshot varchar(10);
ALTER TABLE item_compra ALTER COLUMN unidade_sigla_snapshot SET NOT NULL;
DO $$ DECLARE c record; BEGIN
 FOR c IN SELECT conname FROM pg_constraint WHERE conrelid='item_recebimento_compra'::regclass AND confrelid='produto'::regclass LOOP
 EXECUTE format('ALTER TABLE item_recebimento_compra DROP CONSTRAINT %I',c.conname); END LOOP; END $$;
ALTER TABLE item_recebimento_compra RENAME COLUMN id_produto TO id_produto_sku;
ALTER TABLE item_recebimento_compra ADD FOREIGN KEY(id_produto_sku) REFERENCES produto_sku(id);
ALTER TABLE item_recebimento_compra ADD COLUMN sku_codigo_snapshot varchar(80) NOT NULL;
ALTER TABLE item_recebimento_compra ADD COLUMN sku_apresentacao_snapshot varchar(255) NOT NULL;
ALTER TABLE item_recebimento_compra ADD COLUMN IF NOT EXISTS unidade_sigla_snapshot varchar(10);
ALTER TABLE item_recebimento_compra ALTER COLUMN unidade_sigla_snapshot SET NOT NULL;
DO $$ DECLARE c record; BEGIN
 FOR c IN SELECT conname FROM pg_constraint WHERE conrelid='produto_venda'::regclass AND confrelid='produto'::regclass LOOP
 EXECUTE format('ALTER TABLE produto_venda DROP CONSTRAINT %I',c.conname); END LOOP; END $$;
ALTER TABLE produto_venda DROP COLUMN id_produto;
ALTER TABLE produto_venda ALTER COLUMN id_produto_sku SET NOT NULL;
ALTER TABLE produto_venda ADD COLUMN sku_codigo_snapshot varchar(80) NOT NULL;
ALTER TABLE produto_venda ADD COLUMN sku_apresentacao_snapshot varchar(255) NOT NULL;
ALTER TABLE produto_venda ADD COLUMN IF NOT EXISTS unidade_sigla_snapshot varchar(10);
ALTER TABLE produto_venda ALTER COLUMN unidade_sigla_snapshot SET NOT NULL;
DO $$ DECLARE c record; BEGIN
 FOR c IN SELECT conname FROM pg_constraint WHERE conrelid='item_transferencia'::regclass AND confrelid='produto'::regclass LOOP
 EXECUTE format('ALTER TABLE item_transferencia DROP CONSTRAINT %I',c.conname); END LOOP; END $$;
ALTER TABLE item_transferencia RENAME COLUMN id_produto TO id_produto_sku;
ALTER TABLE item_transferencia ADD FOREIGN KEY(id_produto_sku) REFERENCES produto_sku(id);
ALTER TABLE item_transferencia ADD COLUMN sku_codigo_snapshot varchar(80) NOT NULL;
ALTER TABLE item_transferencia ADD COLUMN sku_apresentacao_snapshot varchar(255) NOT NULL;
ALTER TABLE item_transferencia ADD COLUMN IF NOT EXISTS unidade_sigla_snapshot varchar(10);
ALTER TABLE item_transferencia ALTER COLUMN unidade_sigla_snapshot SET NOT NULL;
DO $$ DECLARE c record; BEGIN
 FOR c IN SELECT conname FROM pg_constraint WHERE conrelid='item_ajuste_estoque'::regclass AND confrelid='produto'::regclass LOOP
 EXECUTE format('ALTER TABLE item_ajuste_estoque DROP CONSTRAINT %I',c.conname); END LOOP; END $$;
ALTER TABLE item_ajuste_estoque RENAME COLUMN id_produto TO id_produto_sku;
ALTER TABLE item_ajuste_estoque ADD FOREIGN KEY(id_produto_sku) REFERENCES produto_sku(id);
ALTER TABLE item_ajuste_estoque ADD COLUMN sku_codigo_snapshot varchar(80) NOT NULL;
ALTER TABLE item_ajuste_estoque ADD COLUMN sku_apresentacao_snapshot varchar(255) NOT NULL;
ALTER TABLE item_ajuste_estoque ADD COLUMN IF NOT EXISTS unidade_sigla_snapshot varchar(10);
ALTER TABLE item_ajuste_estoque ALTER COLUMN unidade_sigla_snapshot SET NOT NULL;
DO $$ DECLARE c record; BEGIN
 FOR c IN SELECT conname FROM pg_constraint WHERE conrelid='item_devolucao_cliente'::regclass AND confrelid='produto'::regclass LOOP
 EXECUTE format('ALTER TABLE item_devolucao_cliente DROP CONSTRAINT %I',c.conname); END LOOP; END $$;
ALTER TABLE item_devolucao_cliente RENAME COLUMN id_produto TO id_produto_sku;
ALTER TABLE item_devolucao_cliente ADD FOREIGN KEY(id_produto_sku) REFERENCES produto_sku(id);
ALTER TABLE item_devolucao_cliente ADD COLUMN sku_codigo_snapshot varchar(80) NOT NULL;
ALTER TABLE item_devolucao_cliente ADD COLUMN sku_apresentacao_snapshot varchar(255) NOT NULL;
ALTER TABLE item_devolucao_cliente ADD COLUMN IF NOT EXISTS unidade_sigla_snapshot varchar(10);
ALTER TABLE item_devolucao_cliente ALTER COLUMN unidade_sigla_snapshot SET NOT NULL;
DO $$ DECLARE c record; BEGIN
 FOR c IN SELECT conname FROM pg_constraint WHERE conrelid='item_devolucao_fornecedor'::regclass AND confrelid='produto'::regclass LOOP
 EXECUTE format('ALTER TABLE item_devolucao_fornecedor DROP CONSTRAINT %I',c.conname); END LOOP; END $$;
ALTER TABLE item_devolucao_fornecedor RENAME COLUMN id_produto TO id_produto_sku;
ALTER TABLE item_devolucao_fornecedor ADD FOREIGN KEY(id_produto_sku) REFERENCES produto_sku(id);
ALTER TABLE item_devolucao_fornecedor ADD COLUMN sku_codigo_snapshot varchar(80) NOT NULL;
ALTER TABLE item_devolucao_fornecedor ADD COLUMN sku_apresentacao_snapshot varchar(255) NOT NULL;
ALTER TABLE item_devolucao_fornecedor ADD COLUMN IF NOT EXISTS unidade_sigla_snapshot varchar(10);
ALTER TABLE item_devolucao_fornecedor ALTER COLUMN unidade_sigla_snapshot SET NOT NULL;
DO $$ DECLARE c record; BEGIN
 FOR c IN SELECT conname FROM pg_constraint WHERE conrelid='movimentacao_estoque'::regclass AND confrelid='produto'::regclass LOOP
 EXECUTE format('ALTER TABLE movimentacao_estoque DROP CONSTRAINT %I',c.conname); END LOOP; END $$;
ALTER TABLE movimentacao_estoque RENAME COLUMN id_produto TO id_produto_sku;
ALTER TABLE movimentacao_estoque ADD FOREIGN KEY(id_produto_sku) REFERENCES produto_sku(id);
ALTER TABLE movimentacao_estoque ADD COLUMN sku_codigo_snapshot varchar(80) NOT NULL;
ALTER TABLE movimentacao_estoque ADD COLUMN sku_apresentacao_snapshot varchar(255) NOT NULL;
ALTER TABLE movimentacao_estoque ADD COLUMN IF NOT EXISTS unidade_sigla_snapshot varchar(10);
ALTER TABLE movimentacao_estoque ALTER COLUMN unidade_sigla_snapshot SET NOT NULL;
DO $$ DECLARE c record; BEGIN
 FOR c IN SELECT conname FROM pg_constraint WHERE conrelid='produto_loja'::regclass AND confrelid='produto'::regclass LOOP
 EXECUTE format('ALTER TABLE produto_loja DROP CONSTRAINT %I',c.conname); END LOOP; END $$;
ALTER TABLE produto_loja RENAME COLUMN id_produto TO id_produto_sku;
ALTER TABLE produto_loja ADD FOREIGN KEY(id_produto_sku) REFERENCES produto_sku(id);
DO $$ DECLARE c record; BEGIN
 FOR c IN SELECT conname FROM pg_constraint WHERE conrelid='produto_preco_loja'::regclass AND confrelid='produto'::regclass LOOP
 EXECUTE format('ALTER TABLE produto_preco_loja DROP CONSTRAINT %I',c.conname); END LOOP; END $$;
ALTER TABLE produto_preco_loja RENAME COLUMN id_produto TO id_produto_sku;
ALTER TABLE produto_preco_loja ADD FOREIGN KEY(id_produto_sku) REFERENCES produto_sku(id);
CREATE FUNCTION sku_referenced_guard() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE t text; used boolean;
BEGIN
 IF TG_OP='UPDATE' AND NEW.id_produto=OLD.id_produto AND NEW.id_unidade_medida=OLD.id_unidade_medida AND NEW.tipo_venda=OLD.tipo_venda THEN RETURN NEW; END IF;
 IF EXISTS(SELECT 1 FROM estoque_produto WHERE id_produto_sku=OLD.id) THEN RAISE EXCEPTION 'SKU referenciado: produto, unidade e tipo de venda são imutáveis; use inativação' USING ERRCODE='23514'; END IF;
 IF EXISTS(SELECT 1 FROM item_compra WHERE id_produto_sku=OLD.id) THEN RAISE EXCEPTION 'SKU referenciado: produto, unidade e tipo de venda são imutáveis; use inativação' USING ERRCODE='23514'; END IF;
 IF EXISTS(SELECT 1 FROM item_recebimento_compra WHERE id_produto_sku=OLD.id) THEN RAISE EXCEPTION 'SKU referenciado: produto, unidade e tipo de venda são imutáveis; use inativação' USING ERRCODE='23514'; END IF;
 IF EXISTS(SELECT 1 FROM produto_venda WHERE id_produto_sku=OLD.id) THEN RAISE EXCEPTION 'SKU referenciado: produto, unidade e tipo de venda são imutáveis; use inativação' USING ERRCODE='23514'; END IF;
 IF EXISTS(SELECT 1 FROM item_transferencia WHERE id_produto_sku=OLD.id) THEN RAISE EXCEPTION 'SKU referenciado: produto, unidade e tipo de venda são imutáveis; use inativação' USING ERRCODE='23514'; END IF;
 IF EXISTS(SELECT 1 FROM item_ajuste_estoque WHERE id_produto_sku=OLD.id) THEN RAISE EXCEPTION 'SKU referenciado: produto, unidade e tipo de venda são imutáveis; use inativação' USING ERRCODE='23514'; END IF;
 IF EXISTS(SELECT 1 FROM item_devolucao_cliente WHERE id_produto_sku=OLD.id) THEN RAISE EXCEPTION 'SKU referenciado: produto, unidade e tipo de venda são imutáveis; use inativação' USING ERRCODE='23514'; END IF;
 IF EXISTS(SELECT 1 FROM item_devolucao_fornecedor WHERE id_produto_sku=OLD.id) THEN RAISE EXCEPTION 'SKU referenciado: produto, unidade e tipo de venda são imutáveis; use inativação' USING ERRCODE='23514'; END IF;
 IF EXISTS(SELECT 1 FROM movimentacao_estoque WHERE id_produto_sku=OLD.id) THEN RAISE EXCEPTION 'SKU referenciado: produto, unidade e tipo de venda são imutáveis; use inativação' USING ERRCODE='23514'; END IF;
 IF TG_OP='DELETE' THEN RETURN OLD; END IF; RETURN NEW; END $$;
CREATE TRIGGER sku_referenced_guard BEFORE UPDATE OR DELETE ON produto_sku FOR EACH ROW EXECUTE FUNCTION sku_referenced_guard();

CREATE FUNCTION sku_item_guard() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE s produto_sku%ROWTYPE; unit text;
BEGIN
 -- SHARE serializes commercial-meaning changes against concurrent item insertion.
 SELECT * INTO STRICT s FROM produto_sku WHERE id=NEW.id_produto_sku FOR SHARE;
 IF NEW.quantidade<=0 OR (s.tipo_venda='UNIDADE' AND trunc(NEW.quantidade)<>NEW.quantidade) THEN RAISE EXCEPTION 'Quantidade inválida para SKU %',s.sku USING ERRCODE='23514'; END IF;
 SELECT sigla INTO unit FROM unidade_medida WHERE id=s.id_unidade_medida;
 IF TG_OP='UPDATE' AND NEW.id_produto_sku=OLD.id_produto_sku THEN
  NEW.sku_codigo_snapshot=OLD.sku_codigo_snapshot; NEW.sku_apresentacao_snapshot=OLD.sku_apresentacao_snapshot; NEW.unidade_sigla_snapshot=OLD.unidade_sigla_snapshot;
 ELSE
  NEW.sku_codigo_snapshot=coalesce(NEW.sku_codigo_snapshot,s.sku);NEW.sku_apresentacao_snapshot=coalesce(NEW.sku_apresentacao_snapshot,s.descricao_apresentacao);NEW.unidade_sigla_snapshot=coalesce(NEW.unidade_sigla_snapshot,unit);
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER sku_item_guard BEFORE INSERT OR UPDATE ON item_compra FOR EACH ROW EXECUTE FUNCTION sku_item_guard();
CREATE TRIGGER sku_item_guard BEFORE INSERT OR UPDATE ON item_recebimento_compra FOR EACH ROW EXECUTE FUNCTION sku_item_guard();
CREATE TRIGGER sku_item_guard BEFORE INSERT OR UPDATE ON produto_venda FOR EACH ROW EXECUTE FUNCTION sku_item_guard();
CREATE TRIGGER sku_item_guard BEFORE INSERT OR UPDATE ON item_transferencia FOR EACH ROW EXECUTE FUNCTION sku_item_guard();
CREATE TRIGGER sku_item_guard BEFORE INSERT OR UPDATE ON item_ajuste_estoque FOR EACH ROW EXECUTE FUNCTION sku_item_guard();
CREATE TRIGGER sku_item_guard BEFORE INSERT OR UPDATE ON item_devolucao_cliente FOR EACH ROW EXECUTE FUNCTION sku_item_guard();
CREATE TRIGGER sku_item_guard BEFORE INSERT OR UPDATE ON item_devolucao_fornecedor FOR EACH ROW EXECUTE FUNCTION sku_item_guard();
CREATE TRIGGER sku_item_guard BEFORE INSERT OR UPDATE ON movimentacao_estoque FOR EACH ROW EXECUTE FUNCTION sku_item_guard();
