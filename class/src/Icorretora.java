import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Icorretora extends Remote {
    //consultas e operações básicas
    double consultarPreco(String ticker) throws RemoteException;
    Map<String, Double> listarAcoes() throws RemoteException;
    void atualizarPreco(String ticker, double novoPreco) throws RemoteException;
    void cadastrarAcao(String ticker, double precoInicial) throws RemoteException;
    void removerAcao(String ticker) throws RemoteException;

    //gerenciamento de Callbacks (Para a notificação em tempo real)
    void registrarClienteCallback(IClienteCallback cliente) throws RemoteException;
    void removerClienteCallback(IClienteCallback cliente) throws RemoteException;
}