-- ===========================================================================
-- FUNÇÃO DE ATUALIZAÇÃO DE TIMESTAMPS
-- ===========================================================================

-- Função global para atualização automática do timestamp 'atualizado_em'
CREATE OR REPLACE FUNCTION public.update_atualizado_em_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.atualizado_em = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ===========================================================================
-- 1. NÚCLEO E CONTROLE DE ACESSO (CORE & RBAC)
-- ===========================================================================

CREATE TABLE public.empresas (
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    nome_empresa text NOT NULL,
    cnpj text NOT NULL,
    logo_url text,
    logo_secundaria text,
    icone_sidebar text,
    cor_primaria text DEFAULT '#DC2626'::text,
    cor_secundaria text DEFAULT '#FFFFFF'::text,
    modulos_ativos jsonb DEFAULT '["/frota", "/monitoramento"]'::jsonb,
    ativo boolean DEFAULT true,
    criado_em timestamp with time zone DEFAULT now(),
    atualizado_em timestamp with time zone DEFAULT now(),
    CONSTRAINT empresas_pkey PRIMARY KEY (id),
    CONSTRAINT empresas_cnpj_key UNIQUE (cnpj)
);

CREATE TABLE public.departamentos (
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    empresa_id uuid NOT NULL,
    nome text NOT NULL,
    descricao text,
    rota_tela text,
    ativo boolean DEFAULT true,
    criado_em timestamp with time zone DEFAULT now(),
    atualizado_em timestamp with time zone DEFAULT now(),
    CONSTRAINT departamentos_pkey PRIMARY KEY (id),
    CONSTRAINT departamentos_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES public.empresas(id) ON DELETE CASCADE
);

CREATE TABLE public.perfis (
    id uuid NOT NULL, -- Vinculado diretamente ao ID de autenticação
    empresa_id uuid NOT NULL,
    nome_completo text NOT NULL,
    email text NOT NULL,
    cargo text,
    departamento_id uuid,
    role text NOT NULL CHECK (role = ANY (ARRAY['admin_sistema'::text, 'gestor'::text, 'funcionario'::text])),
    ativo boolean DEFAULT true,
    criado_em timestamp with time zone DEFAULT now(),
    atualizado_em timestamp with time zone DEFAULT now(),
    CONSTRAINT perfis_pkey PRIMARY KEY (id),
    CONSTRAINT perfis_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES public.empresas(id) ON DELETE CASCADE,
    CONSTRAINT fk_perfis_depto FOREIGN KEY (departamento_id) REFERENCES public.departamentos(id) ON DELETE SET NULL
);

-- ===========================================================================
-- 2. OPERAÇÃO DA FROTA E MANUTENÇÃO
-- ===========================================================================

CREATE TABLE public.veiculos (
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    empresa_id uuid NOT NULL,
    placa text NOT NULL,
    numero_frota text,
    categoria text,
    motorista_id uuid,
    status text DEFAULT 'Ativo'::text CHECK (status = ANY (ARRAY['Ativo'::text, 'Parado'::text, 'Manutenção'::text])),
    consumo_km_l numeric,
    vel_max_kmh numeric,
    combustivel_pct integer,
    combustivel_gasto_l numeric,
    km_rodados numeric,
    telemetria boolean DEFAULT false,
    meta_verde numeric,
    meta_amarelo numeric,
    meta_vermelho numeric,
    capacidade_tanque_litros numeric,
    litragem_cam boolean,
    combustivel_divisor numeric DEFAULT 1000,
    criado_em timestamp with time zone DEFAULT now(),
    atualizado_em timestamp with time zone DEFAULT now(),
    CONSTRAINT veiculos_pkey PRIMARY KEY (id),
    CONSTRAINT veiculos_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES public.empresas(id) ON DELETE CASCADE,
    CONSTRAINT veiculos_motorista_id_fkey FOREIGN KEY (motorista_id) REFERENCES public.perfis(id) ON DELETE SET NULL,
    CONSTRAINT veiculos_empresa_placa_key UNIQUE (empresa_id, placa)
);

