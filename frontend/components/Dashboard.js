const Dashboard = ({ data, onRefresh }) => {
    const { smart, cv, iot } = data;

    return (
        <div className="space-y-6">
            {/* Cards de Estatísticas */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                {/* Total de Tarefas */}
                <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-sm text-gray-500 font-medium">Tarefas Ativas</p>
                            <p className="text-3xl font-bold text-gray-900 mt-2">
                                {smart.totalActiveTasks}
                            </p>
                        </div>
                        <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                            <i data-lucide="list-checks" className="w-6 h-6 text-blue-600"></i>
                        </div>
                    </div>
                </div>

                {/* Tarefas Ajustadas */}
                <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-sm text-gray-500 font-medium">Auto-Ajustadas</p>
                            <p className="text-3xl font-bold text-purple-600 mt-2">
                                {smart.autoAdjustedTasks}
                            </p>
                        </div>
                        <div className="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
                            <i data-lucide="sparkles" className="w-6 h-6 text-purple-600"></i>
                        </div>
                    </div>
                </div>

                {/* Status CV */}
                <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-sm text-gray-500 font-medium">Computer Vision</p>
                            <p className="text-sm font-semibold text-green-600 mt-2">
                                {cv.isAnalyzing ? '🟢 Ativo' : '🔴 Inativo'}
                            </p>
                        </div>
                        <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
                            <i data-lucide="camera" className="w-6 h-6 text-green-600"></i>
                        </div>
                    </div>
                </div>

                {/* Sensores IoT */}
                <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-sm text-gray-500 font-medium">Sensores Ativos</p>
                            <p className="text-3xl font-bold text-orange-600 mt-2">
                                {iot.activeSensorTypes?.length || 0}
                            </p>
                        </div>
                        <div className="w-12 h-12 bg-orange-100 rounded-lg flex items-center justify-center">
                            <i data-lucide="radio" className="w-6 h-6 text-orange-600"></i>
                        </div>
                    </div>
                </div>
            </div>

            {/* Sugestões Inteligentes */}
            <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
                <div className="flex items-center justify-between mb-4">
                    <h2 className="text-xl font-bold text-gray-900 flex items-center space-x-2">
                        <i data-lucide="lightbulb" className="w-6 h-6 text-yellow-500"></i>
                        <span>Sugestões Inteligentes</span>
                    </h2>
                    <button
                        onClick={onRefresh}
                        className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center space-x-2"
                    >
                        <i data-lucide="refresh-cw" className="w-4 h-4"></i>
                        <span>Atualizar</span>
                    </button>
                </div>

                {smart.topSuggestions && smart.topSuggestions.length > 0 ? (
                    <div className="space-y-3">
                        {smart.topSuggestions.map((task, index) => (
                            <div
                                key={task.id}
                                className="flex items-center justify-between p-4 bg-gradient-to-r from-blue-50 to-indigo-50 rounded-lg border border-blue-200"
                            >
                                <div className="flex items-center space-x-4">
                                    <div className="w-8 h-8 bg-blue-600 text-white rounded-full flex items-center justify-center font-bold">
                                        {index + 1}
                                    </div>
                                    <div>
                                        <h3 className="font-semibold text-gray-900">{task.title}</h3>
                                        <p className="text-sm text-gray-600">{task.description}</p>
                                        <div className="flex items-center space-x-3 mt-2">
                                            <span className={`text-xs px-2 py-1 rounded-full font-medium ${
                                                task.priority === 'URGENT' ? 'bg-red-100 text-red-700' :
                                                task.priority === 'HIGH' ? 'bg-orange-100 text-orange-700' :
                                                task.priority === 'MEDIUM' ? 'bg-yellow-100 text-yellow-700' :
                                                'bg-gray-100 text-gray-700'
                                            }`}>
                                                {task.priority}
                                            </span>
                                            {task.moodCompatibilityScore && (
                                                <span className="text-xs text-gray-600">
                                                    Compatibilidade: {(task.moodCompatibilityScore * 100).toFixed(0)}%
                                                </span>
                                            )}
                                        </div>
                                    </div>
                                </div>
                                <div className="text-right">
                                    <div className="text-2xl font-bold text-blue-600">
                                        {task.moodCompatibilityScore ?
                                            (task.moodCompatibilityScore * 100).toFixed(0) + '%' :
                                            'N/A'
                                        }
                                    </div>
                                    <p className="text-xs text-gray-500">Score</p>
                                </div>
                            </div>
                        ))}
                    </div>
                ) : (
                    <div className="text-center py-8 text-gray-500">
                        <i data-lucide="inbox" className="w-12 h-12 mx-auto mb-2 opacity-50"></i>
                        <p>Nenhuma sugestão disponível no momento</p>
                    </div>
                )}
            </div>

            {/* Condições Ambientais */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Humor Atual */}
                <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
                    <h3 className="text-lg font-bold text-gray-900 mb-4 flex items-center space-x-2">
                        <i data-lucide="smile" className="w-5 h-5"></i>
                        <span>Análise de Humor</span>
                    </h3>
                    <div className="space-y-3">
                        <div className="p-4 bg-blue-50 rounded-lg">
                            <p className="text-sm text-gray-600">Humor Detectado</p>
                            <p className="text-2xl font-bold text-blue-600 capitalize mt-1">
                                {cv.currentMood}
                            </p>
                            <p className="text-sm text-gray-500 mt-2">{cv.moodDescription}</p>
                        </div>
                        <div className="text-xs text-gray-500">
                            <p>Última análise: {new Date(cv.lastAnalysis).toLocaleString()}</p>
                        </div>
                    </div>
                </div>

                {/* Condições IoT */}
                <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
                    <h3 className="text-lg font-bold text-gray-900 mb-4 flex items-center space-x-2">
                        <i data-lucide="thermometer" className="w-5 h-5"></i>
                        <span>Ambiente</span>
                    </h3>
                    <div className="space-y-2">
                        {iot.environmentalConditions && Object.keys(iot.environmentalConditions).length > 0 ? (
                            Object.entries(iot.environmentalConditions).map(([sensor, value]) => (
                                <div key={sensor} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                                    <span className="text-sm font-medium text-gray-700 capitalize">
                                        {sensor.replace('_', ' ')}
                                    </span>
                                    <span className="text-sm font-bold text-gray-900">
                                        {typeof value === 'number' ? value.toFixed(1) : value}
                                    </span>
                                </div>
                            ))
                        ) : (
                            <p className="text-sm text-gray-500 text-center py-4">
                                Aguardando dados dos sensores...
                            </p>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};