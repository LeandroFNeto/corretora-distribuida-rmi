import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClienteCallback extends UnicastRemoteObject implements IClienteCallback {

    public ClienteCallback() throws RemoteException {
        super();
    }

    // Este método é chamado PELO SERVIDOR (roda numa thread separada gerida pelo RMI)
    @Override
    public void notificarAtualizacaoPreco(String ticker, double novoPreco) throws RemoteException {
        System.out.println("\n"); // Pula uma linha para separar do texto que o utilizador já possa ter no ecrã
        System.out.println("========== NOTIFICAÇÃO EM TEMPO REAL ==========");

        if (novoPreco == -1.0){
            System.out.println("[ALERTA] Ativo '" + ticker + "' foi REMOVIDO do mercado.");
        } else {
            System.out.println("[ALERTA DE MERCADO] O ativo '" + ticker + "' mudou de preço! Novo valor: " + novoPreco);
        }

        System.out.println("===============================================");

        // Aqui está a magia: reimprime a pergunta exata onde o utilizador tinha parado
        System.out.print(ClienteMain.ultimoPrompt);
    }
}