# 🍽️ Restaurant Management API

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
- **Maven**

---

## 📐 Modelo de domínio

O sistema simula o fluxo real de um restaurante: usuários autenticam, mesas são ocupadas, pedidos são abertos, itens são adicionados ao pedido e a comanda é fechada.

```
User (1) ──────────────── (1) Profile
 │
 └──── (1) ──── (*) Order
                  │
                  └──── (1) ──── (*) OrderItem ────(*) ──── (1) MenuItem
                                                                  │
                  RestaurantTable (1) ──── (*) Order    (*) ──── (*) Category
```

### Entidades

| Entidade | Responsabilidade |
|---|---|
| `User` | Autenticação e autorização (roles: `CUSTOMER`, `WAITER`, `ADMIN`) |
| `Profile` | Dados complementares do usuário (telefone, bio, avatar) |
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

### Rotas públicas

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/categories/**`
- `GET /api/v1/menuitems/**`

Todo o restante exige autenticação.

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

---

## ⚙️ Regras de negócio principais

- Um pedido só pode ser criado se a mesa estiver com status `AVAILABLE`
- Ao criar um pedido, a mesa passa automaticamente para `OCCUPIED`
- Ao fechar ou cancelar um pedido, a mesa volta para `AVAILABLE`
- Itens só podem ser adicionados a pedidos com status `OPEN`
- Itens só podem ser adicionados se o `MenuItem` estiver disponível (`available = true`)
- O preço de cada `OrderItem` é congelado (snapshot) no momento da criação — mudanças futuras no preço do `MenuItem` não afetam pedidos já existentes
- O `totalPrice` do pedido é recalculado automaticamente ao adicionar ou remover itens (apenas se o pedido ainda estiver `OPEN`)
- Todo novo usuário nasce com a role `CUSTOMER` — promoção para `WAITER`/`ADMIN` é uma ação administrativa, não uma escolha do próprio usuário no registro

---

## 🚀 Como rodar o projeto

### Pré-requisitos

- Java 21
- Maven
- Docker

### 1. Subir o banco de dados

```bash
docker-compose up -d
```

### 2. Configurar variáveis de ambiente

Ajuste o `application.properties` com suas credenciais de banco e uma chave secreta JWT (mínimo 256 bits, em Base64):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5434/restaurant_db
spring.datasource.username=restaurant_user
spring.datasource.password=restaurant_pass

jwt.secret=sua-chave-secreta-em-base64
jwt.expiration=86400000
```

### 3. Rodar a aplicação

```bash
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

---

## 🧪 Testando a API

Exemplo de fluxo básico via cURL:

```bash
# Registro
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Ian","email":"ian@teste.com","password":"senha123"}'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ian@teste.com","password":"senha123"}'

# Criar categoria (autenticado)
curl -X POST http://localhost:8080/api/v1/categories \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"name":"Bebidas","description":"Sucos e refrigerantes"}'
```

---

## 📌 Roadmap

- [x] Modelagem de entidades e relacionamentos JPA
- [x] Camada de repositórios, DTOs e serviços
- [x] Controllers REST
- [x] Autenticação e autorização com Spring Security + JWT
- [x] Tratamento global de exceções por status HTTP
- [ ] Autorização por role (`@PreAuthorize` / `hasRole`)
- [ ] Testes unitários (JUnit 5 + Mockito)
- [ ] Testes de integração (Testcontainers)
- [ ] Documentação da API (SpringDoc OpenAPI/Swagger)
- [ ] Cache com Redis para consultas de cardápio
- [ ] Notificação de pedidos em tempo real com RabbitMQ

---

## 👤 Autor

**Ian Kiyoshi Kobayashi**
[GitHub](https://github.com/Iankyoo)