import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject; 

public class ClienteCallback extends UnicastRemoteObject implements IClienteCallback {

    Corretoraimpl corretora = new Corretoraimpl();

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
        }
        else if(!corretora.listarAcoes().containsKey(ticker)) {
            System.out.println("[ALERTA DE MERCADO] O ativo '" + ticker + "' foi CADASTRADO no mercado! Preço inicial: " + novoPreco);
        }
        else {
            System.out.println("[ALERTA DE MERCADO] O ativo '" + ticker + "' MUDOU de preço! Novo valor: " + novoPreco);
        }

        System.out.println("===============================================");
        System.out.print(ClienteMain.ultimoPrompt);
    }
}