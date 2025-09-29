import json
import time
import random
import schedule
import threading
from datetime import datetime, timedelta
from paho.mqtt.client import Client as MQTTClient
from faker import Faker
import numpy as np
import os
from colorama import init, Fore, Style

# Inicializa colorama para output colorido
init()

fake = Faker('pt_BR')

class SmartToDoIoTSimulator:
    def __init__(self):
        # Configurações MQTT
        self.mqtt_client = MQTTClient()
        self.mqtt_host = os.getenv('MQTT_BROKER_HOST', 'localhost')
        self.mqtt_port = int(os.getenv('MQTT_BROKER_PORT', 1883))
        self.simulation_interval = int(os.getenv('SIMULATION_INTERVAL', 10))

        # Estado atual do ambiente/usuário
        self.environment_state = {
            'location': 'home',
            'activity': 'working',
            'time_of_day': 'morning',
            'weather': 'sunny',
            'user_present': True,
            'last_movement': datetime.now()
        }

        # Configuração dos sensores com valores realistas
        self.sensors = {
            'temperature': {
                'min': 16, 'max': 32, 'current': 22.5,
                'unit': '°C', 'variance': 0.5
            },
            'humidity': {
                'min': 25, 'max': 85, 'current': 45.0,
                'unit': '%', 'variance': 2.0
            },
            'light': {
                'min': 10, 'max': 2000, 'current': 450.0,
                'unit': 'lux', 'variance': 50.0
            },
            'noise': {
                'min': 15, 'max': 95, 'current': 35.0,
                'unit': 'dB', 'variance': 3.0
            },
            'air_quality': {
                'min': 0, 'max': 500, 'current': 45.0,
                'unit': 'AQI', 'variance': 5.0
            },
            'motion': {
                'min': 0, 'max': 1, 'current': 0,
                'unit': 'detection', 'variance': 0
            },
            'presence': {
                'min': 0, 'max': 1, 'current': 1,
                'unit': 'boolean', 'variance': 0
            }
        }

        self.setup_mqtt()
        self.running = False

    def setup_mqtt(self):
        """Configura cliente MQTT"""
        def on_connect(client, userdata, flags, rc):
            if rc == 0:
                print(f"{Fore.GREEN}✅ Conectado ao broker MQTT: {self.mqtt_host}:{self.mqtt_port}{Style.RESET_ALL}")
                client.subscribe("smarthome/commands")
            else:
                print(f"{Fore.RED}❌ Falha na conexão MQTT: código {rc}{Style.RESET_ALL}")

        def on_disconnect(client, userdata, rc):
            print(f"{Fore.YELLOW}🔌 Desconectado do broker MQTT{Style.RESET_ALL}")

        def on_message(client, userdata, msg):
            try:
                command = json.loads(msg.payload.decode())
                print(f"{Fore.CYAN}📡 Comando recebido: {command}{Style.RESET_ALL}")
                self.handle_smart_home_command(command)
            except Exception as e:
                print(f"{Fore.RED}❌ Erro ao processar comando: {e}{Style.RESET_ALL}")

        self.mqtt_client.on_connect = on_connect
        self.mqtt_client.on_disconnect = on_disconnect
        self.mqtt_client.on_message = on_message

        try:
            self.mqtt_client.connect(self.mqtt_host, self.mqtt_port, 60)
            self.mqtt_client.loop_start()
        except Exception as e:
            print(f"{Fore.RED}❌ Erro ao conectar MQTT: {e}{Style.RESET_ALL}")

    def simulate_realistic_sensor_data(self):
        """Simula dados realistas dos sensores"""
        current_time = datetime.now()
        hour = current_time.hour

        # Atualiza estado do ambiente baseado na hora
        self.update_environment_state(hour)

        print(f"\n{Fore.BLUE}🔄 Simulando dados dos sensores - {current_time.strftime('%H:%M:%S')}{Style.RESET_ALL}")

        for sensor_type, config in self.sensors.items():
            if sensor_type in ['motion', 'presence']:
                new_value = self.simulate_boolean_sensor(sensor_type)
            else:
                new_value = self.simulate_continuous_sensor(sensor_type, config, hour)

            sensor_data = {
                'sensor_id': f"{sensor_type}_001",
                'value': new_value,
                'unit': config['unit'],
                'timestamp': current_time.isoformat(),
                'location': self.environment_state['location'],
                'quality': random.choices(['good', 'fair', 'poor'], weights=[0.8, 0.15, 0.05])[0]
            }

            topic = f"sensors/{sensor_type}/data"
            self.publish_sensor_data(topic, sensor_data)

            # Mostra valor no console com cor baseada no status
            status_color = self.get_status_color(sensor_type, new_value)
            print(f"   {status_color}{sensor_type}: {new_value} {config['unit']}{Style.RESET_ALL}")

    def update_environment_state(self, hour):
        """Atualiza estado do ambiente baseado na hora"""
        # Define período do dia
        if 6 <= hour < 12:
            self.environment_state['time_of_day'] = 'morning'
            self.environment_state['activity'] = 'morning_routine'
        elif 12 <= hour < 18:
            self.environment_state['time_of_day'] = 'afternoon'
            self.environment_state['activity'] = 'working'
        elif 18 <= hour < 22:
            self.environment_state['time_of_day'] = 'evening'
            self.environment_state['activity'] = 'leisure'
        else:
            self.environment_state['time_of_day'] = 'night'
            self.environment_state['activity'] = 'sleeping'

    def simulate_continuous_sensor(self, sensor_type, config, hour):
        """Simula sensores contínuos (temperatura, umidade, etc.)"""
        base_value = config['current']
        variance = config['variance']

        # Adiciona variação natural
        natural_change = random.uniform(-variance, variance)

        # Adiciona padrões baseados na hora e atividade
        contextual_change = self.get_contextual_change(sensor_type, hour)

        new_value = base_value + natural_change + contextual_change

        # Mantém dentro dos limites
        new_value = max(config['min'], min(config['max'], new_value))

        # Atualiza valor atual para próxima iteração
        config['current'] = new_value

        return round(new_value, 1)

    def simulate_boolean_sensor(self, sensor_type):
        """Simula sensores booleanos (movimento, presença)"""
        if sensor_type == 'motion':
            # Simula movimento baseado na atividade
            activity = self.environment_state['activity']
            if activity == 'sleeping':
                probability = 0.05
            elif activity == 'working':
                probability = 0.3
            else:
                probability = 0.6

            return 1 if random.random() < probability else 0

        elif sensor_type == 'presence':
            # Simula presença baseada na hora e atividade
            hour = datetime.now().hour
            if 23 <= hour or hour <= 6:  # Noite
                probability = 0.9
            elif 9 <= hour <= 17:  # Horário comercial
                probability = 0.7
            else:
                probability = 0.8

            return 1 if random.random() < probability else 0

        return 0

    def get_contextual_change(self, sensor_type, hour):
        """Calcula mudanças baseadas no contexto"""
        activity = self.environment_state['activity']
        location = self.environment_state['location']

        contextual_change = 0

        if sensor_type == 'temperature':
            # Temperatura varia com hora do dia
            if 6 <= hour <= 18:  # Dia
                contextual_change += random.uniform(1, 3)
            else:  # Noite
                contextual_change -= random.uniform(1, 2)

            # Aquecimento/ar condicionado baseado na atividade
            if activity == 'working':
                contextual_change += random.uniform(-1, 1)

        elif sensor_type == 'humidity':
            # Umidade varia inversamente com temperatura
            if location == 'bathroom':
                contextual_change += random.uniform(10, 25)
            elif activity == 'cooking':
                contextual_change += random.uniform(5, 15)

        elif sensor_type == 'light':
            # Luz natural durante o dia
            if 6 <= hour <= 20:
                contextual_change += random.uniform(100, 500)
            else:
                contextual_change -= random.uniform(50, 200)

            # Iluminação artificial baseada na atividade
            if activity == 'working':
                contextual_change += random.uniform(50, 150)

        elif sensor_type == 'noise':
            # Ruído baseado na atividade e hora
            if activity == 'working' and 9 <= hour <= 17:
                contextual_change += random.uniform(5, 15)
            elif hour >= 22 or hour <= 6:  # Noite silenciosa
                contextual_change -= random.uniform(5, 15)

        elif sensor_type == 'air_quality':
            # Qualidade do ar baseada na ventilação e atividade
            if activity == 'cooking':
                contextual_change += random.uniform(10, 30)
            elif location == 'office' and 9 <= hour <= 17:
                contextual_change += random.uniform(5, 15)

        return contextual_change

    def simulate_location_changes(self):
        """Simula mudanças de localização do usuário"""
        hour = datetime.now().hour
        current_location = self.environment_state['location']

        # Probabilidades de localização baseada na hora
        location_probabilities = self.get_location_probabilities(hour)

        # Chance de mudança baseada na hora
        change_probability = self.get_location_change_probability(hour)

        if random.random() < change_probability:
            new_location = random.choices(
                list(location_probabilities.keys()),
                weights=list(location_probabilities.values())
            )[0]

            if new_location != current_location:
                self.environment_state['location'] = new_location

                location_data = {
                    'user_id': 'user_001',
                    'location': new_location,
                    'previous_location': current_location,
                    'confidence': round(random.uniform(0.85, 0.98), 2),
                    'timestamp': datetime.now().isoformat(),
                    'detection_method': random.choice(['gps', 'wifi', 'bluetooth', 'beacon'])
                }

                topic = "location/user/001"
                self.publish_sensor_data(topic, location_data)

                print(f"{Fore.MAGENTA}📍 Mudança de localização: {current_location} → {new_location}{Style.RESET_ALL}")

    def get_location_probabilities(self, hour):
        """Retorna probabilidades de localização baseada na hora"""
        if 7 <= hour <= 8:  # Manhã - transição
            return {'home': 0.4, 'commute': 0.4, 'office': 0.2}
        elif 9 <= hour <= 17:  # Horário comercial
            return {'office': 0.7, 'cafe': 0.1, 'home': 0.1, 'meeting_room': 0.1}
        elif 17 <= hour <= 19:  # Fim do dia
            return {'commute': 0.4, 'office': 0.3, 'home': 0.2, 'gym': 0.1}
        elif 19 <= hour <= 22:  # Noite
            return {'home': 0.8, 'restaurant': 0.1, 'friend_house': 0.1}
        else:  # Madrugada
            return {'home': 0.95, 'hospital': 0.02, 'travel': 0.03}

    def get_location_change_probability(self, hour):
        """Retorna probabilidade de mudança de localização"""
        if 7 <= hour <= 9 or 17 <= hour <= 19:  # Horários de deslocamento
            return 0.7
        elif 9 <= hour <= 17:  # Horário comercial
            return 0.2
        elif 22 <= hour or hour <= 6:  # Madrugada
            return 0.05
        else:
            return 0.3

    def handle_smart_home_command(self, command):
        """Processa comandos de casa inteligente"""
        device = command.get('device', '')
        action = command.get('action', '')
        parameters = command.get('parameters', {})

        print(f"{Fore.GREEN}🏠 Executando comando Smart Home:{Style.RESET_ALL}")
        print(f"   Dispositivo: {device}")
        print(f"   Ação: {action}")
        print(f"   Parâmetros: {parameters}")

        # Simula efeito nos sensores
        if device == 'climate':
            if action == 'cool':
                target_temp = parameters.get('target_temperature', 24)
                self.sensors['temperature']['current'] = target_temp
                print(f"   🌡️ Temperatura ajustada para {target_temp}°C")

        elif device == 'lights':
            if action == 'brighten':
                brightness = parameters.get('brightness', 80)
                light_increase = brightness * 10  # Conversão aproximada para lux
                self.sensors['light']['current'] += light_increase
                print(f"   💡 Iluminação aumentada em {light_increase} lux")

        elif device == 'air_purifier':
            if action == 'on':
                # Melhora qualidade do ar gradualmente
                current_aqi = self.sensors['air_quality']['current']
                self.sensors['air_quality']['current'] = max(0, current_aqi - 20)
                print(f"   🌪️ Purificador de ar ligado - AQI reduzido")

    def publish_sensor_data(self, topic, data):
        """Publica dados no broker MQTT"""
        try:
            payload = json.dumps(data, ensure_ascii=False, default=str)
            self.mqtt_client.publish(topic, payload)
        except Exception as e:
            print(f"{Fore.RED}❌ Erro ao publicar {topic}: {e}{Style.RESET_ALL}")

    def get_status_color(self, sensor_type, value):
        """Retorna cor baseada no status do sensor"""
        if sensor_type == 'temperature':
            if 20 <= value <= 24:
                return Fore.GREEN
            elif 18 <= value <= 26:
                return Fore.YELLOW
            else:
                return Fore.RED
        elif sensor_type == 'humidity':
            if 40 <= value <= 60:
                return Fore.GREEN
            elif 30 <= value <= 70:
                return Fore.YELLOW
            else:
                return Fore.RED
        elif sensor_type == 'air_quality':
            if value <= 50:
                return Fore.GREEN
            elif value <= 100:
                return Fore.YELLOW
            else:
                return Fore.RED
        else:
            return Fore.WHITE

    def print_status(self):
        """Mostra status atual do simulador"""
        print(f"\n{Fore.CYAN}📊 Status do Simulador IoT{Style.RESET_ALL}")
        print(f"   Localização: {self.environment_state['location']}")
        print(f"   Atividade: {self.environment_state['activity']}")
        print(f"   Período: {self.environment_state['time_of_day']}")
        print(f"   Sensores ativos: {len(self.sensors)}")
        print(f"   Intervalo: {self.simulation_interval}s")

    def run_simulation(self):
        """Executa simulação principal"""
        print(f"{Fore.BLUE}🚀 Iniciando Simulador IoT Smart ToDo{Style.RESET_ALL}")
        self.print_status()

        # Agenda tarefas
        schedule.every(self.simulation_interval).seconds.do(self.simulate_realistic_sensor_data)
        schedule.every(45).seconds.do(self.simulate_location_changes)
        schedule.every(5).minutes.do(self.print_status)

        self.running = True

        try:
            while self.running:
                schedule.run_pending()
                time.sleep(1)
        except KeyboardInterrupt:
            print(f"\n{Fore.YELLOW}⏹️ Parando simulação...{Style.RESET_ALL}")
        finally:
            self.stop_simulation()

    def stop_simulation(self):
        """Para a simulação"""
        self.running = False
        self.mqtt_client.loop_stop()
        self.mqtt_client.disconnect()
        print(f"{Fore.RED}🔴 Simulação parada{Style.RESET_ALL}")

if __name__ == "__main__":
    print(f"{Fore.BLUE}Smart ToDo IoT Simulator v1.0{Style.RESET_ALL}")
    simulator = SmartToDoIoTSimulator()
    simulator.run_simulation()