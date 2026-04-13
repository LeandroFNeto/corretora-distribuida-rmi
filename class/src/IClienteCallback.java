import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IClienteCallback extends Remote {
    // Agora o servidor avisa o que é: "CADASTRAR", "ATUALIZAR" ou "REMOVER"
    void notificarAtualizacaoPreco(String tipoEvento, String ticker, double novoPreco) throws RemoteException;
}