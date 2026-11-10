-- Preserve history; never infer or apply legacy stock effects.
DO $$ BEGIN
 IF EXISTS (SELECT 1 FROM produto_venda WHERE quantidade<=0 OR preco_unitario<0 OR desconto<0
   OR desconto>quantidade*preco_unitario OR total_item<>round(quantidade*preco_unitario-desconto,2))
 OR EXISTS (SELECT 1 FROM venda v WHERE subtotal<0 OR desconto_total<0 OR valor_total<>subtotal-desconto_total
   OR subtotal<>coalesce((SELECT round(sum(quantidade*preco_unitario),2) FROM produto_venda i WHERE i.id_venda=v.id),0)
   OR desconto_total<>coalesce((SELECT sum(desconto) FROM produto_venda i WHERE i.id_venda=v.id),0)) THEN
   RAISE EXCEPTION 'Invalid legacy sales: run scripts/sales-preflight.sql and reconcile before V4';
 END IF;
 IF EXISTS (SELECT 1 FROM movimentacao_estoque WHERE tipo='SAIDA_VENDA' AND id_item_venda IS NOT NULL GROUP BY id_item_venda HAVING count(*)>1) THEN
   RAISE EXCEPTION 'Duplicate legacy sale movements: reconcile before V4';
 END IF;
END $$;
ALTER TABLE venda ADD COLUMN versao integer NOT NULL DEFAULT 0;
ALTER TABLE venda ADD COLUMN atualizado_em timestamptz;
ALTER TABLE venda ADD COLUMN concluido_em timestamptz;
ALTER TABLE venda ADD COLUMN cancelado_em timestamptz;
ALTER TABLE venda ADD COLUMN motivo_cancelamento text;
ALTER TABLE venda ADD COLUMN id_modificador uuid REFERENCES usuario(id);
ALTER TABLE venda ADD COLUMN id_confirmador uuid REFERENCES usuario(id);
ALTER TABLE venda ADD COLUMN id_cancelador uuid REFERENCES usuario(id);
ALTER TABLE venda ADD COLUMN legado boolean NOT NULL DEFAULT false;
UPDATE venda SET legado=true WHERE status<>'ABERTA';
CREATE TABLE venda_idempotencia (
 chave uuid PRIMARY KEY, request_hash varchar(64) NOT NULL,
 venda_id uuid NOT NULL UNIQUE REFERENCES venda(id), actor_id uuid NOT NULL REFERENCES usuario(id)
);
CREATE UNIQUE INDEX venda_unique_movement ON movimentacao_estoque(id_item_venda)
 WHERE tipo='SAIDA_VENDA' AND id_item_venda IS NOT NULL;
ALTER TABLE produto_venda ADD CONSTRAINT venda_item_values CHECK
 (quantidade>0 AND preco_unitario>=0 AND desconto>=0 AND desconto<=quantidade*preco_unitario
 AND total_item=round(quantidade*preco_unitario-desconto,2));
ALTER TABLE venda ADD CONSTRAINT venda_lifecycle_audit CHECK (legado OR
 (status='ABERTA' AND concluido_em IS NULL AND cancelado_em IS NULL AND id_confirmador IS NULL AND id_cancelador IS NULL) OR
 (status='CONCLUIDA' AND concluido_em IS NOT NULL AND id_confirmador IS NOT NULL AND cancelado_em IS NULL AND id_cancelador IS NULL) OR
 (status='CANCELADA' AND cancelado_em IS NOT NULL AND id_cancelador IS NOT NULL AND length(btrim(motivo_cancelamento))>0 AND concluido_em IS NULL AND id_confirmador IS NULL));
