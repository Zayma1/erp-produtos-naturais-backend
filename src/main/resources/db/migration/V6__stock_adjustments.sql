-- Historical effects must be reconciled externally; never replay legacy documents.
DO $$ BEGIN
 IF EXISTS (SELECT 1 FROM item_ajuste_estoque GROUP BY id_ajuste_estoque,id_produto HAVING count(*)>1) THEN
 RAISE EXCEPTION 'Adjustment reconciliation required: SELECT id_ajuste_estoque,id_produto,array_agg(id) FROM item_ajuste_estoque GROUP BY 1,2 HAVING count(*)>1';
 END IF;
 IF EXISTS (SELECT 1 FROM movimentacao_estoque WHERE id_item_ajuste_estoque IS NOT NULL GROUP BY id_item_ajuste_estoque HAVING count(*)>1) THEN
 RAISE EXCEPTION 'Adjustment reconciliation required: duplicate movements. Run SELECT id_item_ajuste_estoque, array_agg(id) FROM movimentacao_estoque WHERE id_item_ajuste_estoque IS NOT NULL GROUP BY 1 HAVING count(*)>1';
 END IF;
 IF EXISTS (SELECT 1 FROM item_ajuste_estoque WHERE quantidade<=0 OR tipo NOT IN ('AJUSTE_ENTRADA','AJUSTE_SAIDA')) THEN
 RAISE EXCEPTION 'Adjustment reconciliation required: inspect SELECT * FROM item_ajuste_estoque WHERE quantidade<=0 OR tipo NOT IN (AJUSTE_ENTRADA,AJUSTE_SAIDA)';
 END IF;
END $$;
ALTER TABLE ajuste_estoque ADD COLUMN versao integer NOT NULL DEFAULT 0;
ALTER TABLE ajuste_estoque ADD COLUMN atualizado_em timestamptz;
ALTER TABLE ajuste_estoque ADD COLUMN cancelado_em timestamptz;
ALTER TABLE ajuste_estoque ADD COLUMN motivo_cancelamento text;
ALTER TABLE ajuste_estoque ADD COLUMN id_modificador uuid REFERENCES usuario(id);
ALTER TABLE ajuste_estoque ADD COLUMN id_confirmador uuid REFERENCES usuario(id);
ALTER TABLE ajuste_estoque ADD COLUMN id_cancelador uuid REFERENCES usuario(id);
ALTER TABLE ajuste_estoque ADD COLUMN legado boolean NOT NULL DEFAULT false;
UPDATE ajuste_estoque SET legado=true;
ALTER TABLE item_ajuste_estoque ADD COLUMN unidade_sigla_snapshot varchar(10);
ALTER TABLE item_ajuste_estoque ADD COLUMN saldo_antes numeric(14,3);
ALTER TABLE item_ajuste_estoque ADD COLUMN saldo_depois numeric(14,3);
CREATE UNIQUE INDEX ajuste_unique_movement ON movimentacao_estoque(id_item_ajuste_estoque) WHERE id_item_ajuste_estoque IS NOT NULL;
CREATE TABLE ajuste_idempotencia(chave uuid PRIMARY KEY,request_hash varchar(64) NOT NULL, ajuste_id uuid NOT NULL UNIQUE REFERENCES ajuste_estoque(id),actor_id uuid NOT NULL REFERENCES usuario(id));
ALTER TABLE ajuste_estoque ADD CONSTRAINT ajuste_lifecycle CHECK (legado OR
(length(btrim(justificativa))>0 AND id_modificador IS NOT NULL AND
((status='RASCUNHO' AND concluido_em IS NULL AND cancelado_em IS NULL) OR
(status='CONCLUIDO' AND concluido_em IS NOT NULL AND id_confirmador IS NOT NULL AND cancelado_em IS NULL) OR
(status='CANCELADO' AND cancelado_em IS NOT NULL AND id_cancelador IS NOT NULL AND length(btrim(motivo_cancelamento))>0 AND concluido_em IS NULL))));

CREATE UNIQUE INDEX ajuste_unique_product ON item_ajuste_estoque(id_ajuste_estoque,id_produto);
