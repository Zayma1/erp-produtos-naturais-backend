






CREATE TABLE public.ajuste_estoque (
    concluido_em timestamp(6) with time zone,
    criado_em timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    id_loja uuid NOT NULL,
    id_usuario uuid NOT NULL,
    status character varying(20) NOT NULL,
    justificativa character varying(255) NOT NULL,
    CONSTRAINT ajuste_estoque_status_check CHECK (((status)::text = ANY ((ARRAY['RASCUNHO'::character varying, 'CONCLUIDO'::character varying, 'CONCLUIDA'::character varying, 'CANCELADO'::character varying, 'CANCELADA'::character varying])::text[])))
);



CREATE TABLE public.categoria (
    id uuid NOT NULL,
    nome character varying(255) NOT NULL
);



CREATE TABLE public.cliente (
    data_nascimento date,
    criado_em timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    cpf character varying(255),
    email character varying(255),
    nome character varying(255) NOT NULL,
    telefone character varying(255)
);



CREATE TABLE public.compra (
    valor_total numeric(14,2) NOT NULL,
    criado_em timestamp(6) with time zone NOT NULL,
    data_compra timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    id_fornecedor uuid NOT NULL,
    id_loja uuid NOT NULL,
    id_usuario uuid,
    status character varying(30) NOT NULL,
    observacao character varying(255),
    CONSTRAINT compra_status_check CHECK (((status)::text = ANY ((ARRAY['RASCUNHO'::character varying, 'PARCIALMENTE_RECEBIDA'::character varying, 'RECEBIDA'::character varying, 'CANCELADA'::character varying])::text[])))
);



CREATE TABLE public.devolucao_cliente (
    criado_em timestamp(6) with time zone NOT NULL,
    data_devolucao timestamp(6) with time zone,
    id uuid NOT NULL,
    id_usuario uuid,
    id_venda uuid NOT NULL,
    status character varying(20) NOT NULL,
    motivo character varying(255),
    observacao character varying(255),
    CONSTRAINT devolucao_cliente_status_check CHECK (((status)::text = ANY ((ARRAY['RASCUNHO'::character varying, 'CONCLUIDO'::character varying, 'CONCLUIDA'::character varying, 'CANCELADO'::character varying, 'CANCELADA'::character varying])::text[])))
);



CREATE TABLE public.devolucao_fornecedor (
    criado_em timestamp(6) with time zone NOT NULL,
    data_devolucao timestamp(6) with time zone,
    id uuid NOT NULL,
    id_compra uuid NOT NULL,
    id_usuario uuid,
    status character varying(20) NOT NULL,
    motivo character varying(255),
    observacao character varying(255),
    CONSTRAINT devolucao_fornecedor_status_check CHECK (((status)::text = ANY ((ARRAY['RASCUNHO'::character varying, 'CONCLUIDO'::character varying, 'CONCLUIDA'::character varying, 'CANCELADO'::character varying, 'CANCELADA'::character varying])::text[])))
);



CREATE TABLE public.estoque_produto (
    quantidade_atual numeric(14,3) NOT NULL,
    atualizado_em timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    id_loja uuid NOT NULL,
    id_produto uuid NOT NULL
);



CREATE TABLE public.fornecedor (
    estado character varying(2) NOT NULL,
    id uuid NOT NULL,
    status character varying(20) NOT NULL,
    bairro character varying(255) NOT NULL,
    cep character varying(255),
    cidade character varying(255) NOT NULL,
    cnpj character varying(255),
    complemento character varying(255),
    cpf character varying(255),
    email character varying(255),
    logradouro character varying(255),
    nome_fantasia character varying(255) NOT NULL,
    numero character varying(255),
    razao_social character varying(255) NOT NULL,
    telefone character varying(255),
    CONSTRAINT fornecedor_status_check CHECK (((status)::text = ANY ((ARRAY['ATIVO'::character varying, 'INATIVO'::character varying])::text[])))
);



