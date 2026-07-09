# Poupa Compra Scraping - Java 21 + Spring Boot

Serviço de scraping de NFCe para Java 21 com Spring Boot.

## Requisitos

- Java 21
- Maven (ou `./mvnw`)
- Docker + Docker Compose

## Execucao local (sem Docker)

```bash
./mvnw clean package -DskipTests
java --enable-preview -jar target/poupa-compra-scraping-1.0.0.jar
```

Aplicacao disponivel em:

- `http://localhost:8181`

Endpoint principal:

- `POST /dados-nota?url=<url-nfce>`

## Execucao com Docker local

Subir ambiente local com build de debug:

```bash
docker compose -f docker-compose-local.yml up --build -d
```

Parar ambiente:

```bash
docker compose -f docker-compose-local.yml down
```

## Debug visual do browser (somente debug.Dockerfile)

O modo visual do Chromium fica habilitado apenas no fluxo de debug, atraves de:

- `debug.Dockerfile`
- `docker-compose-local.yml`
- `entrypoint.sh` (inicializa Xvfb + x11vnc + noVNC somente quando `SCRAPING_DEBUG_UI=true` e `SCRAPING_BROWSER_HEADLESS=false`)

Variaveis relevantes no modo debug:

- `SCRAPING_DEBUG_UI=true`
- `SCRAPING_BROWSER_HEADLESS=false`
- `SCRAPING_BROWSER_DEBUG_UI_ENABLED=true`

Portas expostas no modo debug:

- `8181` - API
- `5005` - Java debug (JDWP)
- `6080` - noVNC (interface web)
- `5900` - VNC direto

Acesso a interface grafica do browser:

- noVNC: `http://localhost:6080/vnc.html`
- VNC direto: `localhost:5900` (usar cliente VNC; nao abrir no navegador)

Logs da UI dentro do container:

- `/tmp/browser-ui/xvfb.log`
- `/tmp/browser-ui/fluxbox.log`
- `/tmp/browser-ui/x11vnc.log`
- `/tmp/browser-ui/novnc.log`

Comportamento do pool durante scraping:

- Ao finalizar cada requisicao, a instancia usada e reciclada (fecha page/context/browser e cria uma nova instancia limpa com o mesmo ID no pool).
- O pool continua com 3 instancias disponiveis, evitando conexoes penduradas na pagina anterior.

## Healthcheck e observabilidade

- `GET /actuator/health`
- `GET /actuator/metrics`
- `GET /actuator/caches`

## Observacao

O ambiente padrao (fora do `debug.Dockerfile`) deve continuar em modo headless para evitar custo adicional de recursos graficos.