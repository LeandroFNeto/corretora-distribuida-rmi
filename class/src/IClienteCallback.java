import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IClienteCallback extends Remote {
    void notificarAtualizacaoPreco(String tipoEvento, String ticker, double novoPreco) throws RemoteException;
}