CREATE TABLE public.item_ajuste_estoque (
    quantidade numeric(14,3) NOT NULL,
    id uuid NOT NULL,
    id_ajuste_estoque uuid NOT NULL,
    id_produto uuid NOT NULL,
    tipo character varying(30) NOT NULL,
    observacao character varying(255),
    CONSTRAINT item_ajuste_estoque_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['ENTRADA_COMPRA'::character varying, 'SAIDA_VENDA'::character varying, 'AJUSTE_ENTRADA'::character varying, 'AJUSTE_SAIDA'::character varying, 'TRANSFERENCIA_ENTRADA'::character varying, 'TRANSFERENCIA_SAIDA'::character varying, 'DEVOLUCAO'::character varying, 'DEVOLUCAO_FORNECEDOR'::character varying])::text[])))
);



CREATE TABLE public.item_compra (
    custo_unitario numeric(14,4) NOT NULL,
    desconto numeric(14,2) NOT NULL,
    quantidade numeric(14,3) NOT NULL,
    quantidade_recebida numeric(14,3) NOT NULL,
    total_item numeric(14,2) NOT NULL,
    id uuid NOT NULL,
    id_compra uuid NOT NULL,
    id_produto uuid NOT NULL
);



CREATE TABLE public.item_devolucao_cliente (
    quantidade numeric(14,3) NOT NULL,
    retorna_ao_estoque boolean NOT NULL,
    id uuid NOT NULL,
    id_devolucao_cliente uuid NOT NULL,
    id_item_venda uuid NOT NULL,
    id_produto uuid NOT NULL,
    observacao character varying(255)
);



CREATE TABLE public.item_devolucao_fornecedor (
    quantidade numeric(14,3) NOT NULL,
    id uuid NOT NULL,
    id_devolucao_fornecedor uuid NOT NULL,
    id_item_compra uuid NOT NULL,
    id_produto uuid NOT NULL,
    observacao character varying(255)
);



CREATE TABLE public.item_recebimento_compra (
    quantidade numeric(14,3) NOT NULL,
    id uuid NOT NULL,
    id_item_compra uuid NOT NULL,
    id_produto uuid NOT NULL,
    id_recebimento_compra uuid NOT NULL,
    observacao character varying(255)
);



CREATE TABLE public.item_transferencia (
    quantidade numeric(14,3) NOT NULL,
    id uuid NOT NULL,
    id_produto uuid NOT NULL,
    id_transferencia uuid NOT NULL
);



CREATE TABLE public.loja (
    criado_em timestamp(6) with time zone NOT NULL,
    cnpj character varying(14) NOT NULL,
    id uuid NOT NULL,
    status character varying(20) NOT NULL,
    nome_fantasia character varying(150) NOT NULL,
    bairro character varying(255) NOT NULL,
    cep character varying(255),
    cidade character varying(255) NOT NULL,
    complemento character varying(255),
    estado character varying(255) NOT NULL,
    logradouro character varying(255),
    numero character varying(255) NOT NULL,
    razao_social character varying(255) NOT NULL,
    CONSTRAINT loja_status_check CHECK (((status)::text = ANY ((ARRAY['ATIVA'::character varying, 'INATIVA'::character varying])::text[])))
);



CREATE TABLE public.movimentacao_estoque (
    quantidade numeric(14,3) NOT NULL,
    data_movimentacao timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    id_ajuste_estoque uuid,
    id_compra uuid,
    id_devolucao_cliente uuid,
    id_devolucao_fornecedor uuid,
    id_item_ajuste_estoque uuid,
    id_item_devolucao_cliente uuid,
    id_item_devolucao_fornecedor uuid,
    id_item_recebimento_compra uuid,
    id_item_venda uuid,
    id_loja uuid NOT NULL,
    id_movimentacao_origem uuid,
    id_produto uuid NOT NULL,
    id_transferencia uuid,
    id_usuario uuid,
    id_venda uuid,
    tipo character varying(30) NOT NULL,
    observacao character varying(255),
    CONSTRAINT movimentacao_estoque_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['ENTRADA_COMPRA'::character varying, 'SAIDA_VENDA'::character varying, 'AJUSTE_ENTRADA'::character varying, 'AJUSTE_SAIDA'::character varying, 'TRANSFERENCIA_ENTRADA'::character varying, 'TRANSFERENCIA_SAIDA'::character varying, 'DEVOLUCAO'::character varying, 'DEVOLUCAO_FORNECEDOR'::character varying])::text[])))
);



