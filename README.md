# grpc-spring

**Serviço de cadastro de produtos exposto via gRPC, construído com Spring Boot.**

[![Java](https://img.shields.io/badge/Java-21_LTS-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![gRPC](https://img.shields.io/badge/gRPC-1.83.1-4285F4?style=flat-square&logo=googlecloud&logoColor=white)](https://grpc.io/)
[![Protocol Buffers](https://img.shields.io/badge/Protobuf-4.35.1-blue?style=flat-square)](https://protobuf.dev/)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36?style=flat-square&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![License: MIT](https://img.shields.io/github/license/marcosvrc/grpc-spring?style=flat-square)](LICENSE)
[![Last Commit](https://img.shields.io/github/last-commit/marcosvrc/grpc-spring?style=flat-square)](https://github.com/marcosvrc/grpc-spring/commits)

Aplicação de exemplo em **Spring Boot** que expõe um serviço de cadastro de produtos via **gRPC**, com persistência em **PostgreSQL** (JPA/Hibernate) e testes automatizados usando **H2**.

Projeto desenvolvido originalmente a partir do curso **"Spring Boot e gRPC - Crie uma aplicação com gRPC e Java"** (Udemy), evoluído aqui com atualização de dependências e documentação.

## Sumário

- [Sobre o projeto](#sobre-o-projeto)
- [Stack e versões](#stack-e-versões)
- [Arquitetura e estrutura de pastas](#arquitetura-e-estrutura-de-pastas)
- [Pré-requisitos](#pré-requisitos)
- [Como executar](#como-executar)
- [Serviço gRPC](#serviço-grpc)
- [Autenticação e TLS](#autenticação-e-tls)
- [Testando o serviço manualmente](#testando-o-serviço-manualmente)
- [Testes](#testes)
- [Configuração](#configuração)
- [Histórico de atualizações](#histórico-de-atualizações)
- [Próximos passos recomendados](#próximos-passos-recomendados)
- [Observações conhecidas](#observações-conhecidas)
- [Segurança](#segurança)
- [Licença](#licença)

## Sobre o projeto

O projeto implementa um CRUD simples de produtos (`create`, `findById`, `findAll`, `delete`) exposto exclusivamente via gRPC — não há camada REST/HTTP. As mensagens e o contrato do serviço são definidos em Protocol Buffers (`src/main/proto/product-service.proto`) e o código Java do serviço/stub é gerado automaticamente pelo `protobuf-maven-plugin` durante o build.

A camada de persistência usa Spring Data JPA sobre PostgreSQL em tempo de execução, com H2 em memória para os testes. Existe também uma classe cliente (`ClientGrpc`) com um `main` de exemplo, útil para testar o serviço manualmente sem depender de ferramentas externas como `grpcurl` ou `BloomRPC`.

## Stack e versões

| Componente | Versão neste projeto | Observação |
|---|---|---|
| Java | 21 (LTS) | Compatível com Spring Boot 4.1 (mínimo Java 17). Veja [Próximos passos](#próximos-passos-recomendados) sobre Java 25 LTS. |
| Spring Boot | 4.1.1 | Geração atual do Spring Boot, baseada em Spring Framework 7. |
| gRPC | Nativo do Spring Boot (`spring-boot-starter-grpc-server`) | Sem starter de terceiros; versão do `io.grpc` gerenciada pelo BOM do Spring Boot (`1.83.1`). |
| Protocol Buffers | 4.35.1 | Compilador `protoc` e runtime `protobuf-java`, alinhados ao que o Spring Boot 4.1.1 gerencia. |
| Spring Data JPA / Hibernate | Gerenciado pelo BOM do Spring Boot | Namespace `jakarta.persistence`. |
| PostgreSQL Driver | Gerenciado pelo BOM do Spring Boot | Usado em runtime. |
| H2 | Gerenciado pelo BOM do Spring Boot | Usado apenas em testes. |
| Lombok | Gerenciado pelo BOM do Spring Boot | Geração de getters/setters/builders. |
| AssertJ / JUnit 5 / Mockito | Gerenciado pelo BOM do Spring Boot | Testes unitários. |

> As versões "gerenciadas pelo BOM do Spring Boot" não são fixadas explicitamente no `pom.xml`: elas seguem automaticamente a versão do `spring-boot-starter-parent`, o que reduz conflitos de dependência e facilita futuras atualizações.

## Arquitetura e estrutura de pastas

```
src/main/proto/product-service.proto        Contrato gRPC (fonte da verdade da API)
src/main/java/br/com/grpc/spring/
├── Application.java                        Bootstrap do Spring Boot
├── client/ClientGrpc.java                  Cliente de exemplo (main), útil para testes manuais
├── controller/ProductController.java       Implementação do serviço gRPC (@GrpcService)
├── security/ApiKeyServerInterceptor.java   Interceptor global de autenticação (header x-api-key)
├── service/                                Interface + implementação da regra de negócio
├── repository/ProductRepository.java       Spring Data JPA
├── entity/ProductEntity.java               Entidade JPA (jakarta.persistence)
├── dto/                                    DTOs de entrada/saída da camada de serviço
├── util/ProductConverterUtil.java          Conversão entre Entity ↔ DTO ↔ mensagens gRPC
├── exception/                              Exceções de negócio (mapeadas para status gRPC)
└── handler/ExceptionHandler.java           Bean GrpcExceptionHandler - tradução de exceções para Status gRPC
src/main/resources/
├── application.properties                  Configuração de runtime (PostgreSQL, porta gRPC, API key)
├── application-tls.properties              Perfil opcional: habilita TLS (ver Autenticação e TLS)
└── db/migration/V1__Init.sql               Script Flyway (schema + massa inicial)
scripts/generate-dev-cert.sh                Gera o certificado autoassinado usado pelo perfil "tls"
src/test/java/...                           Testes unitários (JUnit 5 + Mockito + AssertJ)
```

O fluxo de uma chamada é: `ProductController` (adaptador gRPC) → `IProductService` / `ProductServiceImpl` (regra de negócio) → `ProductRepository` (persistência) → `ProductEntity`. As conversões entre o mundo gRPC (`ProductRequest`/`ProductResponse`, gerados a partir do `.proto`) e o mundo interno (DTOs/entidades) ficam centralizadas em `ProductConverterUtil`.

## Pré-requisitos

- JDK 21 instalado e configurado (`JAVA_HOME`).
- Maven 3.6.3+ (o wrapper `./mvnw` já está incluído no repositório e não exige instalação manual do Maven).
- Docker e Docker Compose, para subir o PostgreSQL local.
- OpenSSL, apenas se for usar o perfil `tls` (ver [Autenticação e TLS](#autenticação-e-tls)).

## Como executar

**1. Suba o banco de dados PostgreSQL:**

```bash
docker-compose up -d
```

Isso cria um container Postgres na porta `5432`, com o banco `productdb` e usuário/senha `admin`/`admin` (ver `docker-compose.yml` e `application.properties`).

**2. Compile e execute a aplicação:**

```bash
./mvnw spring-boot:run
```

Na primeira execução, o Hibernate cria/atualiza o schema automaticamente (`spring.jpa.generate-ddl=true`). O servidor gRPC sobe na porta `9090` (`spring.grpc.server.port`), **exigindo o header `x-api-key`** em toda chamada (ver [Autenticação e TLS](#autenticação-e-tls)).

**3. (Opcional) Teste manualmente com o cliente de exemplo:**

Com a aplicação rodando, execute a classe `br.com.grpc.spring.client.ClientGrpc` (via sua IDE ou `mvn exec:java`). Ela já envia o header `x-api-key` esperado pelo servidor (usa o mesmo default `local-dev-key-change-me`, ou lê a variável de ambiente `API_KEY` se você tiver customizado). Cria dois produtos, consulta por ID, lista todos (paginado) e depois remove os produtos criados, imprimindo cada resposta no console.

## Serviço gRPC

Contrato completo em [`src/main/proto/product-service.proto`](src/main/proto/product-service.proto):

| RPC | Requisição | Resposta | Descrição |
|---|---|---|---|
| `Create` | `ProductRequest` | `ProductResponse` | Cria um produto (`name`, `price`, `quantity_in_stock`). Retorna erro `ALREADY_EXISTS` se já existir produto com o mesmo nome, ou `INVALID_ARGUMENT` se os campos não passarem na validação (ver abaixo). |
| `FindById` | `RequestById` | `ProductResponse` | Busca um produto pelo `id`. Retorna erro `NOT_FOUND` se não existir, ou `INVALID_ARGUMENT` se `id` não for positivo. |
| `FindAll` | `FindAllRequest` | `ProductResponseList` | Lista produtos paginados (`page`, `size`). `size` omitido/≤ 0 usa o padrão (20); acima de 100 é limitado a 100. A resposta inclui `page`, `size`, `total_elements` e `total_pages`. |
| `Delete` | `RequestById` | `EmptyResponse` | Remove um produto pelo `id`. Retorna erro `NOT_FOUND` se não existir, ou `INVALID_ARGUMENT` se `id` não for positivo. |

Toda chamada passa antes pelo `ApiKeyServerInterceptor` (ver [Autenticação e TLS](#autenticação-e-tls)), que rejeita com `UNAUTHENTICATED` requisições sem o header `x-api-key` correto.

Erros de negócio (`ProductAlreadyExistsException`, `ProductNotFoundException`) e de validação de entrada (`ConstraintViolationException`, via Bean Validation em `ProductInputDTO`/`IProductService`) são traduzidos para o `Status` gRPC correspondente pelo bean `ExceptionHandler` (implementação de `org.springframework.grpc.server.exception.GrpcExceptionHandler`), então o cliente recebe um erro gRPC com o código apropriado (`ALREADY_EXISTS`, `NOT_FOUND`, `INVALID_ARGUMENT`) em vez de um erro genérico.

## Autenticação e TLS

### Autenticação (API key)

Toda chamada gRPC precisa do header (metadata) `x-api-key`, validado pelo `ApiKeyServerInterceptor` (registrado globalmente via `@GlobalServerInterceptor`, aplicado a todos os métodos do serviço). Sem o header, ou com um valor incorreto, a chamada é rejeitada com `Status.UNAUTHENTICATED` antes de chegar em `ProductController`.

A chave esperada vem de `app.security.api-key` em `application.properties`, com default `local-dev-key-change-me` — troque via variável de ambiente `API_KEY` (ver [Variáveis de ambiente](#variáveis-de-ambiente)) fora de um ambiente de estudo local. `ClientGrpc.java` já envia o header automaticamente.

**Sem TLS, esse header trafega em texto plano** — a autenticação por si só não protege a chave em trânsito; combine com o perfil `tls` abaixo antes de expor o serviço fora do `localhost`.

### TLS (opcional, perfil `tls`)

Por padrão a aplicação roda em texto plano (adequado para desenvolvimento local isolado). Para habilitar TLS com um certificado autoassinado:

```bash
# 1. Gere o certificado de desenvolvimento (uma vez só; fica em certs/, fora do Git)
./scripts/generate-dev-cert.sh

# 2. Rode a aplicação com o perfil "tls"
./mvnw spring-boot:run -Dspring-boot.run.profiles=tls
```

O perfil `tls` (`application-tls.properties`) configura um [SSL Bundle](https://docs.spring.io/spring-boot/reference/features/ssl.html) em formato PEM apontando para `certs/server-cert.pem`/`certs/server-key.pem`, e habilita `spring.grpc.server.ssl.enabled=true`.

Com TLS ativo, clientes em texto plano (`.usePlaintext()`) — incluindo o `ClientGrpc.java` padrão — deixam de funcionar. Para testar com `grpcurl`:

```bash
grpcurl -cacert certs/server-cert.pem \
  -import-path ./src/main/proto -proto product-service.proto \
  -H 'x-api-key: local-dev-key-change-me' \
  -d '{}' \
  localhost:9090 br.com.grpc.spring.ProductService/FindAll
```

> **Este certificado é autoassinado, só para desenvolvimento/teste.** Fora do `localhost` (produção, ambiente compartilhado), use um certificado emitido por uma CA de verdade — a SSL Bundle do Spring Boot aceita qualquer PEM válido no lugar do gerado pelo script.

## Testando o serviço manualmente

Como o serviço é exposto só via gRPC (não há REST/HTTP), chamadas manuais precisam de um cliente que fale gRPC — não dá pra usar `curl` puro. Com a aplicação rodando (`./mvnw spring-boot:run`, porta `9090`), toda chamada precisa do header `x-api-key` (ver [Autenticação e TLS](#autenticação-e-tls)); os exemplos abaixo já incluem `local-dev-key-change-me`, o default local. Qualquer uma das opções abaixo funciona; escolha pela conveniência:

### grpcurl (linha de comando, sem GUI)

```bash
brew install grpcurl
```

O projeto não expõe *server reflection* por padrão (ver [Próximos passos](#próximos-passos-recomendados)), então cada chamada aponta direto pro `.proto`:

```bash
# Create
grpcurl -plaintext \
  -import-path ./src/main/proto -proto product-service.proto \
  -H 'x-api-key: local-dev-key-change-me' \
  -d '{"name": "COMPUTADOR", "price": 2540.99, "quantityInStock": 400}' \
  localhost:9090 br.com.grpc.spring.ProductService/Create

# FindById
grpcurl -plaintext \
  -import-path ./src/main/proto -proto product-service.proto \
  -H 'x-api-key: local-dev-key-change-me' \
  -d '{"id": "1"}' \
  localhost:9090 br.com.grpc.spring.ProductService/FindById

# FindAll (paginado — page/size opcionais, default size=20)
grpcurl -plaintext \
  -import-path ./src/main/proto -proto product-service.proto \
  -H 'x-api-key: local-dev-key-change-me' \
  -d '{"page": 0, "size": 10}' \
  localhost:9090 br.com.grpc.spring.ProductService/FindAll

# Delete
grpcurl -plaintext \
  -import-path ./src/main/proto -proto product-service.proto \
  -H 'x-api-key: local-dev-key-change-me' \
  -d '{"id": "1"}' \
  localhost:9090 br.com.grpc.spring.ProductService/Delete
```

Para listar os métodos disponíveis a partir do `.proto` (sem depender de reflection):

```bash
grpcurl -plaintext -import-path ./src/main/proto -proto product-service.proto localhost:9090 list
```

Testando com o perfil `tls` ativo, ver [Autenticação e TLS](#autenticação-e-tls) — troque `-plaintext` por `-cacert certs/server-cert.pem`.

### grpcui (interface web local)

```bash
brew install grpcui
grpcui -plaintext -import-path ./src/main/proto -proto product-service.proto \
  -rpc-header 'x-api-key: local-dev-key-change-me' \
  localhost:9090
```

Abre uma UI no navegador — parecida com o Postman, mas específica pra gRPC — já com os métodos e mensagens de exemplo montados a partir do `.proto`.

### Postman

Há uma collection pronta em [`postman/grpc-spring.postman_collection.json`](postman/grpc-spring.postman_collection.json), com os 4 métodos (`Create`, `FindById`, `FindAll`, `Delete`) já apontando para `localhost:9090` e com o header `x-api-key` pré-preenchido. Depois de importar, abra a aba **Service definition** de cada request e importe `src/main/proto/product-service.proto` — o Postman precisa da definição do proto pra montar/validar as mensagens (o suporte a gRPC do Postman não é totalmente coberto pelo formato de exportação de collection, então esse passo manual às vezes é necessário mesmo com a collection já importada).

### Cliente de exemplo (`ClientGrpc`)

Opção que não exige instalar nenhuma ferramenta nova: com a aplicação rodando, execute a classe `br.com.grpc.spring.client.ClientGrpc` (via IDE ou `mvn exec:java`) — ver [Como executar](#como-executar), passo 3. Ela já envia o header `x-api-key` e exercita os 4 métodos em sequência (cria dois produtos, busca por ID, lista todos, remove) e imprime cada resposta no console.

## Testes

```bash
./mvnw test
```

Os testes de unidade (`ProductServiceImplTest`, `ProductConverterUtilTest`) usam JUnit 5, Mockito e AssertJ, sem subir banco de dados real nem transporte gRPC — testam a camada de serviço isoladamente com mocks do repositório. O perfil de teste (`application-test.properties`) configura apenas o H2 em memória.

Se no futuro forem adicionados testes de integração que exercitem o serviço via gRPC de fato, use o suporte nativo do Spring Boot para transporte in-process (`@AutoConfigureTestGrpcTransport` + `@ImportGrpcClients`), documentado em [docs.spring.io/spring-boot/reference/io/grpc.html](https://docs.spring.io/spring-boot/reference/io/grpc.html).

## Configuração

As configurações de runtime ficam em `src/main/resources/application.properties` (perfil padrão, usado com PostgreSQL), `application-tls.properties` (perfil opcional, ver [Autenticação e TLS](#autenticação-e-tls)) e `application-test.properties` (perfil de testes, usado com H2). Principais propriedades:

| Propriedade | Descrição |
|---|---|
| `spring.datasource.url` | URL de conexão com o banco (Postgres em runtime, H2 em testes). |
| `spring.jpa.generate-ddl` / `hibernate.hbm2ddl.auto` | Geração automática de schema pelo Hibernate. |
| `spring.grpc.server.port` | Porta do servidor gRPC (`9090` por padrão neste projeto). |
| `app.security.api-key` | Chave exigida no header `x-api-key` de toda chamada gRPC (ver [Autenticação e TLS](#autenticação-e-tls)). |
| `spring.grpc.server.ssl.*` | Configuração de TLS do servidor gRPC (perfil `tls`). |

### Variáveis de ambiente

As credenciais e a conexão do Postgres (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`) e a chave de API (`API_KEY`) são lidas de variáveis de ambiente, tanto em `application.properties` quanto em `docker-compose.yml`, com defaults de desenvolvimento local — então `./mvnw spring-boot:run` e `docker-compose up -d` funcionam sem nenhuma configuração extra.

Este projeto **não usa arquivo `.env`**: valores reais (fora do ambiente de estudo local) são esperados como variáveis de ambiente reais do processo, injetadas por quem estiver rodando a aplicação — um cofre de segredos (Vault, AWS Secrets Manager, um secret do Kubernetes, etc.) ou, localmente, `export DB_PASSWORD=... && ./mvnw spring-boot:run`. `${DB_USER:admin}` e os demais placeholders em `application.properties` funcionam com qualquer uma dessas origens, sem precisar de nenhuma configuração adicional no código — é só garantir que a variável exista no ambiente do processo.

## Histórico de atualizações

### Revisão 5 — removido o suporte a `.env` local

- Avaliou-se ligar `application.properties` a um arquivo `.env` local (via `spring.config.import`), mas a decisão final foi não usar `.env`/`.env.example` neste projeto: segredos reais vão para um cofre de segredos (Vault) em vez de um arquivo local. `.env.example` foi removido; `application.properties` e `docker-compose.yml` continuam lendo `${DB_USER:admin}` e afins normalmente — funcionam com qualquer variável de ambiente real presente no processo, seja exportada manualmente, seja injetada pelo Vault (ou equivalente) em produção. Nenhuma mudança de código necessária para isso: o mecanismo `${VAR:default}` já era agnóstico à origem da variável.

### Revisão 4 — auditoria de segurança independente e correção de build

- **Auditoria de segurança do zero:** reavaliação completa do projeto (código, configuração, histórico do Git e CVEs das dependências fixadas), sem se apoiar nas rodadas anteriores, para conferir se algo tinha passado despercebido. Nenhuma vulnerabilidade nova encontrada. Checagens novas desta rodada: o driver JDBC do Postgres resolve para `42.7.13` via o BOM do `spring-boot-starter-parent:4.1.1`, já corrigindo a CVE-2026-54291 (downgrade de channel binding SCRAM); `grpc-netty` `1.83.1` não é afetado pela CVE-2025-55163 ("MadeYouReset" HTTP/2 DoS); e a CVE crítica do Spring Boot deste ano (CVE-2026-40976, bypass de segurança com Actuator) não se aplica — nem a versão (afeta só `4.0.0`–`4.0.5`) nem a dependência (`spring-boot-starter-actuator` não está no `pom.xml`) batem com este projeto.
- **Correção de build:** ao rodar `./mvnw` de verdade pela primeira vez desde a Revisão 3, `ClientGrpc.java` não compilava — `MetadataUtils.attachHeaders(stub, metadata)`, usado para anexar o header `x-api-key` no cliente de exemplo, é um método de conveniência que não existe mais no `grpc-java 1.83.1` (removido em alguma versão entre a época em que esse padrão era comum e a atual). Corrigido para usar `MetadataUtils.newAttachHeadersInterceptor(headers)` aplicado via `.withInterceptors(...)`, que é a API atual. Nenhum outro ponto do projeto usava `MetadataUtils`.

### Revisão 3 — autenticação, TLS e paginação

- **Autenticação:** adicionado `ApiKeyServerInterceptor` (`@GlobalServerInterceptor`), exigindo o header `x-api-key` em toda chamada gRPC. Chave configurável via `app.security.api-key` / variável de ambiente `API_KEY`. `ClientGrpc.java` atualizado para enviar o header automaticamente.
- **TLS:** adicionado perfil opcional `tls` (`application-tls.properties`), usando um [SSL Bundle](https://docs.spring.io/spring-boot/reference/features/ssl.html) PEM. Criado `scripts/generate-dev-cert.sh` para gerar um certificado autoassinado de desenvolvimento (não commitado — `certs/` está no `.gitignore`). Não é o perfil padrão, para não quebrar o fluxo local de `./mvnw spring-boot:run`.
- **Paginação:** `FindAll` passou a receber `FindAllRequest{page, size}` (antes `EmptyRequest`) e `ProductResponseList` ganhou `page`, `size`, `total_elements`, `total_pages`. Tamanho padrão de página: 20; máximo: 100 (protege contra um cliente pedir a tabela inteira de uma vez).
- `ClientGrpc.java`, os comandos `grpcurl`/`grpcui` (ver [Testando o serviço manualmente](#testando-o-serviço-manualmente)) e a collection do Postman foram todos atualizados para o novo contrato (header de API key + `FindAllRequest`).

### Revisão 2 — migração para Spring Boot 4.1 e gRPC nativo

- **Spring Boot `3.5.16` → `4.1.1`** (Spring Framework 7). Requer apenas Java 17+; o projeto permanece em Java 21.
- **Substituído o starter de terceiros `net.devh:grpc-server-spring-boot-starter` pelo suporte a gRPC nativo do Spring Boot** (`org.springframework.boot:spring-boot-starter-grpc-server`, disponível a partir da série 4.1). Isso elimina uma dependência externa e passa a receber suporte oficial do time do Spring.
  - `@GrpcService` agora vem do pacote `org.springframework.grpc.server.service` (antes `net.devh.boot.grpc.server.service`) — mesma anotação, mesmo comportamento sobre `ProductServiceGrpc.ProductServiceImplBase`.
  - O tratamento de exceções (`ExceptionHandler`) deixou de usar `@GrpcAdvice`/`@GrpcExceptionHandler` (anotações do net.devh) e passou a implementar a interface `org.springframework.grpc.server.exception.GrpcExceptionHandler`, registrada como `@Component`.
  - A propriedade de porta do servidor mudou de `grpc.server.port` (net.devh) para `spring.grpc.server.port` (nativa do Spring Boot), agora declarada explicitamente em `application.properties`.
  - Removida a dependência de teste `net.devh:grpc-client-spring-boot-starter` e as propriedades de canal in-process em `application-test.properties`: nenhum teste atual abre um canal gRPC de fato (são testes de unidade com Mockito), então essa configuração estava sem efeito. O caminho oficial para reintroduzir testes de integração gRPC está documentado na seção [Testes](#testes).
  - `io.grpc` (`grpc-stub`, `grpc-protobuf`) deixou de ser declarado explicitamente: agora vem transitivamente do starter, evitando divergência de versão em futuras atualizações do Spring Boot.
  - Removida a fixação manual de `jakarta.annotation-api:1.3.5` (com o comentário "Do NOT update to 2.0.0"): era um workaround para a anotação `@Generated` em código gerado, hoje desnecessário — o Spring Boot já gerencia essa dependência em uma versão compatível.
- `protobuf.version`: `3.25.3` → `4.35.1` e `grpc.version` (usado apenas pelo `protobuf-maven-plugin` para baixar o compilador `protoc` e o plugin `protoc-gen-grpc-java`) → `1.83.1`, alinhados ao que o BOM do Spring Boot 4.1.1 gerencia para `protobuf-java` e `io.grpc`.

### Revisão 1 — correção de compatibilidade Jakarta e atualização dentro da série Spring Boot 3.x

- **Correção crítica:** `ProductEntity` importava `javax.persistence.*`, namespace descontinuado desde a migração do ecossistema Jakarta EE 9 (Spring Boot 3.0+). Como o `pom.xml` já declarava `spring-boot-starter-parent` na série 3.x, o projeto não compilava. Corrigido para `jakarta.persistence.*`.
- Spring Boot: `3.3.2` → `3.5.16`.
- `net.devh:grpc-server-spring-boot-starter` / `grpc-client-spring-boot-starter`: `2.14.0.RELEASE` → `3.1.0.RELEASE`.
- `io.grpc`: `1.58.0` → `1.63.0`. Protocol Buffers: `3.14.0` → `3.25.3`.
- `os-maven-plugin`: `1.7.0` → `1.7.1`.
- `assertj-core`: passou a herdar a versão gerenciada pelo BOM do Spring Boot em vez de fixar `3.24.2` manualmente.

## Próximos passos recomendados

- **Java 25 (LTS):** não foi aplicado nesta revisão porque não é possível confirmar, a partir daqui, que o JDK 25 está instalado na sua máquina — mudar `java.version` no `pom.xml` sem isso quebraria o build local. Java 21 continua sendo uma LTS válida e suportada pelo Spring Boot 4.1. Para migrar: instale o JDK 25, ajuste `<java.version>25</java.version>` no `pom.xml` e rode `./mvnw clean verify` para validar.
- **Validar a migração localmente:** o ambiente usado nas Revisões 1–3 não tinha acesso ao Maven Central, então boa parte das mudanças foi validada só por análise estática/cruzada, não por build real — e isso realmente deixou passar um erro de compilação (ver Revisão 4). Depois da correção, rode `./mvnw clean verify` (com o Postgres via `docker-compose up -d`, se for validar também a subida da aplicação) antes de dar commit, para confirmar que não sobrou mais nada.
- Considerar habilitar `spring.grpc.server.reflection.enabled=true` (Server Reflection) para facilitar testes manuais com `grpcurl`/`grpcui`, já que agora é uma propriedade nativa de configuração.
- **Autenticação:** o `ApiKeyServerInterceptor` é uma solução simples (chave única compartilhada) — para múltiplos consumidores/produção, considerar evoluir para o suporte nativo do Spring gRPC a Spring Security (`GrpcSecurity`, `@PreAuthorize`), que permite tokens por cliente, escopos, etc.
- **TLS em produção:** o certificado gerado por `scripts/generate-dev-cert.sh` é autoassinado e só serve para desenvolvimento local. Fora do `localhost`, aponte o SSL Bundle (`application-tls.properties`) para um certificado emitido por uma CA de verdade.

## Observações conhecidas

- O `flyway-core` está declarado apenas com `scope=test`, mas existe um script de migração em `src/main/resources/db/migration/V1__Init.sql`. Isso significa que, em runtime (perfil padrão com Postgres), o Flyway **não** está no classpath e esse script não é executado — o schema é criado apenas pelo `hibernate.hbm2ddl.auto=update`. Se o uso do Flyway for intencional para produção, mova a dependência para o escopo padrão (compile/runtime) e avalie desativar `spring.jpa.generate-ddl`/`hbm2ddl.auto=update`, já que as duas estratégias de schema juntas tendem a divergir com o tempo.
- Os campos de dependência (`@Autowired` em campo) em `ProductController` e `ProductServiceImpl` funcionam, mas injeção via construtor é a prática atualmente recomendada pelo Spring (facilita testes e deixa dependências obrigatórias explícitas).
- TLS (perfil `tls`) é opcional e não vem ativado por padrão — ver [Autenticação e TLS](#autenticação-e-tls) e [Próximos passos](#próximos-passos-recomendados) sobre por que isso é intencional (um certificado autoassinado não deveria ser o default "seguro" de ninguém).

## Segurança

Foi feita uma análise de segurança do projeto (autenticação, TLS, credenciais, validação de entrada, paginação, imagem Docker, scanner de dependências) — achados e correções estão registrados nas entradas de [Histórico de atualizações](#histórico-de-atualizações), principalmente nas Revisões 3 e 4.

## Licença

Distribuído sob a licença MIT. Veja [`LICENSE`](LICENSE) para o texto completo.
