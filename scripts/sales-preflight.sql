-- Read-only reconciliation report. No movements or quantities are fabricated.
SELECT v.id,v.status,i.id AS item_id,'invalid monetary data' AS issue
FROM venda v LEFT JOIN produto_venda i ON i.id_venda=v.id
WHERE i.quantidade<=0 OR i.preco_unitario<0 OR i.desconto<0 OR i.desconto>i.quantidade*i.preco_unitario
 OR i.total_item<>round(i.quantidade*i.preco_unitario-i.desconto,2)
 OR v.subtotal<>coalesce((SELECT round(sum(quantidade*preco_unitario),2) FROM produto_venda WHERE id_venda=v.id),0)
 OR v.desconto_total<>coalesce((SELECT sum(desconto) FROM produto_venda WHERE id_venda=v.id),0)
 OR v.valor_total<>v.subtotal-v.desconto_total;
SELECT v.id,i.id AS item_id,'unverifiable historical completion; read-only, no replay' AS issue
FROM venda v LEFT JOIN produto_venda i ON i.id_venda=v.id
WHERE v.status='CONCLUIDA' AND (i.id IS NULL OR
 (SELECT count(*) FROM movimentacao_estoque m WHERE m.id_item_venda=i.id AND m.id_venda=v.id
 AND m.id_loja=v.id_loja AND m.id_produto=i.id_produto AND m.tipo='SAIDA_VENDA' AND m.quantidade=i.quantidade)<>1);
SELECT id_item_venda,count(*) AS movements,'duplicate sale movements; reconcile before V4' AS issue
FROM movimentacao_estoque WHERE tipo='SAIDA_VENDA' AND id_item_venda IS NOT NULL
GROUP BY id_item_venda HAVING count(*)>1;