CREATE TABLE public.produto (
    criado_em timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    id_categoria uuid NOT NULL,
    id_unidade_medida uuid NOT NULL,
    tipo_venda character varying(20) NOT NULL,
    descricao character varying(255),
    nome character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    CONSTRAINT produto_status_check CHECK (((status)::text = ANY ((ARRAY['ATIVO'::character varying, 'INATIVO'::character varying])::text[]))),
    CONSTRAINT produto_tipo_venda_check CHECK (((tipo_venda)::text = ANY ((ARRAY['UNIDADE'::character varying, 'PESO'::character varying])::text[])))
);



CREATE TABLE public.produto_loja (
    criado_em timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    id_loja uuid NOT NULL,
    id_produto uuid NOT NULL,
    status character varying(20) NOT NULL,
    CONSTRAINT produto_loja_status_check CHECK (((status)::text = ANY ((ARRAY['ATIVO'::character varying, 'INATIVO'::character varying])::text[])))
);



CREATE TABLE public.produto_preco_loja (
    preco numeric(12,2) NOT NULL,
    criado_em timestamp(6) with time zone NOT NULL,
    data_fim timestamp(6) with time zone,
    data_inicio timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    id_loja uuid NOT NULL,
    id_produto uuid NOT NULL
);



CREATE TABLE public.produto_sku (
    criado_em timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    id_produto uuid NOT NULL,
    sku character varying(80) NOT NULL,
    descricao_apresentacao character varying(255),
    dun14 character varying(255),
    ean13 character varying(255),
    status character varying(255) NOT NULL,
    CONSTRAINT produto_sku_status_check CHECK (((status)::text = ANY ((ARRAY['ATIVO'::character varying, 'INATIVO'::character varying])::text[])))
);



CREATE TABLE public.produto_venda (
    desconto numeric(14,2) NOT NULL,
    preco_unitario numeric(12,2) NOT NULL,
    quantidade numeric(14,3) NOT NULL,
    total_item numeric(14,2) NOT NULL,
    unidade_sigla_snapshot character varying(10) NOT NULL,
    id uuid NOT NULL,
    id_produto uuid NOT NULL,
    id_produto_sku uuid,
    id_venda uuid NOT NULL
);



CREATE TABLE public.recebimento_compra (
    criado_em timestamp(6) with time zone NOT NULL,
    data_recebimento timestamp(6) with time zone,
    id uuid NOT NULL,
    id_compra uuid NOT NULL,
    id_usuario uuid,
    status character varying(20) NOT NULL,
    observacao character varying(255),
    CONSTRAINT recebimento_compra_status_check CHECK (((status)::text = ANY ((ARRAY['RASCUNHO'::character varying, 'CONCLUIDO'::character varying, 'CONCLUIDA'::character varying, 'CANCELADO'::character varying, 'CANCELADA'::character varying])::text[])))
);



CREATE TABLE public.transferencia_estoque (
    criado_em timestamp(6) with time zone NOT NULL,
    data_transferencia timestamp(6) with time zone,
    id uuid NOT NULL,
    id_loja_destino uuid NOT NULL,
    id_loja_origem uuid NOT NULL,
    id_usuario uuid,
    status character varying(20) NOT NULL,
    observacao character varying(255),
    CONSTRAINT transferencia_estoque_status_check CHECK (((status)::text = ANY ((ARRAY['RASCUNHO'::character varying, 'CONCLUIDO'::character varying, 'CONCLUIDA'::character varying, 'CANCELADO'::character varying, 'CANCELADA'::character varying])::text[])))
);



CREATE TABLE public.unidade_medida (
    id uuid NOT NULL,
    nome character varying(255) NOT NULL,
    sigla character varying(255) NOT NULL
);



CREATE TABLE public.usuario (
    criado_em timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    status character varying(20) NOT NULL,
    cargo character varying(255) NOT NULL,
    email character varying(255) NOT NULL,
    nome character varying(255) NOT NULL,
    senha_hash character varying(255) NOT NULL,
    CONSTRAINT usuario_status_check CHECK (((status)::text = ANY ((ARRAY['ATIVO'::character varying, 'INATIVO'::character varying])::text[])))
);



