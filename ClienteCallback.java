import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClienteCallback extends UnicastRemoteObject implements IClienteCallback {

    private final Queue<String> mensagensPendentes = new ConcurrentLinkedQueue<>();

    public ClienteCallback() throws RemoteException {
        super();
    }

    // Este método é chamado PELO SERVIDOR!
    @Override
    public void notificarAtualizacaoPreco(String ticker, double novoPreco) throws RemoteException {
        String mensagem;
            if (novoPreco == -1.0){
                mensagem = "[ALERTA] Ativo '" + ticker + "' foi REMOVIDO do mercado.";
            }
            else{
                mensagem = "[ALERTA DE MERCADO] O ativo '" + ticker + "' mudou de preço! Novo valor: " + novoPreco;
            }
        mensagensPendentes.add(mensagem);
    }

    public void exibirMensagensPendentes() {
        System.out.println("\n========== NOTIFICAÇÕES RECEBIDAS ==========");
        String msg;
        while((msg = mensagensPendentes.poll()) != null) {
            System.out.println(msg);
        }
        System.out.println("============================================");
    }
}