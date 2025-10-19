-- ===========================================
-- SCRIPT DE CRIAÇÃO DO BANCO STM-ADV
-- ===========================================

-- Limpa tabelas existentes
DROP TABLE IF EXISTS documentos CASCADE;
DROP TABLE IF EXISTS anotacoes CASCADE;
DROP TABLE IF EXISTS movimentacoes CASCADE;
DROP TABLE IF EXISTS eventos CASCADE;
DROP TABLE IF EXISTS processos CASCADE;
DROP TABLE IF EXISTS clientes CASCADE;
DROP TABLE IF EXISTS auditoria CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;

-- ===========================================
-- TABELA DE USUÁRIOS
-- ===========================================
CREATE TABLE usuarios (
                          id BIGSERIAL PRIMARY KEY,
                          email VARCHAR(255) UNIQUE NOT NULL,
                          senha VARCHAR(255) NOT NULL,
                          nome VARCHAR(255) NOT NULL,
                          perfil VARCHAR(20) NOT NULL CHECK (perfil IN ('MASTER', 'ADVOGADO', 'CLIENTE')),

    -- Informações profissionais (ADVOGADO)
                          oab VARCHAR(50),
                          especialidades TEXT,

    -- Informações de contato
                          telefone VARCHAR(20),
                          endereco TEXT,
                          cidade VARCHAR(100),
                          estado VARCHAR(2),
                          cep VARCHAR(10),

    -- Controle de status
                          ativo BOOLEAN DEFAULT TRUE,
                          aprovado BOOLEAN DEFAULT FALSE,

    -- Auditoria
                          data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          ultimo_acesso TIMESTAMP
);

