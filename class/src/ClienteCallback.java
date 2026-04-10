import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClienteCallback extends UnicastRemoteObject implements IClienteCallback {

    public ClienteCallback() throws RemoteException {
        super();
    }

    // Este método é chamado PELO SERVIDOR e roda em uma thread separada!
    @Override
    public void notificarAtualizacaoPreco(String ticker, double novoPreco) throws RemoteException {
        System.out.println("\n"); // Pula uma linha para não bagunçar se o usuário estiver digitando
        System.out.println("========== NOTIFICAÇÃO EM TEMPO REAL ==========");

        if (novoPreco == -1.0){
            System.out.println("[ALERTA] Ativo '" + ticker + "' foi REMOVIDO do mercado.");
        } else {
            System.out.println("[ALERTA DE MERCADO] O ativo '" + ticker + "' mudou de preço! Novo valor: " + novoPreco);
        }

        System.out.println("===============================================");
        // Truque de UX: reimprime o prompt para o usuário saber que o terminal continua esperando um comando
        System.out.print("Escolha uma opção (ou continue digitando): ");
    }
}