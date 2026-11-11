package com.vilaverde.erp;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.UUID;
final class SkuFixtures {
 static void create(JdbcTemplate sql,UUID sku,UUID category,UUID unit,String name) {
  UUID product=UUID.randomUUID();
  sql.update("insert into produto(id,nome,id_categoria,status,criado_em) values (?,?,?,'ATIVO',now())",product,name,category);
  sql.update("insert into produto_sku(id,id_produto,id_unidade_medida,tipo_venda,status,sku,descricao_apresentacao,criado_em) values (?,?,?,'PESO','ATIVO',?,'Granel',now())",sku,product,unit,sku.toString());
 }
 static void cloneSku(JdbcTemplate sql,UUID id,UUID source,String name) {
  sql.update("insert into produto_sku(id,id_produto,id_unidade_medida,tipo_venda,status,sku,descricao_apresentacao,criado_em) select ?,id_produto,id_unidade_medida,tipo_venda,status,?, ?,now() from produto_sku where id=?",id,id.toString(),name,source);
 }
}
