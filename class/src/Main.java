import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Main {
    public static void main(String[] args) {
        try {
            /* * O GRANDE TRUQUE PARA A APRESENTAÇÃO!
             * Descubra o IP do notebook que vai ser o servidor (usando ipconfig no CMD do Windows).
             * Troque o "192.168.0.10" abaixo pelo seu IP real da rede Wi-Fi.
             */
            System.setProperty("java.rmi.server.hostname", "192.168.0.10");

            // 1. Instancia o nosso motor da corretora
            Icorretora corretora = new Corretoraimpl();

            // 2. Inicia o RMI Registry na porta padrão 1099 (não precisa rodar comando por fora)
            Registry registry = LocateRegistry.createRegistry(1099);

            // 3. Apelida o nosso serviço e publica no catálogo
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