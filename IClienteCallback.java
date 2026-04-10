import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IClienteCallback extends Remote {
    // Método que o Servidor vai chamar no Cliente
    void notificarAtualizacaoPreco(String ticker, double novoPreco) throws RemoteException;
}