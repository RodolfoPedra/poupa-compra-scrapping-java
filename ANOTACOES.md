# Poupa Compra Scraping - Java 21 + Spring Boot

Serviço de scraping de NFe (Nota Fiscal Eletrônica)  para Java 21 com Spring Boot.

## 🚀 Tecnologias

- **Java 21** com Virtual Threads habilitadas
- **Spring Boot 3.2+** com suporte nativo a Virtual Threads
- **Playwright for Java** para automação do browser
- **Caffeine Cache** para cache de resultados com TTL de 24h
- **Pool de Browsers** reutilizáveis para melhor performance

## 📋 Pré-requisitos

- Java 21+ (JDK)
- Maven 3.9+
- Docker (opcional, para containerização)

## 🏗️ Estrutura do Projeto

```
src/main/java/br/com/poupacompra/scraping/
├── PoupaCompraScrapingApplication.java  # Classe principal
├── config/
│   ├── CacheConfig.java                 # Configuração do cache Caffeine
│   └── ScrapingProperties.java          # Propriedades externalizadas
├── controller/
│   └── ScrapingController.java          # Endpoints REST
├── dto/
│   ├── DadosNotaResponseDTO.java        # Response principal
│   ├── EstabelecimentoDTO.java          # Dados do estabelecimento
│   ├── ItemNotaDTO.java                 # Item/produto da nota
│   └── NotaDTO.java                     # Dados gerais da nota
├── exception/
│   ├── GlobalExceptionHandler.java      # Handler global de exceções
│   └── ScrapingException.java           # Exceção customizada
└── service/
    ├── BrowserPoolService.java          # Gerenciamento do pool de browsers
    ├── CacheService.java                # Operações de cache
    └── NfeScrapingService.java          # Lógica de scraping
```

## 🔧 Configuração

As configurações estão em `src/main/resources/application.yml`:

```yaml
server:
  port: 8181

spring:
  threads:
    virtual:
      enabled: true  # Habilita Virtual Threads

scraping:
  browser:
    pool-size: 3           # Número de browsers no pool
    headless: true         # Modo headless
    timeout-ms: 25000      # Timeout de elementos
    page-load-timeout-ms: 30000  # Timeout de carregamento
  cache:
    ttl-hours: 24          # TTL do cache
    max-size: 1000         # Máximo de entradas
```

## 🚀 Executando

### Localmente

```bash
# Instalar dependências do Playwright (primeira vez)
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"

# Compilar e executar
mvn spring-boot:run

# Ou compilar JAR e executar
mvn clean package -DskipTests
java --enable-preview -jar target/poupa-compra-scraping-1.0.0.jar
```

### Com Docker

```bash
# Build e run
docker-compose up --build

# Ou apenas build
docker build -t poupa-compra-scraping-java .
docker run -p 8181:8181 poupa-compra-scraping-java
```

## 📡 API Endpoints

### POST /dados-nota
Realiza scraping de uma NFe a partir da URL.

```bash
curl -X POST "http://localhost:8181/dados-nota?url=https://exemplo.sp.gov.br/nfe/..."
```

**Response:**
```json
{
  "estabelecimento": {
    "nomeEstabelecimento": "Mercado XYZ",
    "cpfCnpj": "12345678000199",
    "endereco": "Rua Exemplo, 123"
  },
  "itensNota": [
    {
      "descricao": "Produto A",
      "quantidade": 2.0,
      "tipoUnidade": "UN",
      "valorUnitario": 10.50,
      "valorTotal": 21.00
    }
  ],
  "nota": {
    "quantidadeItens": 1,
    "valorTotal": 21.00,
    "usuario": 3,
    "ufCfe": "SP",
    "urlCfe": "https://exemplo.sp.gov.br/nfe/... via scrapping docker",
    "chaveAcesso": "35210912345678000199... via scrapping docker"
  }
}
```

### POST /cache/clear
Limpa o cache manualmente.

```bash
curl -X POST http://localhost:8181/cache/clear
```

### GET /cache/stats
Retorna estatísticas do cache.

```bash
curl http://localhost:8181/cache/stats
```

## ⚡ Virtual Threads (Java 21)

O projeto utiliza **Virtual Threads** do Java 21 para maximizar a concorrência:

- **Habilitado via** `spring.threads.virtual.enabled=true`
- Cada requisição HTTP roda em uma Virtual Thread
- O pool de browsers usa `BlockingQueue` que é compatível com Virtual Threads
- Permite escalar para milhares de requisições simultâneas com baixo overhead

## 📝 Notas de Migração

1. **Records Java 16+**: DTOs usam `record` para imutabilidade
2. **Text Blocks Java 15+**: Scripts JavaScript usam text blocks `"""`
3. **Pattern Matching**: Usado em algumas partes do código
4. **Builders**: Adicionados para facilitar construção de objetos

## 🐛 Troubleshooting

### Playwright não encontra Chromium
```bash
# Instalar browsers do Playwright
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"
```

### Erro de permissão no Docker
```bash
# Adicionar flags no Dockerfile/docker-compose
--no-sandbox --disable-setuid-sandbox
```

### OutOfMemory
```bash
# Aumentar memória JVM
java -Xmx1g --enable-preview -jar app.jar
```
