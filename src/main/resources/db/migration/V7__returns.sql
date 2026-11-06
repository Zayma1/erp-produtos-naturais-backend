-- Preserve all historical returns without inferring or replaying their stock effects.

ALTER TABLE devolucao_cliente ADD COLUMN versao integer NOT NULL DEFAULT 0,
 ADD COLUMN atualizado_em timestamptz, ADD COLUMN concluido_em timestamptz,
 ADD COLUMN cancelado_em timestamptz, ADD COLUMN motivo_cancelamento text,
 ADD COLUMN id_modificador uuid REFERENCES usuario(id), ADD COLUMN id_confirmador uuid REFERENCES usuario(id),
 ADD COLUMN id_cancelador uuid REFERENCES usuario(id), ADD COLUMN legado boolean NOT NULL DEFAULT false;
UPDATE devolucao_cliente SET legado=true;
ALTER TABLE item_devolucao_cliente ADD COLUMN unidade_sigla_snapshot varchar(10), ADD COLUMN unidade_origem varchar(255), ADD COLUMN nome_produto_snapshot varchar(255);
DO $$ BEGIN
 IF EXISTS (SELECT 1 FROM movimentacao_estoque WHERE id_item_devolucao_cliente IS NOT NULL GROUP BY id_item_devolucao_cliente HAVING count(*)>1) THEN
 RAISE EXCEPTION 'Return reconciliation required: SELECT id_item_devolucao_cliente,array_agg(id) FROM movimentacao_estoque GROUP BY 1 HAVING count(*)>1';
 END IF;
END $$;
CREATE UNIQUE INDEX devolucao_cliente_unique_movement ON movimentacao_estoque(id_item_devolucao_cliente) WHERE id_item_devolucao_cliente IS NOT NULL;
CREATE INDEX devolucao_cliente_source ON devolucao_cliente(id_venda);
CREATE INDEX item_devolucao_cliente_source ON item_devolucao_cliente(id_item_venda);
CREATE TABLE devolucao_cliente_idempotencia(chave uuid PRIMARY KEY,request_hash varchar(64) NOT NULL, devolucao_id uuid NOT NULL UNIQUE REFERENCES devolucao_cliente(id),actor_id uuid NOT NULL REFERENCES usuario(id));
ALTER TABLE devolucao_cliente ADD CONSTRAINT devolucao_cliente_lifecycle CHECK (legado OR
(motivo IS NOT NULL AND length(btrim(motivo))>0 AND id_usuario IS NOT NULL AND id_modificador IS NOT NULL AND
((status='RASCUNHO' AND concluido_em IS NULL AND cancelado_em IS NULL) OR
(status='CONCLUIDA' AND concluido_em IS NOT NULL AND id_confirmador IS NOT NULL AND cancelado_em IS NULL) OR
(status='CANCELADA' AND cancelado_em IS NOT NULL AND id_cancelador IS NOT NULL AND motivo_cancelamento IS NOT NULL AND length(btrim(motivo_cancelamento))>0 AND concluido_em IS NULL))));

ALTER TABLE devolucao_fornecedor ADD COLUMN versao integer NOT NULL DEFAULT 0,
 ADD COLUMN atualizado_em timestamptz, ADD COLUMN concluido_em timestamptz,
 ADD COLUMN cancelado_em timestamptz, ADD COLUMN motivo_cancelamento text,
 ADD COLUMN id_modificador uuid REFERENCES usuario(id), ADD COLUMN id_confirmador uuid REFERENCES usuario(id),
 ADD COLUMN id_cancelador uuid REFERENCES usuario(id), ADD COLUMN legado boolean NOT NULL DEFAULT false;
UPDATE devolucao_fornecedor SET legado=true;
ALTER TABLE item_devolucao_fornecedor ADD COLUMN unidade_sigla_snapshot varchar(10), ADD COLUMN unidade_origem varchar(255), ADD COLUMN nome_produto_snapshot varchar(255);
DO $$ BEGIN
 IF EXISTS (SELECT 1 FROM movimentacao_estoque WHERE id_item_devolucao_fornecedor IS NOT NULL GROUP BY id_item_devolucao_fornecedor HAVING count(*)>1) THEN
 RAISE EXCEPTION 'Return reconciliation required: SELECT id_item_devolucao_fornecedor,array_agg(id) FROM movimentacao_estoque GROUP BY 1 HAVING count(*)>1';
 END IF;
END $$;
CREATE UNIQUE INDEX devolucao_fornecedor_unique_movement ON movimentacao_estoque(id_item_devolucao_fornecedor) WHERE id_item_devolucao_fornecedor IS NOT NULL;
CREATE INDEX devolucao_fornecedor_source ON devolucao_fornecedor(id_compra);
CREATE INDEX item_devolucao_fornecedor_source ON item_devolucao_fornecedor(id_item_compra);
CREATE TABLE devolucao_fornecedor_idempotencia(chave uuid PRIMARY KEY,request_hash varchar(64) NOT NULL, devolucao_id uuid NOT NULL UNIQUE REFERENCES devolucao_fornecedor(id),actor_id uuid NOT NULL REFERENCES usuario(id));
ALTER TABLE devolucao_fornecedor ADD CONSTRAINT devolucao_fornecedor_lifecycle CHECK (legado OR
(motivo IS NOT NULL AND length(btrim(motivo))>0 AND id_usuario IS NOT NULL AND id_modificador IS NOT NULL AND
((status='RASCUNHO' AND concluido_em IS NULL AND cancelado_em IS NULL) OR
(status='CONCLUIDA' AND concluido_em IS NOT NULL AND id_confirmador IS NOT NULL AND cancelado_em IS NULL) OR
(status='CANCELADA' AND cancelado_em IS NOT NULL AND id_cancelador IS NOT NULL AND motivo_cancelamento IS NOT NULL AND length(btrim(motivo_cancelamento))>0 AND concluido_em IS NULL))));

CREATE FUNCTION devolucao_cliente_quantidade(source_item uuid) RETURNS numeric LANGUAGE sql STABLE AS $$
 SELECT coalesce(sum(i.quantidade),0) FROM item_devolucao_cliente i JOIN devolucao_cliente d ON d.id=i.id_devolucao_cliente
 WHERE i.id_item_venda=source_item AND d.status='CONCLUIDA' AND NOT d.legado
 $$;
CREATE FUNCTION devolucao_cliente_legada(source_id uuid) RETURNS boolean LANGUAGE sql STABLE AS $$
 SELECT exists(SELECT 1 FROM devolucao_cliente d WHERE d.id_venda=source_id AND d.legado)
 $$;

CREATE FUNCTION devolucao_fornecedor_quantidade(source_item uuid) RETURNS numeric LANGUAGE sql STABLE AS $$
 SELECT coalesce(sum(i.quantidade),0) FROM item_devolucao_fornecedor i JOIN devolucao_fornecedor d ON d.id=i.id_devolucao_fornecedor
 WHERE i.id_item_compra=source_item AND d.status='CONCLUIDA' AND NOT d.legado
 $$;
CREATE FUNCTION devolucao_fornecedor_legada(source_id uuid) RETURNS boolean LANGUAGE sql STABLE AS $$
 SELECT exists(SELECT 1 FROM devolucao_fornecedor d WHERE d.id_compra=source_id AND d.legado)
 $$;
