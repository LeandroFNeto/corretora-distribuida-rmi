import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Icorretora extends Remote {
    // Consultas e Operações Básicas
    double consultarPreco(String ticker) throws RemoteException;
    Map<String, Double> listarAcoes() throws RemoteException;
    void atualizarPreco(String ticker, double novoPreco) throws RemoteException;

    // Gerenciamento de Callbacks (Para a notificação em tempo real)
    void registrarClienteCallback(IClienteCallback cliente) throws RemoteException;
    void removerClienteCallback(IClienteCallback cliente) throws RemoteException;
}