CREATE TABLE public.venda (
    desconto_total numeric(14,2) NOT NULL,
    subtotal numeric(14,2) NOT NULL,
    valor_total numeric(14,2) NOT NULL,
    criado_em timestamp(6) with time zone NOT NULL,
    data_venda timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    id_cliente uuid NOT NULL,
    id_loja uuid NOT NULL,
    id_usuario uuid NOT NULL,
    status character varying(20) NOT NULL,
    observacao character varying(255),
    CONSTRAINT venda_status_check CHECK (((status)::text = ANY ((ARRAY['ABERTA'::character varying, 'CONCLUIDA'::character varying, 'CANCELADA'::character varying])::text[])))
);



ALTER TABLE ONLY public.ajuste_estoque
    ADD CONSTRAINT ajuste_estoque_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.categoria
    ADD CONSTRAINT categoria_nome_key UNIQUE (nome);



ALTER TABLE ONLY public.categoria
    ADD CONSTRAINT categoria_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.cliente
    ADD CONSTRAINT cliente_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.compra
    ADD CONSTRAINT compra_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.devolucao_cliente
    ADD CONSTRAINT devolucao_cliente_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.devolucao_fornecedor
    ADD CONSTRAINT devolucao_fornecedor_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.estoque_produto
    ADD CONSTRAINT estoque_produto_id_produto_id_loja_key UNIQUE (id_produto, id_loja);



ALTER TABLE ONLY public.estoque_produto
    ADD CONSTRAINT estoque_produto_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.fornecedor
    ADD CONSTRAINT fornecedor_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.item_ajuste_estoque
    ADD CONSTRAINT item_ajuste_estoque_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.item_compra
    ADD CONSTRAINT item_compra_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.item_devolucao_cliente
    ADD CONSTRAINT item_devolucao_cliente_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.item_devolucao_fornecedor
    ADD CONSTRAINT item_devolucao_fornecedor_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.item_recebimento_compra
    ADD CONSTRAINT item_recebimento_compra_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.item_transferencia
    ADD CONSTRAINT item_transferencia_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.loja
    ADD CONSTRAINT loja_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.movimentacao_estoque
    ADD CONSTRAINT movimentacao_estoque_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.produto_loja
    ADD CONSTRAINT produto_loja_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.produto
    ADD CONSTRAINT produto_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.produto_preco_loja
    ADD CONSTRAINT produto_preco_loja_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.produto_sku
    ADD CONSTRAINT produto_sku_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.produto_sku
    ADD CONSTRAINT produto_sku_sku_key UNIQUE (sku);



ALTER TABLE ONLY public.produto_venda
    ADD CONSTRAINT produto_venda_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.recebimento_compra
    ADD CONSTRAINT recebimento_compra_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.transferencia_estoque
    ADD CONSTRAINT transferencia_estoque_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.unidade_medida
    ADD CONSTRAINT unidade_medida_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.produto_loja
    ADD CONSTRAINT uq_produto_loja_produto_loja UNIQUE (id_produto, id_loja);



ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_email_key UNIQUE (email);



ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.venda
    ADD CONSTRAINT venda_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.item_recebimento_compra
    ADD CONSTRAINT "FK132eqpukbusmbmhqrn9hx4nfs" FOREIGN KEY (id_produto) REFERENCES public.produto(id);



ALTER TABLE ONLY public.movimentacao_estoque
    ADD CONSTRAINT "FK13k3muaabyhtnwnp1q0yi8wyp" FOREIGN KEY (id_ajuste_estoque) REFERENCES public.ajuste_estoque(id);



ALTER TABLE ONLY public.produto_venda
    ADD CONSTRAINT "FK151lggqqkpp5h672duf4kiret" FOREIGN KEY (id_produto) REFERENCES public.produto(id);



ALTER TABLE ONLY public.produto_venda
    ADD CONSTRAINT "FK1j9x9gn006r774sbaxmfk1qnr" FOREIGN KEY (id_produto_sku) REFERENCES public.produto_sku(id);



ALTER TABLE ONLY public.compra
    ADD CONSTRAINT "FK249mhjw1nla492hj7wgnu387r" FOREIGN KEY (id_fornecedor) REFERENCES public.fornecedor(id);



