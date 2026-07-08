docker compose -f docker-compose-local.yml build
echo "Build realizado"
docker compose -f docker-compose-local.yml down
echo "Containers parados"

sleep 5

echo "Iniciando Containers"
docker compose -f docker-compose-local.yml up -d
