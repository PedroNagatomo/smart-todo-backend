# Sistema avançado de gerenciamento de tarefas que utiliza Computer Vision, IoT e Machine Learning para ajustar automaticamente suas tarefas baseado no seu estado emocional e condições ambientais.
Visão Geral
O Smart ToDo é um sistema inovador que vai além de um gerenciador de tarefas tradicional. Ele monitora continuamente seu humor através da câmera (usando Computer Vision), analisa condições ambientais através de sensores IoT (temperatura, luz, ruído, etc.) e ajusta automaticamente a prioridade das suas tarefas para maximizar sua produtividade.
Principais Funcionalidades

 - Computer Vision: Detecta seu humor em tempo real usando OpenCV
 -  IoT & MQTT: Monitora condições ambientais através de sensores
 -  Ajuste Inteligente: Reorganiza tarefas automaticamente baseado em contexto
 -  Dashboard Interativo: Interface React moderna com visualização em tempo real
 -  Tempo Real: Atualizações instantâneas via polling (5 segundos)

## Arquitetura
Stack Tecnológico
Backend:

- Java 21
- Spring Boot 3.5.6
- Spring Data JPA
- Spring Integration (MQTT)
- PostgreSQL / H2 (desenvolvimento)
- OpenCV 4.7.0 (Computer Vision)

## Frontend:

- React 18 (via CDN)
- TailwindCSS (via CDN)
- Axios (requisições HTTP)
- Lucide Icons

## IoT & Mensageria:

- Eclipse Mosquitto (MQTT Broker)
- Eclipse Paho (MQTT Client)
- Python 3.11 (Simulador IoT)

## Infraestrutura:

- Docker & Docker Compose
- Maven