ALTER TABLE ONLY public.item_ajuste_estoque
    ADD CONSTRAINT "FK3aedax1i8ipge8933fn7h0ar5" FOREIGN KEY (id_ajuste_estoque) REFERENCES public.ajuste_estoque(id);



ALTER TABLE ONLY public.item_recebimento_compra
    ADD CONSTRAINT "FK575hg6t2jy66ab7io49dsbdak" FOREIGN KEY (id_recebimento_compra) REFERENCES public.recebimento_compra(id);



ALTER TABLE ONLY public.recebimento_compra
    ADD CONSTRAINT "FK5cd9f370oha3xhvqaklvvmike" FOREIGN KEY (id_usuario) REFERENCES public.usuario(id);



ALTER TABLE ONLY public.item_devolucao_fornecedor
    ADD CONSTRAINT "FK7enryont2i469ukp30aw35ody" FOREIGN KEY (id_devolucao_fornecedor) REFERENCES public.devolucao_fornecedor(id);



ALTER TABLE ONLY public.transferencia_estoque
    ADD CONSTRAINT "FK7foakdt31eo19j7auupt64bqw" FOREIGN KEY (id_usuario) REFERENCES public.usuario(id);



ALTER TABLE ONLY public.ajuste_estoque
    ADD CONSTRAINT "FK87gdpjk4xr2txt5dl44ipo3xe" FOREIGN KEY (id_loja) REFERENCES public.loja(id);



ALTER TABLE ONLY public.estoque_produto
    ADD CONSTRAINT "FK88b5e7vi2fw3l7hy7knxmmgq7" FOREIGN KEY (id_loja) REFERENCES public.loja(id);



ALTER TABLE ONLY public.produto_loja
    ADD CONSTRAINT "FK8ijkol70l2acy5fh2myskodxg" FOREIGN KEY (id_loja) REFERENCES public.loja(id);



ALTER TABLE ONLY public.produto_preco_loja
    ADD CONSTRAINT "FK8n9fhcy4w7rebm099nt94h9aw" FOREIGN KEY (id_produto) REFERENCES public.produto(id);



ALTER TABLE ONLY public.item_devolucao_fornecedor
    ADD CONSTRAINT "FK8yhp3i04ddd124cdqqxf9cvd3" FOREIGN KEY (id_produto) REFERENCES public.produto(id);



ALTER TABLE ONLY public.item_devolucao_fornecedor
    ADD CONSTRAINT "FK9l61576inca8un17i8p6jgn5s" FOREIGN KEY (id_item_compra) REFERENCES public.item_compra(id);



ALTER TABLE ONLY public.devolucao_cliente
    ADD CONSTRAINT "FKanoeo0r8jixk3u09vv2vvufng" FOREIGN KEY (id_venda) REFERENCES public.venda(id);



ALTER TABLE ONLY public.devolucao_fornecedor
    ADD CONSTRAINT "FKau65f5nv0befnnm3eeb3hhtri" FOREIGN KEY (id_compra) REFERENCES public.compra(id);



ALTER TABLE ONLY public.produto
    ADD CONSTRAINT "FKb1ua2ls36fnxu5x8taqogl2yj" FOREIGN KEY (id_categoria) REFERENCES public.categoria(id);



ALTER TABLE ONLY public.venda
    ADD CONSTRAINT "FKbbuiko67p6yo2p85x7ddm0b53" FOREIGN KEY (id_cliente) REFERENCES public.cliente(id);



ALTER TABLE ONLY public.movimentacao_estoque
    ADD CONSTRAINT "FKbeihclae4biofdby48t952q04" FOREIGN KEY (id_produto) REFERENCES public.produto(id);



ALTER TABLE ONLY public.transferencia_estoque
    ADD CONSTRAINT "FKboywccgre1pc92f9hirwdivvb" FOREIGN KEY (id_loja_origem) REFERENCES public.loja(id);



ALTER TABLE ONLY public.movimentacao_estoque
    ADD CONSTRAINT "FKc1lh72c2laujajatu6q5hioh8" FOREIGN KEY (id_devolucao_cliente) REFERENCES public.devolucao_cliente(id);



