const IoTSensors = ({ data, onRefresh }) => {
    const [sensorHistory, setSensorHistory] = useState([]);
    const [selectedSensor, setSelectedSensor] = useState(null);

    useEffect(() => {
        if (selectedSensor) {
            loadSensorHistory(selectedSensor);
        }
    }, [selectedSensor]);

    const loadSensorHistory = async (sensorType) => {
        try {
            const response = await axios.get(`${API_BASE_URL}/iot/sensor/${sensorType}?hours=1`);
            setSensorHistory(response.data.recentReadings || []);
        } catch (error) {
            console.error('Erro ao carregar histórico:', error);
        }
    };

    const getSensorIcon = (type) => {
        const icons = {
            temperature: 'thermometer',
            humidity: 'droplet',
            light: 'sun',
            noise: 'volume-2',
            air_quality: 'wind',
            motion: 'move',
            presence: 'user'
        };
        return icons[type] || 'activity';
    };

    const getSensorUnit = (type) => {
        const units = {
            temperature: '°C',
            humidity: '%',
            light: 'lux',
            noise: 'dB',
            air_quality: 'AQI',
            motion: 'detection',
            presence: 'boolean'
        };
        return units[type] || '';
    };

    const getSensorStatus = (type, value) => {
        if (type === 'temperature') {
            if (value < 18 || value > 26) return { status: 'warning', color: 'yellow' };
            if (value >= 20 && value <= 24) return { status: 'optimal', color: 'green' };
            return { status: 'good', color: 'blue' };
        }
        if (type === 'humidity') {
            if (value < 30 || value > 70) return { status: 'warning', color: 'yellow' };
            if (value >= 40 && value <= 60) return { status: 'optimal', color: 'green' };
            return { status: 'good', color: 'blue' };
        }
        if (type === 'light') {
            if (value < 200) return { status: 'low', color: 'yellow' };
            if (value >= 300 && value <= 800) return { status: 'optimal', color: 'green' };
            return { status: 'high', color: 'orange' };
        }
        if (type === 'noise') {
            if (value > 60) return { status: 'high', color: 'red' };
            if (value <= 40) return { status: 'optimal', color: 'green' };
            return { status: 'moderate', color: 'yellow' };
        }
        if (type === 'air_quality') {
            if (value > 100) return { status: 'poor', color: 'red' };
            if (value <= 50) return { status: 'optimal', color: 'green' };
            return { status: 'moderate', color: 'yellow' };
        }
        return { status: 'unknown', color: 'gray' };
    };

    const latestSensorData = data.latestSensorData || {};
    const sensors = Object.keys(latestSensorData);

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
                <div className="flex items-center justify-between">
                    <div>
                        <h2 className="text-2xl font-bold text-gray-900">Sensores IoT</h2>
                        <p className="text-sm text-gray-500 mt-1">
                            {sensors.length} sensores ativos
                        </p>
                    </div>
                    <button
                        onClick={onRefresh}
                        className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center space-x-2 font-medium"
                    >
                        <i data-lucide="refresh-cw" className="w-5 h-5"></i>
                        <span>Atualizar</span>
                    </button>
                </div>
            </div>

            {/* Localização atual */}
            <div className="bg-gradient-to-r from-green-50 to-emerald-50 rounded-xl p-6 border border-green-200">
                <div className="flex items-center space-x-4">
                    <div className="w-16 h-16 bg-green-500 rounded-full flex items-center justify-center">
                        <i data-lucide="map-pin" className="w-8 h-8 text-white"></i>
                    </div>
                    <div>
                        <p className="text-sm text-green-700 font-medium">Localização Atual</p>
                        <p className="text-2xl font-bold text-green-900 capitalize">{data.currentLocation}</p>
                    </div>
                </div>
            </div>

            {/* Grid de sensores */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {sensors.length > 0 ? sensors.map(sensorType => {
                    const sensorData = latestSensorData[sensorType];
                    const status = getSensorStatus(sensorType, sensorData.value);

                    return (
                        <div
                            key={sensorType}
                            onClick={() => setSelectedSensor(sensorType)}
                            className={`bg-white rounded-xl shadow-sm p-6 border-2 cursor-pointer transition-all hover:shadow-md ${
                                selectedSensor === sensorType
                                    ? 'border-blue-500'
                                    : 'border-gray-200'
                            }`}
                        >
                            <div className="flex items-start justify-between mb-4">
                                <div className={`w-12 h-12 bg-${status.color}-100 rounded-lg flex items-center justify-center`}>
                                    <i data-lucide={getSensorIcon(sensorType)} className={`w-6 h-6 text-${status.color}-600`}></i>
                                </div>
                                <span className={`px-3 py-1 bg-${status.color}-100 text-${status.color}-700 rounded-full text-xs font-semibold uppercase`}>
                                    {status.status}
                                </span>
                            </div>

                            <h3 className="text-lg font-semibold text-gray-900 capitalize mb-2">
                                {sensorType.replace('_', ' ')}
                            </h3>

                            <div className="flex items-baseline space-x-2">
                                <span className="text-4xl font-bold text-gray-900">
                                    {sensorData.value.toFixed(1)}
                                </span>
                                <span className="text-lg text-gray-500">
                                    {sensorData.unit}
                                </span>
                            </div>

                            <div className="mt-4 pt-4 border-t border-gray-100">
                                <p className="text-xs text-gray-500">
                                    Atualizado: {new Date(sensorData.timestamp).toLocaleTimeString()}
                                </p>
                            </div>
                        </div>
                    );
                }) : (
                    <div className="col-span-3 text-center py-12">
                        <i data-lucide="radio-tower" className="w-16 h-16 mx-auto text-gray-400 mb-4"></i>
                        <p className="text-gray-600">Nenhum sensor ativo</p>
                        <p className="text-sm text-gray-500 mt-2">Aguardando dados dos sensores...</p>
                    </div>
                )}
            </div>

            {/* Histórico do sensor selecionado */}
            {selectedSensor && sensorHistory.length > 0 && (
                <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
                    <div className="flex items-center justify-between mb-6">
                        <h3 className="text-lg font-bold text-gray-900 capitalize">
                            Histórico: {selectedSensor.replace('_', ' ')}
                        </h3>
                        <button
                            onClick={() => setSelectedSensor(null)}
                            className="text-gray-500 hover:text-gray-700"
                        >
                            <i data-lucide="x" className="w-5 h-5"></i>
                        </button>
                    </div>

                    <div className="space-y-2">
                        {sensorHistory.slice(0, 10).map((reading, index) => (
                            <div key={reading.id || index} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                                <span className="text-sm text-gray-600">
                                    {new Date(reading.timestamp).toLocaleTimeString()}
                                </span>
                                <div className="flex items-center space-x-2">
                                    <span className="text-lg font-bold text-gray-900">
                                        {reading.value.toFixed(1)}
                                    </span>
                                    <span className="text-sm text-gray-500">
                                        {reading.unit}
                                    </span>
                                    <span className={`px-2 py-1 rounded text-xs font-medium ${
                                        reading.quality === 'good' ? 'bg-green-100 text-green-700' :
                                        reading.quality === 'fair' ? 'bg-yellow-100 text-yellow-700' :
                                        'bg-red-100 text-red-700'
                                    }`}>
                                        {reading.quality}
                                    </span>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* Condições ambientais consolidadas */}
            <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
                <h3 className="text-lg font-bold text-gray-900 mb-4">Resumo Ambiental</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                    {data.environmentalConditions && Object.keys(data.environmentalConditions).length > 0 ? (
                        Object.entries(data.environmentalConditions).map(([key, value]) => (
                            <div key={key} className="p-4 bg-gradient-to-br from-gray-50 to-gray-100 rounded-lg">
                                <p className="text-xs text-gray-600 capitalize mb-1">
                                    {key.replace('_', ' ')}
                                </p>
                                <p className="text-2xl font-bold text-gray-900">
                                    {typeof value === 'number' ? value.toFixed(1) : value}
                                </p>
                            </div>
                        ))
                    ) : (
                        <div className="col-span-4 text-center py-8 text-gray-500">
                            <p>Aguardando dados ambientais...</p>
                        </div>
                    )}
                </div>
            </div>

            {/* Informações */}
            <div className="bg-blue-50 rounded-xl p-6 border border-blue-200">
                <div className="flex items-start space-x-3">
                    <i data-lucide="info" className="w-6 h-6 text-blue-600 flex-shrink-0 mt-1"></i>
                    <div>
                        <h4 className="font-bold text-blue-900 mb-2">Sistema IoT Ativo</h4>
                        <ul className="space-y-2 text-sm text-blue-800">
                            <li>• Sensores transmitindo via MQTT em tempo real</li>
                            <li>• Dados armazenados no PostgreSQL para histórico</li>
                            <li>• Ajuste automático de tarefas baseado nas condições</li>
                            <li>• Comandos smart home enviados automaticamente</li>
                            <li>• Análise de padrões ambientais ao longo do tempo</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    );
};