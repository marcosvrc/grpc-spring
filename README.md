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
- [Testando o serviço manualmente](#testando-o-serviço-manualmente)
- [Testes](#testes)
- [Configuração](#configuração)
- [Histórico de atualizações](#histórico-de-atualizações)
- [Próximos passos recomendados](#próximos-passos-recomendados)
- [Observações conhecidas](#observações-conhecidas)
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
├── service/                                Interface + implementação da regra de negócio
├── repository/ProductRepository.java       Spring Data JPA
├── entity/ProductEntity.java               Entidade JPA (jakarta.persistence)
├── dto/                                    DTOs de entrada/saída da camada de serviço
├── util/ProductConverterUtil.java          Conversão entre Entity ↔ DTO ↔ mensagens gRPC
├── exception/                              Exceções de negócio (mapeadas para status gRPC)
└── handler/ExceptionHandler.java           Bean GrpcExceptionHandler - tradução de exceções para Status gRPC
src/main/resources/
├── application.properties                  Configuração de runtime (PostgreSQL, porta gRPC)
└── db/migration/V1__Init.sql               Script Flyway (schema + massa inicial)
src/test/java/...                           Testes unitários (JUnit 5 + Mockito + AssertJ)
```

O fluxo de uma chamada é: `ProductController` (adaptador gRPC) → `IProductService` / `ProductServiceImpl` (regra de negócio) → `ProductRepository` (persistência) → `ProductEntity`. As conversões entre o mundo gRPC (`ProductRequest`/`ProductResponse`, gerados a partir do `.proto`) e o mundo interno (DTOs/entidades) ficam centralizadas em `ProductConverterUtil`.

## Pré-requisitos

- JDK 21 instalado e configurado (`JAVA_HOME`).
- Maven 3.6.3+ (o wrapper `./mvnw` já está incluído no repositório e não exige instalação manual do Maven).
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

Na primeira execução, o Hibernate cria/atualiza o schema automaticamente (`spring.jpa.generate-ddl=true`). O servidor gRPC sobe na porta `9090` (`spring.grpc.server.port`).

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

Erros de negócio (`ProductAlreadyExistsException`, `ProductNotFoundException`) são traduzidos para o `Status` gRPC correspondente pelo bean `ExceptionHandler` (implementação de `org.springframework.grpc.server.exception.GrpcExceptionHandler`), então o cliente recebe um erro gRPC com o código apropriado (`ALREADY_EXISTS`, `NOT_FOUND`) em vez de um erro genérico.

## Testando o serviço manualmente

Como o serviço é exposto só via gRPC (não há REST/HTTP), chamadas manuais precisam de um cliente que fale gRPC — não dá pra usar `curl` puro. Com a aplicação rodando (`./mvnw spring-boot:run`, porta `9090`), qualquer uma das opções abaixo funciona; escolha pela conveniência:

### grpcurl (linha de comando, sem GUI)

```bash
brew install grpcurl
```

O projeto não expõe *server reflection* por padrão (ver [Próximos passos](#próximos-passos-recomendados)), então cada chamada aponta direto pro `.proto`:

```bash
# Create
grpcurl -plaintext \
  -import-path ./src/main/proto -proto product-service.proto \
  -d '{"name": "COMPUTADOR", "price": 2540.99, "quantityInStock": 400}' \
  localhost:9090 br.com.grpc.spring.ProductService/Create

# FindById
grpcurl -plaintext \
  -import-path ./src/main/proto -proto product-service.proto \
  -d '{"id": "1"}' \
  localhost:9090 br.com.grpc.spring.ProductService/FindById

# FindAll
grpcurl -plaintext \
  -import-path ./src/main/proto -proto product-service.proto \
  -d '{}' \
  localhost:9090 br.com.grpc.spring.ProductService/FindAll

# Delete
grpcurl -plaintext \
  -import-path ./src/main/proto -proto product-service.proto \
  -d '{"id": "1"}' \
  localhost:9090 br.com.grpc.spring.ProductService/Delete
```

Para listar os métodos disponíveis a partir do `.proto` (sem depender de reflection):

```bash
grpcurl -plaintext -import-path ./src/main/proto -proto product-service.proto localhost:9090 list
```

### grpcui (interface web local)

```bash
brew install grpcui
grpcui -plaintext -import-path ./src/main/proto -proto product-service.proto localhost:9090
```

Abre uma UI no navegador — parecida com o Postman, mas específica pra gRPC — já com os métodos e mensagens de exemplo montados a partir do `.proto`.

### Postman

Há uma collection pronta em [`postman/grpc-spring.postman_collection.json`](postman/grpc-spring.postman_collection.json), com os 4 métodos (`Create`, `FindById`, `FindAll`, `Delete`) já apontando para `localhost:9090`. Depois de importar, abra a aba **Service definition** de cada request e importe `src/main/proto/product-service.proto` — o Postman precisa da definição do proto pra montar/validar as mensagens (o suporte a gRPC do Postman não é totalmente coberto pelo formato de exportação de collection, então esse passo manual às vezes é necessário mesmo com a collection já importada).

### Cliente de exemplo (`ClientGrpc`)

Opção que não exige instalar nenhuma ferramenta nova: com a aplicação rodando, execute a classe `br.com.grpc.spring.client.ClientGrpc` (via IDE ou `mvn exec:java`) — ver [Como executar](#como-executar), passo 3. Ela já exercita os 4 métodos em sequência (cria dois produtos, busca por ID, lista todos, remove) e imprime cada resposta no console.

## Testes

```bash
./mvnw test
```

Os testes de unidade (`ProductServiceImplTest`, `ProductConverterUtilTest`) usam JUnit 5, Mockito e AssertJ, sem subir banco de dados real nem transporte gRPC — testam a camada de serviço isoladamente com mocks do repositório. O perfil de teste (`application-test.properties`) configura apenas o H2 em memória.

Se no futuro forem adicionados testes de integração que exercitem o serviço via gRPC de fato, use o suporte nativo do Spring Boot para transporte in-process (`@AutoConfigureTestGrpcTransport` + `@ImportGrpcClients`), documentado em [docs.spring.io/spring-boot/reference/io/grpc.html](https://docs.spring.io/spring-boot/reference/io/grpc.html).

## Configuração

As configurações de runtime ficam em `src/main/resources/application.properties` (perfil padrão, usado com PostgreSQL) e `application-test.properties` (perfil de testes, usado com H2). Principais propriedades:

| Propriedade | Descrição |
|---|---|
| `spring.datasource.url` | URL de conexão com o banco (Postgres em runtime, H2 em testes). |
| `spring.jpa.generate-ddl` / `hibernate.hbm2ddl.auto` | Geração automática de schema pelo Hibernate. |
| `spring.grpc.server.port` | Porta do servidor gRPC (`9090` por padrão neste projeto). |

## Histórico de atualizações

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
- **Validar a migração localmente:** não foi possível compilar o projeto neste ambiente (o sandbox usado para esta revisão não tem acesso ao Maven Central), então as versões e a API do Spring gRPC foram validadas por análise cruzada da documentação oficial e dos POMs publicados, não por build real. Rode `./mvnw clean verify` (com o Postgres via `docker-compose up -d`, se for validar também a subida da aplicação) antes de dar commit.
- Considerar habilitar `spring.grpc.server.reflection.enabled=true` (Server Reflection) para facilitar testes manuais com `grpcurl`/`grpcui`, já que agora é uma propriedade nativa de configuração.

## Observações conhecidas

- O `flyway-core` está declarado apenas com `scope=test`, mas existe um script de migração em `src/main/resources/db/migration/V1__Init.sql`. Isso significa que, em runtime (perfil padrão com Postgres), o Flyway **não** está no classpath e esse script não é executado — o schema é criado apenas pelo `hibernate.hbm2ddl.auto=update`. Se o uso do Flyway for intencional para produção, mova a dependência para o escopo padrão (compile/runtime) e avalie desativar `spring.jpa.generate-ddl`/`hbm2ddl.auto=update`, já que as duas estratégias de schema juntas tendem a divergir com o tempo.
- Os campos de dependência (`@Autowired` em campo) em `ProductController` e `ProductServiceImpl` funcionam, mas injeção via construtor é a prática atualmente recomendada pelo Spring (facilita testes e deixa dependências obrigatórias explícitas).

## Licença

Distribuído sob a licença MIT. Veja [`LICENSE`](LICENSE) para o texto completo.
