@echo off
echo Iniciando infraestrutura para Smart ToDo...

mkdir docker\mosquitto\config 2>nul
mkdir docker\mosquitto\data 2>nul
mkdir docker\mosquitto\log 2>nul

if not exist docker\mosquitto\mosquitto.conf (
    echo listener 1883 > docker\mosquitto\mosquitto.conf
    echo listener 9001 >> docker\mosquitto\mosquitto.conf
    echo protocol websockets >> docker\mosquitto\mosquitto.conf
    echo allow_anonymous true >> docker\mosquitto\mosquitto.conf
)

docker-compose -f docker-compose-dev.yml up -d

echo Infraestrutura iniciada!
echo MQTT Broker: localhost:1883
echo PostgreSQL: localhost:5432
pause