ALTER TABLE ONLY public.movimentacao_estoque
    ADD CONSTRAINT "FKc2vmy6s6ur5iolkf8ntwrpwb9" FOREIGN KEY (id_item_ajuste_estoque) REFERENCES public.item_ajuste_estoque(id);



ALTER TABLE ONLY public.item_ajuste_estoque
    ADD CONSTRAINT "FKcq34melejkq5dxskh3jkjqeql" FOREIGN KEY (id_produto) REFERENCES public.produto(id);



ALTER TABLE ONLY public.item_transferencia
    ADD CONSTRAINT "FKddeduc5fxt7ck7txinvap7vpj" FOREIGN KEY (id_produto) REFERENCES public.produto(id);



ALTER TABLE ONLY public.estoque_produto
    ADD CONSTRAINT "FKdicqqqt32fxfmja8wtsr1qmqq" FOREIGN KEY (id_produto) REFERENCES public.produto(id);



ALTER TABLE ONLY public.produto_sku
    ADD CONSTRAINT "FKdtk76iuvck5mv6vwx1u5am6oa" FOREIGN KEY (id_produto) REFERENCES public.produto(id);



ALTER TABLE ONLY public.devolucao_cliente
    ADD CONSTRAINT "FKfc8dkcjs1uim0xrirpm8krti8" FOREIGN KEY (id_usuario) REFERENCES public.usuario(id);



ALTER TABLE ONLY public.venda
    ADD CONSTRAINT "FKfpcuvfc9htu7k5q4fo5pn3325" FOREIGN KEY (id_usuario) REFERENCES public.usuario(id);



ALTER TABLE ONLY public.venda
    ADD CONSTRAINT "FKg35fw7dc9xu0jrtdl664wdkq7" FOREIGN KEY (id_loja) REFERENCES public.loja(id);



ALTER TABLE ONLY public.movimentacao_estoque
    ADD CONSTRAINT "FKg6q7ycta2epk5je0863vyq4m0" FOREIGN KEY (id_compra) REFERENCES public.compra(id);



ALTER TABLE ONLY public.produto_loja
    ADD CONSTRAINT "FKgsnbrt10ors8jrw1onmm1s3d7" FOREIGN KEY (id_produto) REFERENCES public.produto(id);



ALTER TABLE ONLY public.movimentacao_estoque
    ADD CONSTRAINT "FKh4xvc753c2blxehv78e3q47at" FOREIGN KEY (id_item_devolucao_fornecedor) REFERENCES public.item_devolucao_fornecedor(id);



ALTER TABLE ONLY public.item_recebimento_compra
    ADD CONSTRAINT "FKhrqorhpn6f3jx266skvlos4j3" FOREIGN KEY (id_item_compra) REFERENCES public.item_compra(id);



ALTER TABLE ONLY public.devolucao_fornecedor
    ADD CONSTRAINT "FKimb1ih6t67skcxkb8p5xb3qdw" FOREIGN KEY (id_usuario) REFERENCES public.usuario(id);



ALTER TABLE ONLY public.movimentacao_estoque
    ADD CONSTRAINT "FKiud6wm3tgntjtvwusf8c08yq8" FOREIGN KEY (id_item_venda) REFERENCES public.produto_venda(id);



ALTER TABLE ONLY public.movimentacao_estoque
    ADD CONSTRAINT "FKjdjnmxk0k8fp0m697jr35bw9s" FOREIGN KEY (id_usuario) REFERENCES public.usuario(id);



ALTER TABLE ONLY public.item_devolucao_cliente
    ADD CONSTRAINT "FKjhk83prapls0m1ibuj8ok1ixx" FOREIGN KEY (id_item_venda) REFERENCES public.produto_venda(id);



ALTER TABLE ONLY public.recebimento_compra
    ADD CONSTRAINT "FKklfcgaj1d225tiq43td4dy45b" FOREIGN KEY (id_compra) REFERENCES public.compra(id);



ALTER TABLE ONLY public.produto_venda
    ADD CONSTRAINT "FKm3ikrrbuqg56r8bkgkrdkdr6v" FOREIGN KEY (id_venda) REFERENCES public.venda(id);



