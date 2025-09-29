@echo off
echo Instalando dependências Python...
pip install -r requirements.txt

echo Iniciando simulador IoT...
python simulator.py
pause