const { useState, useEffect } = React;

const API_BASE_URL = 'http://localhost:8080/api';

const App = () => {
    const [activeTab, setActiveTab] = useState('dashboard');
    const [dashboardData, setDashboardData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [lastUpdate, setLastUpdate] = useState(new Date());

    useEffect(() => {
        loadDashboardData();

        const interval = setInterval(() => {
            loadDashboardData();
        }, 5000);

        return () => clearInterval(interval);
    }, []);

    const loadDashboardData = async () => {
        try {
            const [smartResponse, cvResponse, iotResponse] = await Promise.all([
                axios.get(`${API_BASE_URL}/smart/dashboard`),
                axios.get(`${API_BASE_URL}/cv/status`),
                axios.get(`${API_BASE_URL}/iot/status`)
            ]);

            setDashboardData({
                smart: smartResponse.data,
                cv: cvResponse.data,
                iot: iotResponse.data
            });

            setLastUpdate(new Date());
            setLoading(false);
        } catch (error) {
            console.error('Erro ao carregar dados:', error);
            setLoading(false);
        }
    };

    const getMoodColor = (mood) => {
        const colors = {
            focused: 'text-blue-600 bg-blue-100',
            energetic: 'text-orange-600 bg-orange-100',
            creative: 'text-purple-600 bg-purple-100',
            relaxed: 'text-green-600 bg-green-100',
            tired: 'text-gray-600 bg-gray-100',
            stressed: 'text-red-600 bg-red-100',
            neutral: 'text-slate-600 bg-slate-100'
        };
        return colors[mood] || colors.neutral;
    };

    const getMoodEmoji = (mood) => {
        const emojis = {
            focused: '🎯',
            energetic: '⚡',
            creative: '🎨',
            relaxed: '😌',
            tired: '😴',
            stressed: '😰',
            neutral: '😐'
        };
        return emojis[mood] || emojis.neutral;
    };

    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-blue-600 mx-auto mb-4"></div>
                    <p className="text-gray-600 font-medium">Carregando Smart ToDo...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-gray-50 to-blue-50">
            {/* Header */}
            <header className="bg-white shadow-sm border-b border-gray-200">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between items-center py-4">
                        <div className="flex items-center space-x-3">
                            <div className="w-10 h-10 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-lg flex items-center justify-center">
                                <i data-lucide="brain" className="text-white w-6 h-6"></i>
                            </div>
                            <div>
                                <h1 className="text-2xl font-bold text-gray-900">Smart ToDo</h1>
                                <p className="text-sm text-gray-500">Computer Vision + IoT</p>
                            </div>
                        </div>

                        {/* Status atual */}
                        {dashboardData && (
                            <div className="flex items-center space-x-4">
                                {/* Humor atual */}
                                <div className={`px-4 py-2 rounded-lg ${getMoodColor(dashboardData.cv.currentMood)}`}>
                                    <div className="flex items-center space-x-2">
                                        <span className="text-2xl">{getMoodEmoji(dashboardData.cv.currentMood)}</span>
                                        <div>
                                            <p className="text-xs font-medium opacity-75">Humor Atual</p>
                                            <p className="font-semibold capitalize">{dashboardData.cv.currentMood}</p>
                                        </div>
                                    </div>
                                </div>

                                {/* Localização */}
                                <div className="px-4 py-2 bg-green-100 text-green-700 rounded-lg">
                                    <div className="flex items-center space-x-2">
                                        <i data-lucide="map-pin" className="w-5 h-5"></i>
                                        <div>
                                            <p className="text-xs font-medium opacity-75">Localização</p>
                                            <p className="font-semibold capitalize">{dashboardData.iot.currentLocation}</p>
                                        </div>
                                    </div>
                                </div>

                                {/* Última atualização */}
                                <div className="text-sm text-gray-500">
                                    <p className="text-xs">Atualizado</p>
                                    <p className="font-medium">{lastUpdate.toLocaleTimeString()}</p>
                                </div>
                            </div>
                        )}
                    </div>

                    {/* Navigation Tabs */}
                    <nav className="flex space-x-4 mt-2">
                        {[
                            { id: 'dashboard', label: 'Dashboard', icon: 'layout-dashboard' },
                            { id: 'todos', label: 'Tarefas', icon: 'check-square' },
                            { id: 'cv', label: 'Computer Vision', icon: 'eye' },
                            { id: 'iot', label: 'IoT Sensores', icon: 'activity' }
                        ].map(tab => (
                            <button
                                key={tab.id}
                                onClick={() => setActiveTab(tab.id)}
                                className={`flex items-center space-x-2 px-4 py-2 rounded-t-lg transition-all ${
                                    activeTab === tab.id
                                        ? 'bg-white text-blue-600 font-semibold border-b-2 border-blue-600'
                                        : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
                                }`}
                            >
                                <i data-lucide={tab.icon} className="w-4 h-4"></i>
                                <span>{tab.label}</span>
                            </button>
                        ))}
                    </nav>
                </div>
            </header>

            {/* Content */}
            <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {activeTab === 'dashboard' && dashboardData && (
                    <Dashboard data={dashboardData} onRefresh={loadDashboardData} />
                )}
                {activeTab === 'todos' && (
                    <TodoList currentMood={dashboardData?.cv.currentMood} />
                )}
                {activeTab === 'cv' && dashboardData && (
                    <ComputerVision data={dashboardData.cv} onRefresh={loadDashboardData} />
                )}
                {activeTab === 'iot' && dashboardData && (
                    <IoTSensors data={dashboardData.iot} onRefresh={loadDashboardData} />
                )}
            </main>

            {/* Footer */}
            <footer className="mt-12 py-6 bg-white border-t border-gray-200">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center text-gray-500 text-sm">
                    <p>Smart ToDo - Sistema Inteligente de Gerenciamento de Tarefas</p>
                    <p className="mt-1">Computer Vision + IoT + Machine Learning</p>
                </div>
            </footer>
        </div>
    );
};

// Renderiza o App
const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);