-- ===========================================
-- TABELA DE CLIENTES
-- ===========================================
CREATE TABLE clientes (
                          id BIGSERIAL PRIMARY KEY,
                          nome VARCHAR(255) NOT NULL,
                          email VARCHAR(255),
                          telefone VARCHAR(20),
                          cpf_cnpj VARCHAR(20),
                          tipo VARCHAR(10) CHECK (tipo IN ('FISICA', 'JURIDICA')),
                          endereco TEXT,
                          cidade VARCHAR(100),
                          estado VARCHAR(2),
                          cep VARCHAR(10),

    -- Relacionamento
                          usuario_id BIGINT REFERENCES usuarios(id) ON DELETE CASCADE,
                          advogado_responsavel_id BIGINT REFERENCES usuarios(id),

    -- Status
                          status VARCHAR(20) DEFAULT 'ATIVO' CHECK (status IN ('ATIVO', 'INATIVO', 'ARQUIVADO')),

    -- Auditoria
                          data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===========================================
-- TABELA DE PROCESSOS
-- ===========================================
CREATE TABLE processos (
                           id BIGSERIAL PRIMARY KEY,
                           numero_processo VARCHAR(50) UNIQUE NOT NULL,
                           descricao TEXT NOT NULL,
                           tipo VARCHAR(50), -- Trabalhista, Civil, Criminal, etc
                           area VARCHAR(50), -- Área do direito

    -- Status do processo
                           status VARCHAR(30) DEFAULT 'EM_ANDAMENTO' CHECK (status IN ('EM_ANDAMENTO', 'CONCLUIDO', 'ARQUIVADO', 'SUSPENSO')),
                           prioridade VARCHAR(20) DEFAULT 'MEDIA' CHECK (prioridade IN ('BAIXA', 'MEDIA', 'ALTA', 'URGENTE')),

    -- Dados do tribunal
                           tribunal VARCHAR(100),
                           vara VARCHAR(100),
                           comarca VARCHAR(100),

    -- Relacionamentos
                           cliente_id BIGINT REFERENCES clientes(id) ON DELETE CASCADE,
                           advogado_responsavel_id BIGINT REFERENCES usuarios(id),

    -- Datas importantes
                           data_abertura DATE,
                           data_distribuicao DATE,
                           data_conclusao DATE,
                           prazo_final DATE,

    -- Valores
                           valor_causa DECIMAL(15,2),
                           valor_condenacao DECIMAL(15,2),

    -- Observações
                           observacoes TEXT,

    -- Auditoria
                           data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===========================================
-- TABELA DE EVENTOS/AGENDA
-- ===========================================
CREATE TABLE eventos (
                         id BIGSERIAL PRIMARY KEY,
                         titulo VARCHAR(255) NOT NULL,
                         descricao TEXT,
                         tipo VARCHAR(30) CHECK (tipo IN ('AUDIENCIA', 'REUNIAO', 'PRAZO', 'CONSULTA', 'OUTRO')),

    -- Datas
                         data_inicio TIMESTAMP NOT NULL,
                         data_fim TIMESTAMP NOT NULL,
                         dia_inteiro BOOLEAN DEFAULT FALSE,

    -- Relacionamentos
                         processo_id BIGINT REFERENCES processos(id) ON DELETE CASCADE,
                         usuario_id BIGINT REFERENCES usuarios(id) ON DELETE CASCADE,
                         cliente_id BIGINT REFERENCES clientes(id),

    -- Localização
                         local VARCHAR(255),
                         endereco TEXT,

    -- Configurações
                         cor VARCHAR(20) DEFAULT '#007bff',
                         lembrete_minutos INTEGER DEFAULT 30,
                         notificado BOOLEAN DEFAULT FALSE,

    -- Status
                         status VARCHAR(20) DEFAULT 'AGENDADO' CHECK (status IN ('AGENDADO', 'REALIZADO', 'CANCELADO', 'REMARCADO')),

    -- Auditoria
                         data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===========================================
-- TABELA DE MOVIMENTAÇÕES DO PROCESSO
-- ===========================================
CREATE TABLE movimentacoes (
                               id BIGSERIAL PRIMARY KEY,
                               processo_id BIGINT NOT NULL REFERENCES processos(id) ON DELETE CASCADE,
                               tipo VARCHAR(50) NOT NULL,
                               descricao TEXT NOT NULL,
                               data_movimentacao TIMESTAMP NOT NULL,
                               usuario_id BIGINT REFERENCES usuarios(id),

    -- Auditoria
                               data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===========================================
-- TABELA DE ANOTAÇÕES
-- ===========================================
CREATE TABLE anotacoes (
                           id BIGSERIAL PRIMARY KEY,
                           titulo VARCHAR(255) NOT NULL,
                           conteudo TEXT NOT NULL,

    -- Relacionamentos
                           processo_id BIGINT REFERENCES processos(id) ON DELETE CASCADE,
                           usuario_id BIGINT REFERENCES usuarios(id),

    -- Configurações
                           privada BOOLEAN DEFAULT FALSE,
                           importante BOOLEAN DEFAULT FALSE,

    -- Auditoria
                           data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===========================================
-- TABELA DE DOCUMENTOS
-- ===========================================
CREATE TABLE documentos (
                            id BIGSERIAL PRIMARY KEY,
                            nome VARCHAR(255) NOT NULL,
                            descricao TEXT,
                            tipo VARCHAR(50), -- PDF, DOCX, JPG, etc
                            tamanho BIGINT, -- em bytes

    -- Armazenamento (caminho ou conteúdo)
                            caminho TEXT,
                            conteudo_base64 TEXT, -- Para armazenar no banco se necessário

    -- Relacionamentos
                            processo_id BIGINT REFERENCES processos(id) ON DELETE CASCADE,
                            usuario_id BIGINT REFERENCES usuarios(id),

    -- Categorização
                            categoria VARCHAR(50), -- Petição, Contrato, Sentença, etc

    -- Auditoria
                            data_upload TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===========================================
-- TABELA DE AUDITORIA
-- ===========================================
CREATE TABLE auditoria (
                           id BIGSERIAL PRIMARY KEY,
                           usuario_id BIGINT REFERENCES usuarios(id),
                           usuario_nome VARCHAR(255),
                           acao VARCHAR(50) NOT NULL,
                           entidade VARCHAR(50), -- usuarios, processos, clientes, etc
                           entidade_id BIGINT,
                           descricao TEXT,
                           ip_address VARCHAR(45),
                           user_agent TEXT,

    -- Dados adicionais
                           dados_antes TEXT, -- JSON com dados antes da alteração
                           dados_depois TEXT, -- JSON com dados depois da alteração

                           data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ===========================================
-- ÍNDICES PARA PERFORMANCE
-- ===========================================

-- Usuários
CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_perfil ON usuarios(perfil);
CREATE INDEX idx_usuarios_ativo ON usuarios(ativo);

-- Clientes
CREATE INDEX idx_clientes_usuario ON clientes(usuario_id);
CREATE INDEX idx_clientes_advogado ON clientes(advogado_responsavel_id);
CREATE INDEX idx_clientes_status ON clientes(status);

-- Processos
CREATE INDEX idx_processos_numero ON processos(numero_processo);
CREATE INDEX idx_processos_cliente ON processos(cliente_id);
CREATE INDEX idx_processos_advogado ON processos(advogado_responsavel_id);
CREATE INDEX idx_processos_status ON processos(status);
CREATE INDEX idx_processos_prazo ON processos(prazo_final);

-- Eventos
CREATE INDEX idx_eventos_usuario ON eventos(usuario_id);
CREATE INDEX idx_eventos_processo ON eventos(processo_id);
CREATE INDEX idx_eventos_data ON eventos(data_inicio);
CREATE INDEX idx_eventos_status ON eventos(status);

-- Documentos
CREATE INDEX idx_documentos_processo ON documentos(processo_id);

-- Auditoria
CREATE INDEX idx_auditoria_usuario ON auditoria(usuario_id);
CREATE INDEX idx_auditoria_data ON auditoria(data_hora);
CREATE INDEX idx_auditoria_acao ON auditoria(acao);

-- ===========================================
-- DADOS INICIAIS
-- ===========================================

-- Usuário Master (senha: master123)
INSERT INTO usuarios (email, senha, nome, perfil, ativo, aprovado) VALUES
    ('admin@stm-adv.com', 'master123', 'Administrador do Sistema', 'MASTER', true, true);

-- Advogado de teste (senha: adv123)
INSERT INTO usuarios (email, senha, nome, perfil, oab, especialidades, telefone, cidade, estado, ativo, aprovado) VALUES
    ('arthur.silva@stm-adv.com', 'adv123', 'Dr. Arthur Silva', 'ADVOGADO', '123456/DF', 'Direito Trabalhista, Direito Civil', '(61) 99999-0000', 'Brasília', 'DF', true, true);

-- Cliente de teste (senha: cliente123)
INSERT INTO usuarios (email, senha, nome, perfil, telefone, cidade, estado, ativo, aprovado) VALUES
    ('joao.silva@email.com', 'cliente123', 'João Silva', 'CLIENTE', '(61) 98888-1111', 'Brasília', 'DF', true, true);

-- Cliente vinculado ao advogado
INSERT INTO clientes (nome, email, telefone, cpf_cnpj, tipo, usuario_id, advogado_responsavel_id, status, cidade, estado) VALUES
                                                                                                                              ('João Silva', 'joao.silva@email.com', '(61) 98888-1111', '123.456.789-00', 'FISICA', 3, 2, 'ATIVO', 'Brasília', 'DF'),
                                                                                                                              ('Maria Santos', 'maria.santos@email.com', '(61) 97777-2222', '987.654.321-00', 'FISICA', NULL, 2, 'ATIVO', 'Brasília', 'DF');

-- Processos de exemplo
INSERT INTO processos (numero_processo, descricao, tipo, area, status, tribunal, vara, cliente_id, advogado_responsavel_id, data_abertura, data_distribuicao, valor_causa, prioridade) VALUES
                                                                                                                                                                                           ('0001234-56.2025.8.07.0001', 'Reclamação trabalhista - Horas extras não pagas e adicional noturno', 'Trabalhista', 'Direito do Trabalho', 'EM_ANDAMENTO', 'TRT-7', '2ª Vara do Trabalho', 1, 2, '2025-01-15', '2025-01-20', 50000.00, 'ALTA'),
                                                                                                                                                                                           ('0005678-90.2025.8.07.0002', 'Ação de divórcio consensual com partilha de bens', 'Civil', 'Direito de Família', 'EM_ANDAMENTO', 'TJDFT', '1ª Vara de Família', 2, 2, '2025-02-01', '2025-02-05', 0.00, 'MEDIA');

-- Eventos/Agenda
INSERT INTO eventos (titulo, descricao, tipo, data_inicio, data_fim, processo_id, usuario_id, local, cor, status) VALUES
                                                                                                                      ('Audiência Trabalhista - João Silva', 'Audiência inicial de conciliação', 'AUDIENCIA', '2025-10-25 14:00:00', '2025-10-25 16:00:00', 1, 2, 'TRT-7 - Sala 3', '#28a745', 'AGENDADO'),
                                                                                                                      ('Reunião com cliente Maria Santos', 'Discussão sobre documentos do divórcio', 'REUNIAO', '2025-10-22 10:00:00', '2025-10-22 11:00:00', 2, 2, 'Escritório STM-ADV', '#007bff', 'AGENDADO');

-- Movimentações
INSERT INTO movimentacoes (processo_id, tipo, descricao, data_movimentacao, usuario_id) VALUES
                                                                                            (1, 'PETIÇÃO_INICIAL', 'Petição inicial protocolada com sucesso', '2025-01-15 10:30:00', 2),
                                                                                            (1, 'DISTRIBUIÇÃO', 'Processo distribuído para a 2ª Vara do Trabalho', '2025-01-20 14:15:00', 2);

-- Anotações
INSERT INTO anotacoes (titulo, conteudo, processo_id, usuario_id, importante) VALUES
                                                                                  ('Primeira Audiência', 'Cliente deve levar todos os comprovantes de pagamento e holerites dos últimos 12 meses. Confirmar presença até 2 dias antes.', 1, 2, true),
                                                                                  ('Documentos Pendentes', 'Aguardando envio da certidão de casamento atualizada.', 2, 2, false);

-- Auditoria inicial
INSERT INTO auditoria (usuario_id, usuario_nome, acao, entidade, descricao) VALUES
                                                                                (1, 'Administrador do Sistema', 'LOGIN', 'usuarios', 'Primeiro acesso ao sistema'),
                                                                                (2, 'Dr. Arthur Silva', 'LOGIN', 'usuarios', 'Login realizado com sucesso'),
                                                                                (2, 'Dr. Arthur Silva', 'CRIAR', 'processos', 'Criou processo 0001234-56.2025.8.07.0001');

-- ===========================================
-- TRIGGERS PARA AUDITORIA AUTOMÁTICA
-- ===========================================

-- Função para atualizar data_atualizacao
CREATE OR REPLACE FUNCTION atualizar_data_atualizacao()
RETURNS TRIGGER AS $$
BEGIN
    NEW.data_atualizacao = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers para atualização automática
CREATE TRIGGER trigger_usuarios_atualizacao
    BEFORE UPDATE ON usuarios
    FOR EACH ROW
    EXECUTE FUNCTION atualizar_data_atualizacao();

CREATE TRIGGER trigger_clientes_atualizacao
    BEFORE UPDATE ON clientes
    FOR EACH ROW
    EXECUTE FUNCTION atualizar_data_atualizacao();

CREATE TRIGGER trigger_processos_atualizacao
    BEFORE UPDATE ON processos
    FOR EACH ROW
    EXECUTE FUNCTION atualizar_data_atualizacao();

CREATE TRIGGER trigger_eventos_atualizacao
    BEFORE UPDATE ON eventos
    FOR EACH ROW
    EXECUTE FUNCTION atualizar_data_atualizacao();

-- ===========================================
-- VIEWS ÚTEIS
-- ===========================================

-- View de processos com informações completas
CREATE OR REPLACE VIEW vw_processos_completos AS
SELECT
    p.*,
    c.nome AS cliente_nome,
    c.email AS cliente_email,
    c.telefone AS cliente_telefone,
    u.nome AS advogado_nome,
    u.email AS advogado_email,
    (SELECT COUNT(*) FROM documentos WHERE processo_id = p.id) AS total_documentos,
    (SELECT COUNT(*) FROM anotacoes WHERE processo_id = p.id) AS total_anotacoes,
    (SELECT COUNT(*) FROM eventos WHERE processo_id = p.id AND status = 'AGENDADO') AS eventos_pendentes
FROM processos p
         LEFT JOIN clientes c ON p.cliente_id = c.id
         LEFT JOIN usuarios u ON p.advogado_responsavel_id = u.id;

-- View de estatísticas por advogado
CREATE OR REPLACE VIEW vw_estatisticas_advogado AS
SELECT
    u.id AS advogado_id,
    u.nome AS advogado_nome,
    COUNT(DISTINCT p.id) AS total_processos,
    COUNT(DISTINCT CASE WHEN p.status = 'EM_ANDAMENTO' THEN p.id END) AS processos_ativos,
    COUNT(DISTINCT c.id) AS total_clientes,
    COUNT(DISTINCT CASE WHEN e.status = 'AGENDADO' AND e.data_inicio >= CURRENT_TIMESTAMP THEN e.id END) AS eventos_futuros
FROM usuarios u
         LEFT JOIN processos p ON u.id = p.advogado_responsavel_id
         LEFT JOIN clientes c ON u.id = c.advogado_responsavel_id
         LEFT JOIN eventos e ON u.id = e.usuario_id
WHERE u.perfil = 'ADVOGADO'
GROUP BY u.id, u.nome;

COMMIT;