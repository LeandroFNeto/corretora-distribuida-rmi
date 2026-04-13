import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

public class Corretoraimpl extends UnicastRemoteObject implements Icorretora {

    // ConcurrentHashMap garante thread-safety (concorrência)
    private Map<String, Double> acoes;

    // CopyOnWriteArrayList evita ConcurrentModificationException ao notificar clientes
    private List<IClienteCallback> clientesConectados;

    public Corretoraimpl() throws RemoteException {
        super();
        acoes = new ConcurrentHashMap<>();
        clientesConectados = new CopyOnWriteArrayList<>();

        // Cadastrando os ativos iniciais
        acoes.put("BTC", 350000.0);
        acoes.put("ETH", 18000.0);
        acoes.put("SOL", 800.0);
    }

    @Override
    public double consultarPreco(String ticker) throws RemoteException {
        System.out.println("[LOG] Consulta recebida para: " + ticker);
        return acoes.getOrDefault(ticker.toUpperCase(), -1.0);
    }

    @Override
    public Map<String, Double> listarAcoes() throws RemoteException {
        System.out.println("[LOG] Listagem de ações solicitada.");
        return acoes;
    }

    // ... restante do código (consultar, listar, etc) fica igual ...

    @Override
    public void atualizarPreco(String ticker, double novoPreco) throws RemoteException {
        String tickerUpper = ticker.toUpperCase();
        if (acoes.containsKey(tickerUpper)) {
            acoes.put(tickerUpper, novoPreco);
            System.out.println("[LOG] Preço atualizado: " + tickerUpper + " -> " + novoPreco);
            notificarTodosClientes("ATUALIZAR", tickerUpper, novoPreco); // <-- MUDOU AQUI
        } else {
            System.out.println("[LOG] Tentativa de atualizar ativo inexistente: " + tickerUpper);
        }
    }

    @Override
    public void cadastrarAcao(String ticker, double precoInicial) throws RemoteException {
        String tickerUpper = ticker.toUpperCase().trim();
        if (!acoes.containsKey(tickerUpper)) {
            acoes.put(tickerUpper, precoInicial);
            System.out.println("[LOG] Nova ação cadastrada: " + tickerUpper + " -> " + precoInicial);
            notificarTodosClientes("CADASTRAR", tickerUpper, precoInicial); // <-- MUDOU AQUI
        } else {
            System.out.println("[LOG] Tentativa de cadastrar ativo já existente: " + tickerUpper);
        }
    }

    @Override
    public void removerAcao(String ticker) throws RemoteException {
        String tickerUpper = ticker.toUpperCase();
        if (acoes.containsKey(tickerUpper)) {
            acoes.remove(tickerUpper);
            System.out.println("[LOG] Ação removida: " + tickerUpper);
            notificarTodosClientes("REMOVER", tickerUpper, -1.0); // <-- MUDOU AQUI
        } else {
            System.out.println("[LOG] Tentativa de remover ativo inexistente: " + tickerUpper);
        }
    }
    @Override
    public void registrarClienteCallback(IClienteCallback cliente) throws RemoteException {
        clientesConectados.add(cliente);
        System.out.println("[LOG] Novo cliente registrado para callbacks. Total: " + clientesConectados.size());
    }

    @Override
    public void removerClienteCallback(IClienteCallback cliente) throws RemoteException {
        clientesConectados.remove(cliente);
        System.out.println("[LOG] Cliente removido dos callbacks.");
    }

    // Método interno atualizado para receber a String
    private void notificarTodosClientes(String tipoEvento, String ticker, double novoPreco) {
        for (IClienteCallback cliente : clientesConectados) {
            try {
                cliente.notificarAtualizacaoPreco(tipoEvento, ticker, novoPreco);
            } catch (RemoteException e) {
                System.err.println("[AVISO] Falha ao notificar cliente. Removendo da lista.");
                clientesConectados.remove(cliente);
            }
        }
    }
}