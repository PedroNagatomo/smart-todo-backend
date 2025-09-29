const TodoList = ({ currentMood }) => {
    const [todos, setTodos] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState('all');
    const [showAddModal, setShowAddModal] = useState(false);
    const [newTodo, setNewTodo] = useState({
        title: '',
        description: '',
        priority: 'MEDIUM',
        requiredMood: '',
        cognitiveLoad: 3,
        optimalEnvironment: 'any',
        locationContext: 'anywhere'
    });

    useEffect(() => {
        loadTodos();
    }, [filter]);

    const loadTodos = async () => {
        try {
            setLoading(true);
            const response = await axios.get(`${API_BASE_URL}/todos`);
            let filteredTodos = response.data;

            if (filter === 'active') {
                filteredTodos = filteredTodos.filter(t => t.status === 'PENDING' || t.status === 'IN_PROGRESS');
            } else if (filter === 'completed') {
                filteredTodos = filteredTodos.filter(t => t.status === 'COMPLETED');
            }

            setTodos(filteredTodos);
            setLoading(false);
        } catch (error) {
            console.error('Erro ao carregar tarefas:', error);
            setLoading(false);
        }
    };

    const createTodo = async () => {
        try {
            await axios.post(`${API_BASE_URL}/todos`, newTodo);
            setShowAddModal(false);
            setNewTodo({
                title: '',
                description: '',
                priority: 'MEDIUM',
                requiredMood: '',
                cognitiveLoad: 3,
                optimalEnvironment: 'any',
                locationContext: 'anywhere'
            });
            loadTodos();
        } catch (error) {
            console.error('Erro ao criar tarefa:', error);
            alert('Erro ao criar tarefa!');
        }
    };

    const completeTodo = async (id) => {
        try {
            await axios.patch(`${API_BASE_URL}/todos/${id}/complete`);
            loadTodos();
        } catch (error) {
            console.error('Erro ao completar tarefa:', error);
        }
    };

    const deleteTodo = async (id) => {
        if (confirm('Deseja realmente excluir esta tarefa?')) {
            try {
                await axios.delete(`${API_BASE_URL}/todos/${id}`);
                loadTodos();
            } catch (error) {
                console.error('Erro ao deletar tarefa:', error);
            }
        }
    };

    const getPriorityColor = (priority) => {
        const colors = {
            URGENT: 'bg-red-100 text-red-700 border-red-300',
            HIGH: 'bg-orange-100 text-orange-700 border-orange-300',
            MEDIUM: 'bg-yellow-100 text-yellow-700 border-yellow-300',
            LOW: 'bg-green-100 text-green-700 border-green-300'
        };
        return colors[priority] || colors.MEDIUM;
    };

    return (
        <div className="space-y-6">
            {/* Header com filtros */}
            <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-200">
                <div className="flex items-center justify-between">
                    <div>
                        <h2 className="text-2xl font-bold text-gray-900">Minhas Tarefas</h2>
                        <p className="text-sm text-gray-500 mt-1">
                            {todos.length} tarefas no total
                        </p>
                    </div>

                    <button
                        onClick={() => setShowAddModal(true)}
                        className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center space-x-2 font-semibold"
                    >
                        <i data-lucide="plus" className="w-5 h-5"></i>
                        <span>Nova Tarefa</span>
                    </button>
                </div>

                {/* Filtros */}
                <div className="flex space-x-2 mt-6">
                    {[
                        { value: 'all', label: 'Todas' },
                        { value: 'active', label: 'Ativas' },
                        { value: 'completed', label: 'Concluídas' }
                    ].map(f => (
                        <button
                            key={f.value}
                            onClick={() => setFilter(f.value)}
                            className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                                filter === f.value
                                    ? 'bg-blue-600 text-white'
                                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                            }`}
                        >
                            {f.label}
                        </button>
                    ))}
                </div>
            </div>

            {/* Lista de tarefas */}
            {loading ? (
                <div className="text-center py-12">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
                    <p className="text-gray-600 mt-4">Carregando tarefas...</p>
                </div>
            ) : todos.length === 0 ? (
                <div className="bg-white rounded-xl shadow-sm p-12 text-center border border-gray-200">
                    <i data-lucide="inbox" className="w-16 h-16 mx-auto text-gray-400 mb-4"></i>
                    <h3 className="text-xl font-semibold text-gray-700 mb-2">Nenhuma tarefa encontrada</h3>
                    <p className="text-gray-500">Comece criando sua primeira tarefa!</p>
                </div>
            ) : (
                <div className="space-y-4">
                    {todos.map(todo => (
                        <div
                            key={todo.id}
                            className={`bg-white rounded-xl shadow-sm p-6 border-2 transition-all hover:shadow-md ${
                                todo.status === 'COMPLETED' ? 'opacity-60 border-gray-200' : 'border-gray-200'
                            }`}
                        >
                            <div className="flex items-start justify-between">
                                <div className="flex items-start space-x-4 flex-1">
                                    {/* Checkbox */}
                                    <button
                                        onClick={() => completeTodo(todo.id)}
                                        disabled={todo.status === 'COMPLETED'}
                                        className={`mt-1 w-6 h-6 rounded-full border-2 flex items-center justify-center transition-all ${
                                            todo.status === 'COMPLETED'
                                                ? 'bg-green-500 border-green-500'
                                                : 'border-gray-300 hover:border-blue-500'
                                        }`}
                                    >
                                        {todo.status === 'COMPLETED' && (
                                            <i data-lucide="check" className="w-4 h-4 text-white"></i>
                                        )}
                                    </button>

                                    {/* Conteúdo */}
                                    <div className="flex-1">
                                        <div className="flex items-center space-x-3 mb-2">
                                            <h3 className={`text-lg font-semibold ${
                                                todo.status === 'COMPLETED' ? 'line-through text-gray-500' : 'text-gray-900'
                                            }`}>
                                                {todo.title}
                                            </h3>
                                            <span className={`px-3 py-1 rounded-full text-xs font-semibold ${getPriorityColor(todo.priority)}`}>
                                                {todo.priority}
                                            </span>
                                        </div>

                                        {todo.description && (
                                            <p className="text-gray-600 text-sm mb-3">{todo.description}</p>
                                        )}

                                        {/* Metadata */}
                                        <div className="flex flex-wrap gap-2">
                                            {todo.requiredMood && (
                                                <span className="px-3 py-1 bg-purple-100 text-purple-700 rounded-lg text-xs font-medium">
                                                    <i data-lucide="smile" className="w-3 h-3 inline mr-1"></i>
                                                    {todo.requiredMood}
                                                </span>
                                            )}
                                            {todo.cognitiveLoad && (
                                                <span className="px-3 py-1 bg-blue-100 text-blue-700 rounded-lg text-xs font-medium">
                                                    <i data-lucide="brain" className="w-3 h-3 inline mr-1"></i>
                                                    Carga: {todo.cognitiveLoad}/5
                                                </span>
                                            )}
                                            {todo.locationContext && todo.locationContext !== 'anywhere' && (
                                                <span className="px-3 py-1 bg-green-100 text-green-700 rounded-lg text-xs font-medium">
                                                    <i data-lucide="map-pin" className="w-3 h-3 inline mr-1"></i>
                                                    {todo.locationContext}
                                                </span>
                                            )}
                                            {todo.moodCompatibilityScore > 0 && (
                                                <span className="px-3 py-1 bg-yellow-100 text-yellow-700 rounded-lg text-xs font-medium">
                                                    <i data-lucide="zap" className="w-3 h-3 inline mr-1"></i>
                                                    Match: {(todo.moodCompatibilityScore * 100).toFixed(0)}%
                                                </span>
                                            )}
                                        </div>

                                        {/* Timestamps */}
                                        <div className="mt-3 text-xs text-gray-500">
                                            <span>Criada: {new Date(todo.createdAt).toLocaleDateString()}</span>
                                            {todo.dueDate && (
                                                <span className="ml-4">
                                                    Prazo: {new Date(todo.dueDate).toLocaleDateString()}
                                                </span>
                                            )}
                                        </div>
                                    </div>
                                </div>

                                {/* Ações */}
                                <button
                                    onClick={() => deleteTodo(todo.id)}
                                    className="ml-4 p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                                >
                                    <i data-lucide="trash-2" className="w-5 h-5"></i>
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* Modal de Nova Tarefa */}
            {showAddModal && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-xl shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
                        <div className="p-6 border-b border-gray-200">
                            <div className="flex items-center justify-between">
                                <h3 className="text-2xl font-bold text-gray-900">Nova Tarefa</h3>
                                <button
                                    onClick={() => setShowAddModal(false)}
                                    className="p-2 text-gray-400 hover:text-gray-600 rounded-lg"
                                >
                                    <i data-lucide="x" className="w-6 h-6"></i>
                                </button>
                            </div>
                        </div>

                        <div className="p-6 space-y-4">
                            {/* Título */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Título *
                                </label>
                                <input
                                    type="text"
                                    value={newTodo.title}
                                    onChange={(e) => setNewTodo({...newTodo, title: e.target.value})}
                                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                    placeholder="Ex: Revisar documentação do projeto"
                                />
                            </div>

                            {/* Descrição */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Descrição
                                </label>
                                <textarea
                                    value={newTodo.description}
                                    onChange={(e) => setNewTodo({...newTodo, description: e.target.value})}
                                    rows="3"
                                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                    placeholder="Detalhes da tarefa..."
                                />
                            </div>

                            {/* Grid com configs */}
                            <div className="grid grid-cols-2 gap-4">
                                {/* Prioridade */}
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        Prioridade
                                    </label>
                                    <select
                                        value={newTodo.priority}
                                        onChange={(e) => setNewTodo({...newTodo, priority: e.target.value})}
                                        className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                    >
                                        <option value="LOW">Baixa</option>
                                        <option value="MEDIUM">Média</option>
                                        <option value="HIGH">Alta</option>
                                        <option value="URGENT">Urgente</option>
                                    </select>
                                </div>

                                {/* Humor necessário */}
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        Humor Ideal
                                    </label>
                                    <select
                                        value={newTodo.requiredMood}
                                        onChange={(e) => setNewTodo({...newTodo, requiredMood: e.target.value})}
                                        className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                    >
                                        <option value="">Qualquer</option>
                                        <option value="focused">Focado</option>
                                        <option value="energetic">Energético</option>
                                        <option value="creative">Criativo</option>
                                        <option value="relaxed">Relaxado</option>
                                    </select>
                                </div>

                                {/* Carga cognitiva */}
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        Carga Cognitiva: {newTodo.cognitiveLoad}
                                    </label>
                                    <input
                                        type="range"
                                        min="1"
                                        max="5"
                                        value={newTodo.cognitiveLoad}
                                        onChange={(e) => setNewTodo({...newTodo, cognitiveLoad: parseInt(e.target.value)})}
                                        className="w-full"
                                    />
                                    <div className="flex justify-between text-xs text-gray-500 mt-1">
                                        <span>Simples</span>
                                        <span>Complexo</span>
                                    </div>
                                </div>

                                {/* Ambiente */}
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        Ambiente
                                    </label>
                                    <select
                                        value={newTodo.optimalEnvironment}
                                        onChange={(e) => setNewTodo({...newTodo, optimalEnvironment: e.target.value})}
                                        className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                    >
                                        <option value="any">Qualquer</option>
                                        <option value="quiet">Silencioso</option>
                                        <option value="collaborative">Colaborativo</option>
                                        <option value="bright">Iluminado</option>
                                    </select>
                                </div>
                            </div>

                            {/* Localização */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Localização
                                </label>
                                <select
                                    value={newTodo.locationContext}
                                    onChange={(e) => setNewTodo({...newTodo, locationContext: e.target.value})}
                                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                >
                                    <option value="anywhere">Qualquer lugar</option>
                                    <option value="home">Casa</option>
                                    <option value="office">Escritório</option>
                                    <option value="cafe">Café</option>
                                </select>
                            </div>
                        </div>

                        <div className="p-6 border-t border-gray-200 flex justify-end space-x-3">
                            <button
                                onClick={() => setShowAddModal(false)}
                                className="px-6 py-3 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors font-medium"
                            >
                                Cancelar
                            </button>
                            <button
                                onClick={createTodo}
                                disabled={!newTodo.title}
                                className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                Criar Tarefa
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};