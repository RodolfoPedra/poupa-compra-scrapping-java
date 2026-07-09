#!/bin/sh
set -eu

export DISPLAY="${DISPLAY:-:99}"
export XVFB_WHD="${XVFB_WHD:-1280x720x24}"
export VNC_PORT="${VNC_PORT:-5900}"
export NOVNC_PORT="${NOVNC_PORT:-6080}"
export SCRAPING_DEBUG_UI="${SCRAPING_DEBUG_UI:-false}"

if [ "$SCRAPING_DEBUG_UI" = "true" ] && [ "${SCRAPING_BROWSER_HEADLESS:-true}" = "false" ]; then
  echo "Iniciando ambiente gráfico de debug para o browser..."

  log_dir="/tmp/browser-ui"
  mkdir -p "$log_dir"

  Xvfb "$DISPLAY" -screen 0 "$XVFB_WHD" >"$log_dir/xvfb.log" 2>&1 &
  fluxbox >"$log_dir/fluxbox.log" 2>&1 &
  x11vnc -display "$DISPLAY" -forever -shared -nopw -listen 0.0.0.0 -rfbport "$VNC_PORT" >"$log_dir/x11vnc.log" 2>&1 &
  websockify --web=/usr/share/novnc 0.0.0.0:"$NOVNC_PORT" localhost:"$VNC_PORT" >"$log_dir/novnc.log" 2>&1 &

  echo "UI de debug habilitada"
  echo "NoVNC: http://localhost:${NOVNC_PORT}/vnc.html"
  echo "VNC direto: localhost:${VNC_PORT}"
  echo "Logs da UI: ${log_dir}"
fi

jar_file=""
for candidate in \
  /app/poupa-compra-scraping-1.0.0.jar \
  /app/poupa-compra-scrapping-1.0.0.jar \
  /app/target/poupa-compra-scraping-1.0.0.jar \
  /app/target/poupa-compra-scrapping-1.0.0.jar; do
  if [ -f "$candidate" ]; then
    jar_file="$candidate"
    break
  fi
done

if [ -z "$jar_file" ]; then
  echo "Nenhum JAR da aplicação foi encontrado em /app" >&2
  exit 1
fi

echo "Iniciando aplicação com ${jar_file}"
exec java $JAVA_OPTS $JAVA_DEBUG_OPTS -jar "$jar_file"
