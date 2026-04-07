import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClienteCallback extends UnicastRemoteObject implements IClienteCallback {

    public ClienteCallback() throws RemoteException {
        super();
    }

    // Este método é chamado PELO SERVIDOR!
    @Override
    public void notificarAtualizacaoPreco(String ticker, double novoPreco) throws RemoteException {
        System.out.println("\n[ALERTA DE MERCADO] -> O ativo " + ticker + " mudou de preço! Novo valor: " + novoPreco);
        System.out.print("Escolha uma opção (1-Consultar, 2-Listar, 3-Atualizar, 0-Sair): "); // Reimprime o prompt para não bagunçar a tela
    }
}