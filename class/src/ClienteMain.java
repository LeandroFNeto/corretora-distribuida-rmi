import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.Scanner;

public class ClienteMain {

    private static Icorretora corretora;
    private static IClienteCallback callback;
    private static final String IP_SERVIDOR = "192.168.0.10"; // COLOQUE AQUI O MESMO IP DO PASSO 3

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Tenta conectar a primeira vez
        conectarAoServidor();

        int opcao = -1;
        while (opcao != 0) {
            System.out.println("\n--- MENU DA CORRETORA ---");
            System.out.println("1. Consultar preço de uma ação");
            System.out.println("2. Listar todas as ações");
            System.out.println("3. Atualizar preço de uma ação");
            System.out.println("0. Sair");
            System.out.print("Escolha uma opção: ");

            try {
                opcao = Integer.parseInt(scanner.nextLine());
                processarOpcao(opcao, scanner);
            } catch (NumberFormatException e) {
                System.out.println("Por favor, digite um número válido.");
            } catch (Exception e) {
                System.err.println("[ERRO] Perda de conexão com o servidor!");
                System.out.println("[SISTEMA] Tentando reconectar...");
                conectarAoServidor(); // <-- TOLERÂNCIA A FALHAS: Tenta reconectar se a chamada falhar
            }
        }

        // Ao sair, avisa o servidor para parar de mandar mensagens (boa prática)
        try {
            if (corretora != null) corretora.removerClienteCallback(callback);
            System.out.println("Desconectado com sucesso.");
        } catch (Exception e) {
            // Ignora erro ao fechar se o servidor já estiver fora do ar
        }
        System.exit(0);
    }

    // MÉTODO DE CONEXÃO E RECONEXÃO
    private static void conectarAoServidor() {
        boolean conectado = false;
        while (!conectado) {
            try {
                // Procura o catálogo no IP do servidor, porta 1099
                Registry registry = LocateRegistry.getRegistry(IP_SERVIDOR, 1099);
                corretora = (Icorretora) registry.lookup("ServicoCorretora");

                // Cria o callback e avisa o servidor: "Me avise quando mudar algo!"
                if (callback == null) {
                    callback = new ClienteCallback();
                }
                corretora.registrarClienteCallback(callback);

                System.out.println("[OK] Conectado à Corretora com sucesso!");
                conectado = true;

            } catch (Exception e) {
                System.err.println("[FALHA] Não foi possível conectar ao servidor. Tentando novamente em 5 segundos...");
                try {
                    Thread.sleep(5000); // Espera 5 segundos antes de tentar de novo
                } catch (InterruptedException ie) { }
            }
        }
    }

    // PROCESSAMENTO DO MENU
    private static void processarOpcao(int opcao, Scanner scanner) throws Exception {
        switch (opcao) {
            case 1:
                System.out.print("Digite o Ticker da ação (ex: BTC): ");
                String ticker = scanner.nextLine();
                double preco = corretora.consultarPreco(ticker);
                if (preco != -1.0) {
                    System.out.println("Preço atual: " + preco);
                } else {
                    System.out.println("Ativo não encontrado.");
                }
                break;
            case 2:
                Map<String, Double> acoes = corretora.listarAcoes();
                System.out.println("--- AÇÕES DISPONÍVEIS ---");
                for (Map.Entry<String, Double> entry : acoes.entrySet()) {
                    System.out.println(entry.getKey() + " -> " + entry.getValue());
                }
                break;
            case 3:
                System.out.print("Digite o Ticker da ação a ser atualizada: ");
                String tickerUpdate = scanner.nextLine();
                System.out.print("Digite o novo preço: ");
                double novoPreco = Double.parseDouble(scanner.nextLine());
                corretora.atualizarPreco(tickerUpdate, novoPreco);
                System.out.println("Preço atualizado com sucesso no servidor!");
                break;
            case 0:
                System.out.println("Saindo...");
                break;
            default:
                System.out.println("Opção inválida.");
        }
    }
}