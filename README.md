# ecossistema-sgc-backend

**SGC -- Sistema de Gestao Consular (Backend API)**

Parte do [Ecossistema Digital da Embaixada de Angola na Alemanha](https://github.com/embaixada-angola-alemanha/ecossistema-project).

API REST que suporta todos os servicos consulares: gestao de cidadaos, processamento de vistos, agendamentos, registo civil, servicos notariais, gestao documental, notificacoes e relatorios.

---

## Stack Tecnologica

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.4.3 |
| Base de Dados | PostgreSQL + Flyway migrations |
| ORM | Spring Data JPA / Hibernate |
| Seguranca | OAuth2 Resource Server + Keycloak RBAC (6 roles) |
| Mensageria | RabbitMQ (Spring AMQP) |
| Armazenamento | MinIO (S3-compatible) |
| Mapeamento | MapStruct 1.5.5 |
| Documentacao API | SpringDoc OpenAPI (Swagger UI) |
| Geracao PDF | OpenPDF 2.0.3 |
| Rate Limiting | Bucket4j |
| Testes | JUnit 5, Testcontainers, Spring Security Test, Awaitility |
| Cobertura | JaCoCo (minimo 70%) |
| Container | Docker (Eclipse Temurin 21 JRE Alpine) |

---

## Estrutura do Projecto

```
src/main/java/ao/gov/embaixada/sgc/
  SgcApplication.java              # Ponto de entrada
  config/                           # Configuracao da aplicacao
    AsyncConfig.java                #   Execucao assincrona
    InputSanitizer.java             #   Sanitizacao de input
    OpenApiConfig.java              #   Configuracao Swagger/OpenAPI
    RabbitMQConfig.java             #   Filas e exchanges RabbitMQ
    RateLimitFilter.java            #   Rate limiting por IP
    RateLimitProperties.java        #   Propriedades de rate limit
    SanitizingRequestFilter.java    #   Filtro de sanitizacao HTTP
  controller/                       # REST Controllers
    AgendamentoController.java      #   Agendamentos consulares
    CidadaoController.java          #   CRUD de cidadaos
    DocumentoController.java        #   Gestao de documentos
    NotificacaoController.java      #   Notificacoes
    ProcessoController.java         #   Processos consulares
    RegistoCivilController.java     #   Registo civil (nascimento, casamento, obito)
    RelatorioController.java        #   Relatorios PDF/CSV
    ServicoNotarialController.java  #   Servicos notariais
    VisaController.java             #   Processamento de vistos
  dto/                              # Data Transfer Objects (42 classes)
  entity/                           # Entidades JPA (15 entidades)
  enums/                            # Enumeracoes (17 enums)
    EstadoVisto.java                #   Estados do visto (state machine)
    TipoVisto.java                  #   Tipos de visto
    TipoRegistoCivil.java           #   Nascimento, casamento, obito
    TipoServicoNotarial.java        #   Procuracao, legalizacao, apostila, copia certificada
    ...
  exception/                        # Tratamento global de excepcoes
  integration/                      # Integracao inter-servicos
    SgcEventPublisher.java          #   Publicacao de eventos via RabbitMQ
  mapper/                           # MapStruct mappers
  repository/                       # Spring Data repositories
  service/                          # Logica de negocio (22 servicos)
    AgendamentoService.java         #   Agendamentos (slots, conflitos)
    AgendamentoSlotConfig.java      #   Configuracao de slots horarios
    CertificadoService.java         #   Geracao de certificados PDF
    CidadaoService.java             #   Gestao de cidadaos
    CidadaoLookupService.java       #   Pesquisa avancada de cidadaos
    CitizenContextService.java      #   Contexto do cidadao autenticado
    CsvExportService.java           #   Exportacao CSV
    DocumentoService.java           #   Upload/download de documentos (MinIO)
    JpaAuditService.java            #   Trilha de auditoria
    NotarialFeeCalculator.java      #   Calculo de taxas notariais
    NotificationConsumer.java       #   Consumidor de notificacoes RabbitMQ
    NotificationLogService.java     #   Log de notificacoes enviadas
    NotificationPreferenceService.java  # Preferencias de notificacao
    NotificationTemplateResolver.java   # Templates de email
    ProcessoService.java            #   Processos consulares
    RegistoCivilService.java        #   Registo civil
    RelatorioPdfService.java        #   Geracao de relatorios PDF
    RelatorioService.java           #   Dashboard e estatisticas
    ServicoNotarialService.java     #   Servicos notariais
    VisaDocumentChecklistService.java   # Checklist de documentos por tipo de visto
    VisaFeeCalculator.java          #   Calculo de taxas de visto
    VisaService.java                #   Processamento de vistos
  specification/                    # JPA Specifications (filtros dinamicos)
    CidadaoSpecification.java       #   Filtros avancados de cidadaos
  statemachine/                     # Maquinas de estado (workflow)
    WorkflowStateMachine.java       #   Base generica
    VisaStateMachine.java           #   Workflow de vistos
    AgendamentoStateMachine.java    #   Workflow de agendamentos
    ProcessoStateMachine.java       #   Workflow de processos
    RegistoCivilStateMachine.java   #   Workflow de registo civil
    ServicoNotarialStateMachine.java    # Workflow de servicos notariais
    event/                          #   Eventos de transicao

src/main/resources/
  application.yml                   # Configuracao principal (porta 8081)
  application-test.yml              # Perfil de testes
  application-staging.yml           # Perfil de staging
  application-production.yml        # Perfil de producao
  db/migration/                     # Flyway migrations
    V1__create_sgc_tables.sql       #   Tabelas base (cidadaos, processos)
    V2__create_visa_tables.sql      #   Tabelas de vistos
    V3__create_agendamento_tables.sql   # Tabelas de agendamentos
    V4__add_documento_versioning.sql    # Versionamento de documentos
    V5__create_registo_civil_tables.sql # Tabelas de registo civil
    V6__create_servico_notarial_tables.sql  # Tabelas de servicos notariais
    V7__create_notification_tables.sql  # Tabelas de notificacoes
    V8__create_audit_events_table.sql   # Tabela de auditoria
    V9__add_keycloak_id_to_cidadaos.sql # Keycloak ID nos cidadaos

src/test/java/ao/gov/embaixada/sgc/    # 53 ficheiros de teste
  controller/                       # Testes de API (unit + integration + authorization)
  service/                          # Testes de servicos
  repository/                       # Testes de repositorios (Testcontainers)
  integration/                      # Testes end-to-end
  statemachine/                     # Testes de maquinas de estado
  config/                           # Testes de configuracao
```

---

## Endpoints da API

### Cidadaos
| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | `/api/v1/cidadaos` | Listar cidadaos (paginacao + filtros) |
| GET | `/api/v1/cidadaos/{id}` | Detalhe do cidadao |
| POST | `/api/v1/cidadaos` | Registar novo cidadao |
| PUT | `/api/v1/cidadaos/{id}` | Actualizar cidadao |
| DELETE | `/api/v1/cidadaos/{id}` | Remover cidadao |

### Vistos
| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | `/api/v1/visas` | Listar pedidos de visto |
| POST | `/api/v1/visas` | Criar pedido de visto |
| PUT | `/api/v1/visas/{id}` | Actualizar pedido |
| POST | `/api/v1/visas/{id}/transition` | Transicao de estado (state machine) |
| GET | `/api/v1/visas/{id}/checklist` | Checklist de documentos |
| GET | `/api/v1/visas/{id}/fee` | Calculo de taxa |

### Agendamentos
| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | `/api/v1/agendamentos` | Listar agendamentos |
| POST | `/api/v1/agendamentos` | Criar agendamento |
| PUT | `/api/v1/agendamentos/{id}` | Actualizar agendamento |
| GET | `/api/v1/agendamentos/slots` | Slots disponiveis |

### Registo Civil
| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | `/api/v1/registos-civis` | Listar registos |
| POST | `/api/v1/registos-civis` | Criar registo (nascimento/casamento/obito) |
| GET | `/api/v1/registos-civis/{id}/certificado` | Download do certificado PDF |

### Servicos Notariais
| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | `/api/v1/servicos-notariais` | Listar servicos |
| POST | `/api/v1/servicos-notariais` | Criar servico (procuracao/legalizacao/apostila/copia) |
| GET | `/api/v1/servicos-notariais/{id}/fee` | Calculo de taxa |

### Documentos
| Metodo | Endpoint | Descricao |
|---|---|---|
| POST | `/api/v1/documentos/upload` | Upload de documento (MinIO) |
| GET | `/api/v1/documentos/{id}/download` | Download de documento |
| GET | `/api/v1/documentos` | Listar documentos |

### Relatorios
| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | `/api/v1/relatorios/dashboard` | Resumo do dashboard |
| GET | `/api/v1/relatorios/estatisticas` | Estatisticas gerais |
| GET | `/api/v1/relatorios/pdf` | Exportar relatorio PDF |
| GET | `/api/v1/relatorios/csv` | Exportar relatorio CSV |

### Notificacoes
| Metodo | Endpoint | Descricao |
|---|---|---|
| GET | `/api/v1/notificacoes` | Listar notificacoes |
| GET | `/api/v1/notificacoes/preferences` | Preferencias de notificacao |
| PUT | `/api/v1/notificacoes/preferences` | Actualizar preferencias |

**Swagger UI:** http://localhost:8081/swagger-ui.html

---

## Seguranca (Keycloak RBAC)

A API utiliza Keycloak como servidor de autenticacao/autorizacao com 6 roles:

| Role | Descricao |
|---|---|
| `ADMIN` | Acesso total ao sistema |
| `CONSUL` | Gestao consular completa |
| `OFFICER` | Funcionario consular |
| `REGISTRAR` | Oficial de registo civil |
| `NOTARY` | Oficial notarial |
| `CITIZEN` | Cidadao (self-service limitado) |

---

## Como Executar

### Pre-requisitos

- Java 21+
- Maven 3.9+
- PostgreSQL 16
- Keycloak 26 (realm `ecossistema`)
- MinIO (armazenamento de documentos)
- RabbitMQ 3.13 (mensageria)

### Desenvolvimento Local

```bash
# Clonar o repositorio
git clone https://github.com/embaixada-angola-alemanha/ecossistema-sgc-backend.git
cd ecossistema-sgc-backend

# Compilar e executar
./mvnw spring-boot:run

# A API fica disponivel em http://localhost:8081
# Swagger UI em http://localhost:8081/swagger-ui.html
```

### Testes

```bash
# Executar todos os testes (53 ficheiros, Testcontainers para PostgreSQL/MinIO/RabbitMQ)
./mvnw verify

# Apenas testes unitarios
./mvnw test

# Relatorio de cobertura (JaCoCo)
./mvnw verify
# Relatorio gerado em target/site/jacoco/index.html
```

### Build para Producao

```bash
# Gerar JAR
./mvnw clean package -DskipTests

# Docker
docker build -t ecossistema-sgc-backend .
docker run -p 8081:8081 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e SPRING_DATASOURCE_PASSWORD=<password> \
  -e RABBITMQ_PASSWORD=<password> \
  -e MINIO_ACCESS_KEY=<key> \
  -e MINIO_SECRET_KEY=<secret> \
  ecossistema-sgc-backend
```

---

## Configuracao de Ambiente

### Variaveis de Ambiente (Producao)

| Variavel | Descricao | Default |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Perfil activo | `default` |
| `SPRING_DATASOURCE_URL` | URL do PostgreSQL | `jdbc:postgresql://postgres:5432/sgc_db` |
| `SPRING_DATASOURCE_USERNAME` | Utilizador BD | `ecossistema` |
| `SPRING_DATASOURCE_PASSWORD` | Password BD | -- (obrigatorio) |
| `RABBITMQ_HOST` | Host do RabbitMQ | `rabbitmq` |
| `RABBITMQ_PORT` | Porta do RabbitMQ | `5672` |
| `RABBITMQ_USERNAME` | Utilizador RabbitMQ | `ecossistema` |
| `RABBITMQ_PASSWORD` | Password RabbitMQ | -- (obrigatorio) |
| `MINIO_ENDPOINT` | Endpoint do MinIO | `http://minio:9000` |
| `MINIO_ACCESS_KEY` | Chave de acesso MinIO | -- (obrigatorio) |
| `MINIO_SECRET_KEY` | Chave secreta MinIO | -- (obrigatorio) |
| `MAIL_HOST` | Servidor SMTP | -- (obrigatorio) |
| `MAIL_PORT` | Porta SMTP | `587` |
| `MAIL_USERNAME` | Utilizador SMTP | -- (obrigatorio) |
| `MAIL_PASSWORD` | Password SMTP | -- (obrigatorio) |

### Perfis Spring

| Perfil | Ficheiro | Uso |
|---|---|---|
| `default` | `application.yml` | Desenvolvimento local |
| `test` | `application-test.yml` | Testes automatizados |
| `staging` | `application-staging.yml` | Ambiente de staging |
| `production` | `application-production.yml` | Producao |

### URLs de Producao

- **API:** `https://sgc-api.embaixada-angola.site`
- **Keycloak:** `https://auth.embaixada-angola.site/realms/ecossistema`

---

## Dependencias do Ecossistema

Este servico depende dos seguintes modulos partilhados do [ecossistema-commons](https://github.com/embaixada-angola-alemanha/ecossistema-commons):

- `commons-dto` -- DTOs partilhados entre servicos
- `commons-security` -- Configuracao de seguranca OAuth2/Keycloak
- `commons-audit` -- Trilha de auditoria
- `commons-integration` -- Integracao inter-servicos (eventos)
- `commons-notification` -- Servico de notificacoes (RabbitMQ + email)
- `commons-storage` -- Armazenamento de ficheiros (MinIO)

---

## Projecto Principal

Este repositorio faz parte do **Ecossistema Digital da Embaixada de Angola na Alemanha**.

Repositorio principal: https://github.com/embaixada-angola-alemanha/ecossistema-project
