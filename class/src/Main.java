import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Main {
    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", ""); //trocar IP para o do servidor

            //instancia o nosso motor da corretora
            Icorretora corretora = new Corretoraimpl();

            //inicia o RMI Registry na porta padrão 1099
            Registry registry = LocateRegistry.createRegistry(1099);

            registry.rebind("ServicoCorretora", corretora);

            System.out.println("[OK] Servidor RMI da Corretora no ar!");
            System.out.println("[INFO] Rodando no IP: " + System.getProperty("java.rmi.server.hostname"));
            System.out.println("[INFO] Aguardando clientes...");

        } catch (Exception e) {
            System.err.println("[ERRO CRÍTICO] O servidor falhou ao iniciar:");
            e.printStackTrace();
        }
    }
}