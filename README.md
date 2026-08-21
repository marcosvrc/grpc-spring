# grpc-spring

Aplicação de exemplo em **Spring Boot** que expõe um serviço de cadastro de produtos via **gRPC**, com persistência em **PostgreSQL** (JPA/Hibernate) e testes automatizados usando **H2** e o servidor gRPC em modo *in-process*.

Projeto desenvolvido originalmente a partir do curso **"Spring Boot e gRPC - Crie uma aplicação com gRPC e Java"** (Udemy), evoluído aqui com atualização de dependências e documentação.

## Sumário

- [Sobre o projeto](#sobre-o-projeto)
- [Stack e versões](#stack-e-versões)
- [Arquitetura e estrutura de pastas](#arquitetura-e-estrutura-de-pastas)
- [Pré-requisitos](#pré-requisitos)
- [Como executar](#como-executar)
- [Serviço gRPC](#serviço-grpc)
- [Testes](#testes)
- [Configuração](#configuração)
- [Atualizações realizadas nesta revisão](#atualizações-realizadas-nesta-revisão)
- [Próximos passos recomendados](#próximos-passos-recomendados)
- [Observações conhecidas](#observações-conhecidas)

## Sobre o projeto

O projeto implementa um CRUD simples de produtos (`create`, `findById`, `findAll`, `delete`) exposto exclusivamente via gRPC — não há camada REST/HTTP. As mensagens e o contrato do serviço são definidos em Protocol Buffers (`src/main/proto/product-service.proto`) e o código Java do serviço/stub é gerado automaticamente pelo `protobuf-maven-plugin` durante o build.

A camada de persistência usa Spring Data JPA sobre PostgreSQL em tempo de execução, com H2 em memória para os testes. Existe também uma classe cliente (`ClientGrpc`) com um `main` de exemplo, útil para testar o serviço manualmente sem depender de ferramentas externas como `grpcurl` ou `BloomRPC`.

## Stack e versões

| Componente | Versão neste projeto | Observação |
|---|---|---|
| Java | 21 (LTS) | Compatível com Spring Boot 3.x. Veja [Próximos passos](#próximos-passos-recomendados) sobre Java 25 LTS. |
| Spring Boot | 3.5.16 | Última versão mantida da série 3.x (a série 3.3 encerrou o suporte open source em jun/2026). |
| gRPC (io.grpc) | 1.63.0 | Alinhado com a versão testada pelo starter `net.devh` 3.1.0.RELEASE. |
| Protocol Buffers | 3.25.3 | Compilador `protoc` e runtime `protobuf-java`. |
| grpc-spring-boot-starter (`net.devh`) | 3.1.0.RELEASE | Primeira linha do starter compatível com Jakarta EE 9 / Spring Boot 3.x. |
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
├── service/                                Interface + implementação da regra de negócio
├── repository/ProductRepository.java       Spring Data JPA
├── entity/ProductEntity.java               Entidade JPA (jakarta.persistence)
├── dto/                                    DTOs de entrada/saída da camada de serviço
├── util/ProductConverterUtil.java          Conversão entre Entity ↔ DTO ↔ mensagens gRPC
├── exception/                              Exceções de negócio (mapeadas para status gRPC)
└── handler/ExceptionHandler.java           @GrpcAdvice - tradução de exceções para Status gRPC
src/main/resources/
├── application.properties                  Configuração de runtime (PostgreSQL)
└── db/migration/V1__Init.sql               Script Flyway (schema + massa inicial)
src/test/java/...                           Testes unitários (JUnit 5 + Mockito + AssertJ)
```

O fluxo de uma chamada é: `ProductController` (adaptador gRPC) → `IProductService` / `ProductServiceImpl` (regra de negócio) → `ProductRepository` (persistência) → `ProductEntity`. As conversões entre o mundo gRPC (`ProductRequest`/`ProductResponse`, gerados a partir do `.proto`) e o mundo interno (DTOs/entidades) ficam centralizadas em `ProductConverterUtil`.

## Pré-requisitos

- JDK 21 instalado e configurado (`JAVA_HOME`).
- Maven 3.9+ (o wrapper `./mvnw` já está incluído no repositório e não exige instalação manual do Maven).
- Docker e Docker Compose, para subir o PostgreSQL local.

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

Na primeira execução, o Hibernate cria/atualiza o schema automaticamente (`spring.jpa.generate-ddl=true`). O servidor gRPC sobe, por padrão, na porta `9090`.

**3. (Opcional) Teste manualmente com o cliente de exemplo:**

Com a aplicação rodando, execute a classe `br.com.grpc.spring.client.ClientGrpc` (via sua IDE ou `mvn exec:java`). Ela cria dois produtos, consulta por ID, lista todos e depois remove os produtos criados, imprimindo cada resposta no console.

## Serviço gRPC

Contrato completo em [`src/main/proto/product-service.proto`](src/main/proto/product-service.proto):

| RPC | Requisição | Resposta | Descrição |
|---|---|---|---|
| `Create` | `ProductRequest` | `ProductResponse` | Cria um produto (`name`, `price`, `quantity_in_stock`). Retorna erro `ALREADY_EXISTS` se já existir produto com o mesmo nome. |
| `FindById` | `RequestById` | `ProductResponse` | Busca um produto pelo `id`. Retorna erro `NOT_FOUND` se não existir. |
| `FindAll` | `EmptyRequest` | `ProductResponseList` | Lista todos os produtos cadastrados. |
| `Delete` | `RequestById` | `EmptyResponse` | Remove um produto pelo `id`. Retorna erro `NOT_FOUND` se não existir. |

Erros de negócio (`ProductAlreadyExistsException`, `ProductNotFoundException`) são traduzidos para o `Status` gRPC correspondente por `ExceptionHandler` (`@GrpcAdvice`), então o cliente recebe um `StatusRuntimeException` com o código apropriado (`ALREADY_EXISTS`, `NOT_FOUND`) em vez de um erro genérico.

## Testes

```bash
./mvnw test
```

Os testes de unidade (`ProductServiceImplTest`, `ProductConverterUtilTest`) usam JUnit 5, Mockito e AssertJ, sem subir banco de dados real. O perfil de teste (`application-test.properties`) configura H2 em memória e o servidor gRPC em modo *in-process* (sem abrir porta de rede), o que torna os testes rápidos e independentes de infraestrutura externa.

## Configuração

As configurações de runtime ficam em `src/main/resources/application.properties` (perfil padrão, usado com PostgreSQL) e `application-test.properties` (perfil de testes, usado com H2). Principais propriedades:

| Propriedade | Descrição |
|---|---|
| `spring.datasource.url` | URL de conexão com o banco (Postgres em runtime, H2 em testes). |
| `spring.jpa.generate-ddl` / `hibernate.hbm2ddl.auto` | Geração automática de schema pelo Hibernate. |
| `grpc.server.port` | Porta do servidor gRPC (`-1` nos testes, para usar apenas o canal in-process). |
| `grpc.server.inProcessName` / `grpc.client.inProcess.address` | Nome do canal in-process compartilhado entre servidor e cliente de teste. |

## Atualizações realizadas nesta revisão

Esta revisão focou em colocar o projeto em dia com a linha atual do Spring Boot 3 e corrigir uma incompatibilidade que impedia a compilação:

- **Correção crítica:** `ProductEntity` importava `javax.persistence.*`, namespace descontinuado desde a migração do ecossistema Jakarta EE 9 (Spring Boot 3.0+). Como o `pom.xml` já declarava `spring-boot-starter-parent` na série 3.x, o projeto não compilava. Corrigido para `jakarta.persistence.*`.
- Spring Boot: `3.3.2` → `3.5.16` (última versão da série 3.x com suporte ativo).
- `net.devh:grpc-server-spring-boot-starter` / `grpc-client-spring-boot-starter`: `2.14.0.RELEASE` → `3.1.0.RELEASE` (a série `2.x` é a última compatível com `javax.*`/Spring Boot 2; a série `3.x` é a compatível com Jakarta/Spring Boot 3).
- `io.grpc` (`grpc-stub`, `grpc-protobuf`): `1.58.0` → `1.63.0`, alinhado com a versão testada pelo starter `net.devh` 3.1.0.RELEASE.
- Protocol Buffers: `3.14.0` → `3.25.3`, também alinhado ao BOM usado pelo starter `net.devh` 3.1.0.RELEASE.
- `os-maven-plugin`: `1.7.0` → `1.7.1`.
- `assertj-core`: passou a herdar a versão gerenciada pelo BOM do Spring Boot em vez de fixar `3.24.2` manualmente, evitando divergência silenciosa em futuras atualizações do Boot.

## Próximos passos recomendados

Estes itens não foram aplicados automaticamente por representarem mudanças maiores, que merecem uma rodada de testes dedicada:

- **Java 25 (LTS):** Java 21 continua sendo uma LTS válida e suportada, mas Java 25 (lançada em set/2025) é a LTS mais recente. Migrar é opcional, mas recomendado a médio prazo.
- **Spring Boot 4 / Spring Framework 7:** já é a geração atual do Spring Boot (branches `4.0`/`4.1` ativas), exige Java 17+ e traz mudanças de configuração e possíveis breaking changes. Vale planejar como um projeto próprio de migração, e não como parte desta atualização de rotina.
- **Migrar de `net.devh/grpc-spring-boot-starter` para o [Spring gRPC](https://github.com/spring-projects/spring-grpc) oficial** (`org.springframework.grpc`, hoje na série `1.0.x`): é o projeto mantido pela própria equipe do Spring para integração gRPC, e passa a ser o caminho natural ao evoluir para Spring Boot 4.x. O starter `net.devh` segue funcional, mas não é mais o único caminho recomendado.
- Revisar se a dependência `jakarta.annotation-api:1.3.5` (com o comentário "Do NOT update to 2.0.0") ainda é necessária: esse workaround era usado para suprir a anotação `@Generated` em módulos Java 9+ com stacks baseadas em `javax.*`; hoje o `jakarta.annotation-api` já é gerenciado pelo BOM do Spring Boot em uma versão compatível com Jakarta, então vale testar a remoção dessa fixação manual.

## Observações conhecidas

- O `flyway-core` está declarado apenas com `scope=test`, mas existe um script de migração em `src/main/resources/db/migration/V1__Init.sql`. Isso significa que, em runtime (perfil padrão com Postgres), o Flyway **não** está no classpath e esse script não é executado — o schema é criado apenas pelo `hibernate.hbm2ddl.auto=update`. Se o uso do Flyway for intencional para produção, mova a dependência para o escopo padrão (compile/runtime) e avalie desativar `spring.jpa.generate-ddl`/`hbm2ddl.auto=update`, já que as duas estratégias de schema juntas tendem a divergir com o tempo.
- Os campos de dependência (`@Autowired` em campo) em `ProductController` e `ProductServiceImpl` funcionam, mas injeção via construtor é a prática atualmente recomendada pelo Spring (facilita testes e deixa dependências obrigatórias explícitas).
