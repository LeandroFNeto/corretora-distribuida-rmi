import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClienteCallback extends UnicastRemoteObject implements IClienteCallback {

    public ClienteCallback() throws RemoteException {
        super();
    }

    @Override
    public void notificarAtualizacaoPreco(String tipoEvento, String ticker, double novoPreco) throws RemoteException {
        System.out.println("\n");
        System.out.println("========== NOTIFICAÇÃO EM TEMPO REAL ==========");

        // O switch resolve tudo de forma elegante
        switch (tipoEvento) {
            case "REMOVER":
                System.out.println("[ALERTA] Ativo '" + ticker + "' foi REMOVIDO do mercado.");
                break;
            case "CADASTRAR":
                System.out.println("[ALERTA DE MERCADO] O ativo '" + ticker + "' foi CADASTRADO! Preço: " + novoPreco);
                break;
            case "ATUALIZAR":
                System.out.println("[ALERTA DE MERCADO] O ativo '" + ticker + "' MUDOU de preço! Novo valor: " + novoPreco);
                break;
        }

        System.out.println("===============================================");

        // Só reimprime se a variável existir e não for vazia
        if (ClienteMain.ultimoPrompt != null && !ClienteMain.ultimoPrompt.isEmpty()) {
            System.out.print(ClienteMain.ultimoPrompt);
        }
    }
}