CREATE TABLE public.oficina (
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    empresa_id uuid NOT NULL,
    veiculo_id uuid NOT NULL,
    descricao text NOT NULL,
    prioridade text DEFAULT 'Média'::text CHECK (prioridade = ANY (ARRAY['Alta'::text, 'Média'::text, 'Baixa'::text])),
    responsavel_id uuid,
    data_registro date DEFAULT CURRENT_DATE,
    status text DEFAULT 'Aberto'::text CHECK (status = ANY (ARRAY['Aberto'::text, 'Em andamento'::text, 'Concluído'::text])),
    criado_em timestamp with time zone DEFAULT now(),
    atualizado_em timestamp with time zone DEFAULT now(),
    CONSTRAINT oficina_pkey PRIMARY KEY (id),
    CONSTRAINT oficina_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES public.empresas(id) ON DELETE CASCADE,
    CONSTRAINT oficina_veiculo_id_fkey FOREIGN KEY (veiculo_id) REFERENCES public.veiculos(id) ON DELETE CASCADE,
    CONSTRAINT oficina_responsavel_id_fkey FOREIGN KEY (responsavel_id) REFERENCES public.perfis(id) ON DELETE SET NULL
);

-- ===========================================================================
-- 3. INGESTÃO DE DADOS BRUTOS (INGESTION BUFFERS)
-- ===========================================================================

CREATE TABLE public.payload (
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    source text DEFAULT 'rapid-processor'::text,
    payload jsonb NOT NULL,
    created_at timestamp with time zone NOT NULL DEFAULT timezone('utc'::text, now()),
    CONSTRAINT payload_pkey PRIMARY KEY (id)
);

-- ===========================================================================
-- 4. TELEMETRIA E RASTREAMENTO EM ALTO VOLUME
-- ===========================================================================

CREATE TABLE public.posicoes_veiculos (
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    empresa_id uuid NOT NULL,
    veiculo_id uuid NOT NULL,
    motorista_id uuid,
    evento text,
    gps_valido boolean,
    id_rastreamento bigint,
    ignicao boolean NOT NULL DEFAULT false,
    latitude numeric NOT NULL,
    longitude numeric NOT NULL,
    velocidade numeric NOT NULL DEFAULT 0,
    tensao_bateria numeric,
    hodometro numeric,
    placa_snapshot text,
    motorista_snapshot text,
    codigo_externo text,
    nome_area_segura text,
    data_evento timestamp with time zone NOT NULL,
    inserido_em timestamp with time zone DEFAULT now(),
    CONSTRAINT posicoes_veiculos_pkey PRIMARY KEY (id),
    CONSTRAINT posicoes_veiculos_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES public.empresas(id) ON DELETE CASCADE,
    CONSTRAINT posicoes_veiculos_veiculo_id_fkey FOREIGN KEY (veiculo_id) REFERENCES public.veiculos(id) ON DELETE CASCADE,
    CONSTRAINT posicoes_veiculos_motorista_id_fkey FOREIGN KEY (motorista_id) REFERENCES public.perfis(id) ON DELETE SET NULL
);

CREATE TABLE public.telemetria (
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    payload_id uuid,
    empresa_id uuid NOT NULL,
    veiculo_id uuid NOT NULL,
    motorista_id uuid,
    id_rastreamento bigint,
    evento text,
    latitude numeric,
    longitude numeric,
    velocidade numeric,
    gps_valido boolean,
    ignicao boolean NOT NULL DEFAULT false,
    rpm numeric,
    tensao_bateria numeric,
    nivel_combustivel numeric,
    hodometro numeric,
    horimetro numeric,
    status_freio boolean,
    evento_frenagem boolean,
    evento_aceleracao boolean,
    total_combustivel numeric,
    temperatura_oleo numeric,
    posicao_pedal_acelerador numeric,
    posicao_pedal_freio numeric,
    loaded boolean,
    extra jsonb,
    placa_snapshot text,
    motorista_snapshot text,
    data_evento timestamp with time zone NOT NULL,
    criado_em timestamp with time zone NOT NULL DEFAULT timezone('utc'::text, now()),
    CONSTRAINT telemetria_pkey PRIMARY KEY (id),
    CONSTRAINT telemetria_payload_id_fkey FOREIGN KEY (payload_id) REFERENCES public.payload(id) ON DELETE SET NULL,
    CONSTRAINT telemetria_empresa_id_fkey FOREIGN KEY (empresa_id) REFERENCES public.empresas(id) ON DELETE CASCADE,
    CONSTRAINT telemetria_veiculo_id_fkey FOREIGN KEY (veiculo_id) REFERENCES public.veiculos(id) ON DELETE CASCADE,
    CONSTRAINT telemetria_motorista_id_fkey FOREIGN KEY (motorista_id) REFERENCES public.perfis(id) ON DELETE SET NULL
);

