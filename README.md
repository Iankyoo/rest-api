# 🍽️ Restaurant Management API

[![CI](https://github.com/Iankyoo/rest-api/actions/workflows/ci.yml/badge.svg)](https://github.com/Iankyoo/rest-api/actions/workflows/ci.yml)

API REST para gestão de restaurante, construída com Java e Spring Boot, cobrindo desde modelagem de dados com relacionamentos complexos até autenticação e autorização com JWT.

Projeto desenvolvido com foco em fixar o workflow completo de construção de uma API profissional: entidades, relacionamentos JPA, camada de serviço com regras de negócio, tratamento de exceções, segurança com Spring Security e testes.

---

## 🧱 Stack

- **Java 21**
- **Spring Boot 3.5**
- **Spring Data JPA / Hibernate**
- **Spring Security + JWT** (jjwt 0.12.6)
- **PostgreSQL 16**
- **Docker / Docker Compose**
- **Lombok**
- **Bean Validation (Jakarta Validation)**
- **SpringDoc OpenAPI (Swagger UI)**
- **JUnit 5, Mockito e MockMvc** (H2 em memória nos testes)
- **Maven**
- **GitHub Actions** (CI)

---

## 📐 Modelo de domínio

O sistema simula o fluxo real de um restaurante: usuários autenticam, mesas são ocupadas, pedidos são abertos, itens são adicionados ao pedido e a comanda é fechada.

```
User (1) ──── (*) Order
                  │
                  └──── (1) ──── (*) OrderItem ────(*) ──── (1) MenuItem
                                                                  │
                  RestaurantTable (1) ──── (*) Order    (*) ──── (*) Category
```

### Entidades

| Entidade | Responsabilidade |
|---|---|
| `User` | Autenticação e autorização (roles: `CUSTOMER`, `WAITER`, `ADMIN`) |
| `Category` | Categoria do cardápio (ex: Bebidas, Pratos principais) |
| `MenuItem` | Item do cardápio (nome, preço, disponibilidade) |
| `RestaurantTable` | Mesa física do restaurante (número, capacidade, status) |
| `Order` | Pedido/comanda vinculado a um usuário e uma mesa |
| `OrderItem` | Item dentro de um pedido — tabela de junção rica entre `Order` e `MenuItem` |

### Por que `OrderItem` é uma entidade própria

`OrderItem` não é um simples `@ManyToMany` entre `Order` e `MenuItem` porque carrega atributos próprios que um relacionamento simples não conseguiria armazenar:

- `quantity` — quantidade do item no pedido
- `unitPrice` — **preço snapshot**: o preço do item no momento exato da compra, preservado mesmo que o preço do `MenuItem` mude no futuro
- `observation` — observações do cliente (ex: "sem cebola")
- `orderItemStatus` — ciclo de vida próprio do item (`PENDING`, `PREPARING`, `READY`, `DELIVERED`, `CANCELLED`)

---

## 🔐 Autenticação e autorização

A API usa **Spring Security + JWT** com sessão `STATELESS` — o servidor não guarda nenhum estado de sessão entre requisições; toda a informação necessária para autenticar o usuário vive dentro do próprio token.

### Fluxo

1. `POST /api/v1/auth/register` — cria um novo usuário (role `CUSTOMER` por padrão, senha hasheada com BCrypt)
2. `POST /api/v1/auth/login` — valida credenciais e retorna um JWT
3. Requisições subsequentes enviam o token no header:
   ```
   Authorization: Bearer <token>
   ```
4. O `JwtAuthenticationFilter` intercepta cada requisição, valida o token e popula o `SecurityContextHolder` com o usuário autenticado

### Autorização por role

A API é um sistema interno do restaurante: o admin cuida do cardápio e das mesas, o garçom opera as comandas e o cliente apenas consulta o cardápio. As regras ficam centralizadas no `SecurityConfig`.

| Recurso | Público | CUSTOMER | WAITER | ADMIN |
|---|---|---|---|---|
| `POST /api/v1/auth/**` | ✅ | ✅ | ✅ | ✅ |
| `GET` categories / menuitems | ✅ | ✅ | ✅ | ✅ |
| `POST/PUT/DELETE` categories / menuitems | | | | ✅ |
| `GET` tables | | | ✅ | ✅ |
| `POST/PUT/DELETE` tables | | | | ✅ |
| Orders e order items | | | ✅ | ✅ |
| `PATCH /api/v1/users/{id}/role` | | | | ✅ |

- Sem token (ou com token inválido): **401 Unauthorized**
- Autenticado, mas sem a role necessária: **403 Forbidden**

### Admin inicial

Na inicialização, o `AdminSeeder` cria um usuário `ADMIN` com as credenciais de `ADMIN_EMAIL` e `ADMIN_PASSWORD` (definidas no `.env`), caso ele ainda não exista. A partir dele, outros usuários podem ser promovidos a `WAITER` ou `ADMIN`.

---

## 📋 Endpoints

### Auth
| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/auth/register` | Cria um novo usuário |
| POST | `/api/v1/auth/login` | Autentica e retorna um JWT |

### Categories
| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/categories` | Cria uma categoria |
| GET | `/api/v1/categories` | Lista categorias (paginado) |
| GET | `/api/v1/categories/{id}` | Busca categoria por ID |
| PUT | `/api/v1/categories/{id}` | Atualiza categoria |
| DELETE | `/api/v1/categories/{id}` | Remove categoria |

### Menu Items
| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/menuitems` | Cria um item do cardápio |
| GET | `/api/v1/menuitems` | Lista itens (paginado) |
| GET | `/api/v1/menuitems/{id}` | Busca item por ID |
| PUT | `/api/v1/menuitems/{id}` | Atualiza item |
| DELETE | `/api/v1/menuitems/{id}` | Remove item |

### Restaurant Tables
| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/tables` | Cria uma mesa |
| GET | `/api/v1/tables` | Lista mesas (paginado) |
| GET | `/api/v1/tables/{id}` | Busca mesa por ID |
| PUT | `/api/v1/tables/{id}` | Atualiza mesa |
| DELETE | `/api/v1/tables/{id}` | Remove mesa |

### Orders
| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/orders` | Abre um novo pedido (mesa vira `OCCUPIED`) |
| GET | `/api/v1/orders` | Lista pedidos (paginado) |
| GET | `/api/v1/orders/{id}` | Busca pedido por ID |
| PATCH | `/api/v1/orders/{id}/close` | Fecha o pedido (mesa volta a `AVAILABLE`) |
| PATCH | `/api/v1/orders/{id}/cancel` | Cancela o pedido (mesa volta a `AVAILABLE`) |

### Order Items
| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/orders/{orderId}/items` | Adiciona um item ao pedido |
| PATCH | `/api/v1/items/{itemId}/status` | Atualiza o status do item |
| DELETE | `/api/v1/items/{itemId}` | Remove item do pedido |

### Users
| Método | Rota | Descrição |
|---|---|---|
| PATCH | `/api/v1/users/{id}/role` | Altera a role de um usuário (somente `ADMIN`) |

---

## ⚙️ Regras de negócio principais

- Um pedido só pode ser criado se a mesa estiver com status `AVAILABLE`
- Ao criar um pedido, a mesa passa automaticamente para `OCCUPIED`
- Ao fechar ou cancelar um pedido, a mesa volta para `AVAILABLE`
- Itens só podem ser adicionados, removidos ou ter o status alterado em pedidos com status `OPEN` — pedido fechado ou cancelado é imutável
- Só é possível fechar ou cancelar um pedido que ainda está `OPEN`
- Itens só podem ser adicionados se o `MenuItem` estiver disponível (`available = true`)
- O preço de cada `OrderItem` é congelado (snapshot) no momento da criação — mudanças futuras no preço do `MenuItem` não afetam pedidos já existentes
- O `totalPrice` do pedido é recalculado automaticamente ao adicionar ou remover itens
- Todo novo usuário nasce com a role `CUSTOMER` — promoção para `WAITER`/`ADMIN` é uma ação administrativa, não uma escolha do próprio usuário no registro

---

## ❗ Tratamento de erros

Todas as exceções passam pelo `GlobalExceptionHandler`, que devolve um status HTTP coerente e um corpo JSON padronizado (`{"message": "..."}`):

| Status | Quando |
|---|---|
| 400 | Falha de validação (`@Valid`), JSON malformado, enum inválido ou regra de negócio violada (ex: pedido não está `OPEN`) |
| 401 | Sem token, token inválido/expirado ou credenciais erradas no login |
| 403 | Autenticado, mas sem a role necessária |
| 404 | Recurso não encontrado |
| 409 | Email já cadastrado, mesa ocupada ou recurso em uso (ex: apagar categoria vinculada a itens) |
| 500 | Erro inesperado — mensagem genérica para o cliente, detalhes apenas no log |

---

## 🚀 Como rodar o projeto

### Pré-requisitos

- Java 21
- Docker

O Maven não precisa estar instalado: o projeto usa o Maven Wrapper (`./mvnw`).

### 1. Configurar variáveis de ambiente

Credenciais do banco e a chave JWT não ficam no repositório. Copie o arquivo de exemplo e preencha os valores:

```bash
cp .env.example .env
```

Para gerar o `JWT_SECRET` (mínimo 256 bits, em Base64):

```bash
openssl rand -base64 64
```

O `.env` é lido tanto pelo Docker Compose quanto pelo Spring Boot (via `spring.config.import`).

### 2. Subir o banco de dados

```bash
docker compose up -d
```

### 3. Rodar a aplicação

```bash
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

---

## 📖 Documentação (Swagger)

Com a aplicação rodando, a documentação interativa fica em:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI (JSON): `http://localhost:8080/v3/api-docs`

Para chamar rotas protegidas pelo Swagger, faça login em `POST /api/v1/auth/login`, copie o `token` da resposta e cole no botão **Authorize**.

### Exemplo via cURL

```bash
# Login com o admin inicial (credenciais do .env)
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@restaurant.com","password":"<ADMIN_PASSWORD>"}'

# Criar categoria (somente ADMIN)
curl -X POST http://localhost:8080/api/v1/categories \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"name":"Bebidas","description":"Sucos e refrigerantes"}'

# Registrar um novo usuário (nasce como CUSTOMER)
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Ian","email":"ian@teste.com","password":"senha123"}'
```

---

## 🧪 Testes

```bash
./mvnw test
```

Os testes usam H2 em memória, então não precisam do Docker nem do `.env`. O CI roda a mesma suíte a cada push na `main`.

| Camada | Ferramentas | O que cobre |
|---|---|---|
| Services | JUnit 5 + Mockito | Regras de negócio isoladas, incluindo os caminhos de erro |
| Controllers | `@WebMvcTest` + MockMvc | Status HTTP, validação de entrada e tratamento de exceções |
| Segurança | `@WebMvcTest` + `@WithMockUser` | Matriz de roles (401/403/2xx), filtro JWT e `JwtService` |
| Integração | `@SpringBootTest` + MockMvc + H2 | Fluxo completo: login → cardápio → mesa → pedido → itens → fechamento |

---

## 📌 Roadmap

- [x] Modelagem de entidades e relacionamentos JPA
- [x] Camada de repositórios, DTOs e serviços
- [x] Controllers REST
- [x] Autenticação e autorização com Spring Security + JWT
- [x] Tratamento global de exceções por status HTTP
- [x] Autorização por role (`hasRole` no `SecurityConfig`)
- [x] Testes unitários, de controller e de integração
- [x] Documentação da API (SpringDoc OpenAPI/Swagger)
- [x] CI com GitHub Actions

### Próximos passos

- Testes de integração com Testcontainers (Postgres real em vez de H2)
- Migrations versionadas com Flyway no lugar do `ddl-auto=update`
- Máquina de estados para o status dos itens (`PENDING → PREPARING → READY → DELIVERED`)

---

## 👤 Autor

**Ian Kiyoshi Kobayashi**
[GitHub](https://github.com/Iankyoo)