const ComputerVision = ({ data, onRefresh }) => {
    const [analyzing, setAnalyzing] = useState(data.isAnalyzing);

    const toggleAnalysis = async () => {
        try {
            if (analyzing) {
                await axios.post(`${API_BASE_URL}/cv/stop`);
            } else {
                await axios.post(`${API_BASE_URL}/cv/start`);
            }
            setAnalyzing(!analyzing);
            setTimeout(onRefresh, 1000);
        } catch (error) {
            console.error('Erro ao alternar análise:', error);
        }
    };

    const analyzeNow = async () => {
        try {
            await axios.get(`${API_BASE_URL}/cv/mood/analyze`);
            onRefresh();
        } catch (error) {
            console.error('Erro ao analisar:', error);
        }
    };

    const moods = [
        { id: 'focused', emoji: '🎯', label: 'Focado', color: 'blue' },
        { id: 'energetic', emoji: '⚡', label: 'Energético', color: 'orange' },
        { id: 'creative', emoji: '🎨', label: 'Criativo', color: 'purple' },
        { id: 'relaxed', emoji: '😌', label: 'Relaxado', color: 'green' },
        { id: 'tired', emoji: '😴', label: 'Cansado', color: 'gray' },
        { id: 'stressed', emoji: '😰', label: 'Estressado', color: 'red' },
        { id: 'neutral', emoji: '😐', label: 'Neutro', color: 'slate' }
    ];

    const currentMoodData = moods.find(m => m.id === data.currentMood) || moods[6];

    return (
        <div className="space-y-6">
            {/* Status da Câmera */}
            <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
                <div className="flex items-center justify-between mb-6">
                    <div className="flex items-center space-x-3">
                        <div className={`w-12 h-12 rounded-full flex items-center justify-center ${
                            analyzing ? 'bg-green-100 animate-pulse-slow' : 'bg-gray-100'
                        }`}>
                            <i data-lucide="camera" className={`w-6 h-6 ${
                                analyzing ? 'text-green-600' : 'text-gray-400'
                            }`}></i>
                        </div>
                        <div>
                            <h2 className="text-xl font-bold text-gray-900">Computer Vision</h2>
                            <p className="text-sm text-gray-500">
                                {analyzing ? 'Análise ativa' : 'Análise pausada'}
                            </p>
                        </div>
                    </div>

                    <div className="flex space-x-3">
                        <button
                            onClick={analyzeNow}
                            className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center space-x-2 font-medium"
                        >
                            <i data-lucide="scan" className="w-5 h-5"></i>
                            <span>Analisar Agora</span>
                        </button>
                        <button
                            onClick={toggleAnalysis}
                            className={`px-6 py-3 rounded-lg transition-colors flex items-center space-x-2 font-medium ${
                                analyzing
                                    ? 'bg-red-100 text-red-700 hover:bg-red-200'
                                    : 'bg-green-100 text-green-700 hover:bg-green-200'
                            }`}
                        >
                            <i data-lucide={analyzing ? 'pause' : 'play'} className="w-5 h-5"></i>
                            <span>{analyzing ? 'Pausar' : 'Iniciar'}</span>
                        </button>
                    </div>
                </div>

                {/* Status atual grande */}
                <div className={`p-8 rounded-xl bg-gradient-to-br from-${currentMoodData.color}-50 to-${currentMoodData.color}-100 border-2 border-${currentMoodData.color}-200`}>
                    <div className="text-center">
                        <div className="text-8xl mb-4">{currentMoodData.emoji}</div>
                        <h3 className="text-3xl font-bold text-gray-900 mb-2">
                            {currentMoodData.label}
                        </h3>
                        <p className="text-lg text-gray-600">{data.moodDescription}</p>
                        <p className="text-sm text-gray-500 mt-4">
                            Última análise: {new Date(data.lastAnalysis).toLocaleString()}
                        </p>
                    </div>
                </div>
            </div>

            {/* Todos os humores */}
            <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
                <h3 className="text-lg font-bold text-gray-900 mb-4">Estados de Humor</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                    {moods.map(mood => (
                        <div
                            key={mood.id}
                            className={`p-4 rounded-xl border-2 transition-all ${
                                data.currentMood === mood.id
                                    ? `border-${mood.color}-500 bg-${mood.color}-50 shadow-md`
                                    : 'border-gray-200 hover:border-gray-300'
                            }`}
                        >
                            <div className="text-center">
                                <div className="text-4xl mb-2">{mood.emoji}</div>
                                <p className="font-semibold text-gray-900">{mood.label}</p>
                                {data.currentMood === mood.id && (
                                    <span className="inline-block mt-2 px-2 py-1 bg-green-500 text-white text-xs rounded-full font-medium">
                                        Atual
                                    </span>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            {/* Informações técnicas */}
            <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
                <h3 className="text-lg font-bold text-gray-900 mb-4">Informações Técnicas</h3>
                <div className="grid grid-cols-2 gap-4">
                    <div className="p-4 bg-gray-50 rounded-lg">
                        <p className="text-sm text-gray-600">Status da Análise</p>
                        <p className="text-lg font-bold text-gray-900 mt-1">
                            {analyzing ? 'Ativo' : 'Inativo'}
                        </p>
                    </div>
                    <div className="p-4 bg-gray-50 rounded-lg">
                        <p className="text-sm text-gray-600">Humor Detectado</p>
                        <p className="text-lg font-bold text-gray-900 mt-1 capitalize">
                            {data.currentMood}
                        </p>
                    </div>
                    <div className="p-4 bg-gray-50 rounded-lg">
                        <p className="text-sm text-gray-600">Última Análise</p>
                        <p className="text-sm font-medium text-gray-900 mt-1">
                            {new Date(data.lastAnalysis).toLocaleTimeString()}
                        </p>
                    </div>
                    <div className="p-4 bg-gray-50 rounded-lg">
                        <p className="text-sm text-gray-600">Timestamp</p>
                        <p className="text-sm font-medium text-gray-900 mt-1">
                            {new Date(data.timestamp).toLocaleTimeString()}
                        </p>
                    </div>
                </div>
            </div>

            {/* Como funciona */}
            <div className="bg-blue-50 rounded-xl p-6 border border-blue-200">
                <div className="flex items-start space-x-3">
                    <i data-lucide="info" className="w-6 h-6 text-blue-600 flex-shrink-0 mt-1"></i>
                    <div>
                        <h4 className="font-bold text-blue-900 mb-2">Como funciona?</h4>
                        <ul className="space-y-2 text-sm text-blue-800">
                            <li>• Captura frames da câmera a cada 5 segundos</li>
                            <li>• Detecta faces usando OpenCV</li>
                            <li>• Analisa características faciais (brilho, contraste, simetria)</li>
                            <li>• Classifica o humor baseado em heurísticas</li>
                            <li>• Ajusta tarefas automaticamente quando humor muda</li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    );
};