ALTER TABLE ONLY public.item_compra
    ADD CONSTRAINT "FKn8h42v7o298ektqwffetvefk1" FOREIGN KEY (id_produto) REFERENCES public.produto(id);



ALTER TABLE ONLY public.movimentacao_estoque
    ADD CONSTRAINT "FKnw7rdjcbbjxrvoyg8cw7byx1f" FOREIGN KEY (id_item_recebimento_compra) REFERENCES public.item_recebimento_compra(id);



ALTER TABLE ONLY public.item_devolucao_cliente
    ADD CONSTRAINT "FKnxxj1x0r7j2ta8csy2kmhn2fn" FOREIGN KEY (id_produto) REFERENCES public.produto(id);



ALTER TABLE ONLY public.compra
    ADD CONSTRAINT "FKokb9lb2ksxfv9nx8td1fw8biv" FOREIGN KEY (id_loja) REFERENCES public.loja(id);



ALTER TABLE ONLY public.movimentacao_estoque
    ADD CONSTRAINT "FKoma6e0c6bfcvyc6u6ls1r6ba1" FOREIGN KEY (id_transferencia) REFERENCES public.transferencia_estoque(id);



ALTER TABLE ONLY public.compra
    ADD CONSTRAINT "FKpkpd78s6p7x0puidhx28dr0gp" FOREIGN KEY (id_usuario) REFERENCES public.usuario(id);



ALTER TABLE ONLY public.movimentacao_estoque
    ADD CONSTRAINT "FKqfhgb2tvm615fvt6v23qs91pt" FOREIGN KEY (id_movimentacao_origem) REFERENCES public.movimentacao_estoque(id);



ALTER TABLE ONLY public.item_devolucao_cliente
    ADD CONSTRAINT "FKrayt0r66y5n58ci21ytg5k6" FOREIGN KEY (id_devolucao_cliente) REFERENCES public.devolucao_cliente(id);



ALTER TABLE ONLY public.transferencia_estoque
    ADD CONSTRAINT "FKrcd056e16k57wfjcrd16g283g" FOREIGN KEY (id_loja_destino) REFERENCES public.loja(id);



ALTER TABLE ONLY public.movimentacao_estoque
    ADD CONSTRAINT "FKrn5mh74b0avr0o7xvjw95cv0d" FOREIGN KEY (id_loja) REFERENCES public.loja(id);



ALTER TABLE ONLY public.ajuste_estoque
    ADD CONSTRAINT "FKs0joiduqu8lvkaecss9bu0tta" FOREIGN KEY (id_usuario) REFERENCES public.usuario(id);



ALTER TABLE ONLY public.item_transferencia
    ADD CONSTRAINT "FKs3noq3fcxajrenjbjqyvjpvky" FOREIGN KEY (id_transferencia) REFERENCES public.transferencia_estoque(id);



ALTER TABLE ONLY public.movimentacao_estoque
    ADD CONSTRAINT "FKs8wyd7pxetgfokg7bnbbhhcif" FOREIGN KEY (id_venda) REFERENCES public.venda(id);



ALTER TABLE ONLY public.movimentacao_estoque
    ADD CONSTRAINT "FKsbbq9mtxs2y1g6rlyv4xkv6uy" FOREIGN KEY (id_devolucao_fornecedor) REFERENCES public.devolucao_fornecedor(id);



ALTER TABLE ONLY public.item_compra
    ADD CONSTRAINT "FKsicwp6qada5p0jrkx2o326m3o" FOREIGN KEY (id_compra) REFERENCES public.compra(id);



ALTER TABLE ONLY public.produto
    ADD CONSTRAINT "FKsmn5njnobu8q64np3ofgjiaep" FOREIGN KEY (id_unidade_medida) REFERENCES public.unidade_medida(id);



ALTER TABLE ONLY public.produto_preco_loja
    ADD CONSTRAINT "FKtek126flaoo86kdsq5bt74j6h" FOREIGN KEY (id_loja) REFERENCES public.loja(id);



ALTER TABLE ONLY public.movimentacao_estoque
    ADD CONSTRAINT "FKtgsyjkhx2y9m9kfkcxisrjx68" FOREIGN KEY (id_item_devolucao_cliente) REFERENCES public.item_devolucao_cliente(id);



