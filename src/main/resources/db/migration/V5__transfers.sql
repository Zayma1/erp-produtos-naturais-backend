-- All preexisting transfers are historical. Never infer or replay stock effects.
ALTER TABLE transferencia_estoque ADD COLUMN versao integer NOT NULL DEFAULT 0;
ALTER TABLE transferencia_estoque ADD COLUMN atualizado_em timestamptz;
ALTER TABLE transferencia_estoque ADD COLUMN concluido_em timestamptz;
ALTER TABLE transferencia_estoque ADD COLUMN cancelado_em timestamptz;
ALTER TABLE transferencia_estoque ADD COLUMN motivo_cancelamento text;
ALTER TABLE transferencia_estoque ADD COLUMN id_modificador uuid REFERENCES usuario(id);
ALTER TABLE transferencia_estoque ADD COLUMN id_confirmador uuid REFERENCES usuario(id);
ALTER TABLE transferencia_estoque ADD COLUMN id_cancelador uuid REFERENCES usuario(id);
ALTER TABLE transferencia_estoque ADD COLUMN legado boolean NOT NULL DEFAULT false;
UPDATE transferencia_estoque SET legado=true;
ALTER TABLE item_transferencia ADD COLUMN unidade_sigla_snapshot varchar(10);
ALTER TABLE movimentacao_estoque ADD COLUMN id_item_transferencia uuid REFERENCES item_transferencia(id);
CREATE UNIQUE INDEX transferencia_unique_movement ON movimentacao_estoque(id_item_transferencia,tipo)
 WHERE id_item_transferencia IS NOT NULL AND tipo IN ('TRANSFERENCIA_SAIDA','TRANSFERENCIA_ENTRADA');
CREATE TABLE transferencia_idempotencia (
 chave uuid PRIMARY KEY, request_hash varchar(64) NOT NULL,
 transferencia_id uuid NOT NULL UNIQUE REFERENCES transferencia_estoque(id), actor_id uuid NOT NULL REFERENCES usuario(id)
);
ALTER TABLE transferencia_estoque ADD CONSTRAINT transferencia_lifecycle CHECK (legado OR
 (id_loja_origem<>id_loja_destino AND id_usuario IS NOT NULL AND id_modificador IS NOT NULL AND
 ((status='RASCUNHO' AND concluido_em IS NULL AND cancelado_em IS NULL AND id_confirmador IS NULL AND id_cancelador IS NULL) OR
 (status='CONCLUIDA' AND concluido_em IS NOT NULL AND id_confirmador IS NOT NULL AND cancelado_em IS NULL AND id_cancelador IS NULL) OR
 (status='CANCELADA' AND cancelado_em IS NOT NULL AND id_cancelador IS NOT NULL AND motivo_cancelamento IS NOT NULL AND length(btrim(motivo_cancelamento))>0 AND concluido_em IS NULL AND id_confirmador IS NULL))));
