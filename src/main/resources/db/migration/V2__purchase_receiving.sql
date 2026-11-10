-- Historical stock effects cannot be inferred or replayed safely. Reconcile before migrating.
DO $$ BEGIN
  IF EXISTS (SELECT 1 FROM recebimento_compra WHERE status <> 'RASCUNHO')
     OR EXISTS (SELECT 1 FROM item_compra WHERE quantidade_recebida <> 0)
     OR EXISTS (SELECT 1 FROM movimentacao_estoque WHERE tipo = 'ENTRADA_COMPRA')
     OR EXISTS (SELECT 1 FROM compra WHERE status IN ('PARCIALMENTE_RECEBIDA','RECEBIDA')) THEN
    RAISE EXCEPTION 'Legacy receiving requires manual reconciliation; no receipts will be replayed';
  END IF;
  IF EXISTS (SELECT 1 FROM pg_trigger t WHERE NOT t.tgisinternal AND t.tgrelid IN
     ('recebimento_compra'::regclass,'item_recebimento_compra'::regclass,'compra'::regclass,
      'item_compra'::regclass,'estoque_produto'::regclass,'movimentacao_estoque'::regclass)) THEN
    RAISE EXCEPTION 'Unversioned triggers require review and reconciliation before receiving';
  END IF;
  IF EXISTS (SELECT 1 FROM item_recebimento_compra i JOIN recebimento_compra r ON r.id=i.id_recebimento_compra
    JOIN item_compra c ON c.id=i.id_item_compra
    WHERE c.id_compra <> r.id_compra OR c.id_produto <> i.id_produto OR i.quantidade <= 0 OR i.quantidade > c.quantidade)
    OR EXISTS (SELECT 1 FROM item_recebimento_compra GROUP BY id_recebimento_compra,id_item_compra HAVING count(*) > 1)
    OR EXISTS (SELECT 1 FROM recebimento_compra r WHERE r.id_usuario IS NULL OR r.data_recebimento IS NULL
      OR NOT EXISTS (SELECT 1 FROM item_recebimento_compra i WHERE i.id_recebimento_compra=r.id)) THEN
    RAISE EXCEPTION 'Invalid legacy receipt drafts require manual reconciliation';
  END IF;
END $$;
ALTER TABLE recebimento_compra ADD COLUMN versao integer NOT NULL DEFAULT 0;
ALTER TABLE recebimento_compra ADD COLUMN concluido_em timestamptz;
ALTER TABLE recebimento_compra ALTER COLUMN id_usuario SET NOT NULL;
ALTER TABLE recebimento_compra ALTER COLUMN data_recebimento SET NOT NULL;
ALTER TABLE recebimento_compra ADD CONSTRAINT recebimento_completion CHECK
  ((status = 'RASCUNHO' AND concluido_em IS NULL) OR (status = 'CONCLUIDO' AND concluido_em IS NOT NULL));
ALTER TABLE item_recebimento_compra ADD CONSTRAINT recebimento_unique_item UNIQUE(id_recebimento_compra,id_item_compra);
ALTER TABLE item_recebimento_compra ADD CONSTRAINT recebimento_positive_quantity CHECK (quantidade > 0);
ALTER TABLE item_compra ADD CONSTRAINT compra_received_quantity CHECK (quantidade_recebida >= 0 AND quantidade_recebida <= quantidade);
CREATE UNIQUE INDEX recebimento_unique_movement ON movimentacao_estoque(id_item_recebimento_compra) WHERE id_item_recebimento_compra IS NOT NULL;
CREATE INDEX recebimento_purchase ON recebimento_compra(id_compra);
CREATE TABLE recebimento_idempotencia (
  chave uuid PRIMARY KEY,
  request_hash varchar(64) NOT NULL,
  receipt_id uuid NOT NULL UNIQUE,
  deleted boolean NOT NULL DEFAULT false
  -- Deliberately no FK: the identity remains a tombstone after a draft is deleted.
);