-- ===========================================================================
-- 5. INTELIGÊNCIA E EVENTOS DE VELOCIDADE
-- ===========================================================================

CREATE TABLE public.excesso_velocidade (
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    empresa_id uuid NOT NULL,
    veiculo_id uuid NOT NULL,
    motorista_id uuid,
    data_ocorrencia date NOT NULL,
    inicio_excesso_dia timestamp with time zone NOT NULL,
    fim_excesso_dia timestamp with time zone NOT NULL,
    velocidade_maxima_atingida numeric NOT NULL,
    limite_velocidade_configurado numeric,
    quantidade_de_excessos integer NOT NULL DEFAULT 1,
    lat_excesso_maximo numeric,
    lon_excesso_maximo numeric,
    placa_snapshot text,
    frota_snapshot text,
    categoria_snapshot text,
    motorista_nome_snapshot text,
    duracao_total_minutos numeric GENERATED ALWAYS AS (EXTRACT(epoch FROM (fim_excesso_dia - inicio_excesso_dia)) / 60) STORED,
    criado_em timestamp with time zone NOT NULL DEFAULT now(),
    atualizado_em timestamp with time zone NOT NULL DEFAULT now(),
    CONSTRAINT excesso_velocidade_pkey PRIMARY KEY (id),
    CONSTRAINT excesso_velocidade_empresa_fkey FOREIGN KEY (empresa_id) REFERENCES public.empresas(id) ON DELETE CASCADE,
    CONSTRAINT excesso_velocidade_veiculo_fkey FOREIGN KEY (veiculo_id) REFERENCES public.veiculos(id) ON DELETE CASCADE,
    CONSTRAINT excesso_velocidade_motorista_fkey FOREIGN KEY (motorista_id) REFERENCES public.perfis(id) ON DELETE SET NULL,
    CONSTRAINT excesso_velocidade_veiculo_inicio_key UNIQUE (veiculo_id, inicio_excesso_dia)
);

-- ===========================================================================
-- 6. ÍNDICES DE PERFORMANCE
-- ===========================================================================

CREATE INDEX idx_posicoes_veiculos_busca ON public.posicoes_veiculos (veiculo_id, data_evento DESC);
CREATE INDEX idx_telemetria_busca ON public.telemetria (veiculo_id, data_evento DESC);
CREATE INDEX idx_posicoes_veiculos_empresa ON public.posicoes_veiculos (empresa_id);
CREATE INDEX idx_telemetria_empresa ON public.telemetria (empresa_id);
CREATE INDEX idx_excesso_velocidade_empresa ON public.excesso_velocidade (empresa_id);

-- ===========================================================================
-- 7. TRIGGERS DE ATUALIZAÇÃO
-- ===========================================================================

CREATE TRIGGER trigger_update_empresas BEFORE UPDATE ON public.empresas FOR EACH ROW EXECUTE FUNCTION public.update_atualizado_em_column();
CREATE TRIGGER trigger_update_departamentos BEFORE UPDATE ON public.departamentos FOR EACH ROW EXECUTE FUNCTION public.update_atualizado_em_column();
CREATE TRIGGER trigger_update_perfis BEFORE UPDATE ON public.perfis FOR EACH ROW EXECUTE FUNCTION public.update_atualizado_em_column();
CREATE TRIGGER trigger_update_veiculos BEFORE UPDATE ON public.veiculos FOR EACH ROW EXECUTE FUNCTION public.update_atualizado_em_column();
CREATE TRIGGER trigger_update_oficina BEFORE UPDATE ON public.oficina FOR EACH ROW EXECUTE FUNCTION public.update_atualizado_em_column();
CREATE TRIGGER trigger_update_excesso_velocidade BEFORE UPDATE ON public.excesso_velocidade FOR EACH ROW EXECUTE FUNCTION public.update_atualizado_em_column();