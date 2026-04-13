import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.Scanner;

public class ClienteMain {

    private static Icorretora corretora;
    private static IClienteCallback callback;
    private static final String IP_SERVIDOR = "172.28.112.1";

    // 1. Variável global e thread-safe para armazenar onde o usuário parou
    public static volatile String ultimoPrompt = "";

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
            System.out.println("4. Cadastrar nova ação");
            System.out.println("5. Remover ação");
            System.out.println("0. Sair");

            // 2. Salva o prompt do menu principal antes de pedir a opção
            ultimoPrompt = "Escolha uma opção: ";
            System.out.print(ultimoPrompt);

            try {
                opcao = Integer.parseInt(scanner.nextLine());
                ultimoPrompt = "";
                processarOpcao(opcao, scanner);
            } catch (NumberFormatException e) {
                System.out.println("Por favor, digite um número válido.");
            } catch (Exception e) {
                System.err.println("[ERRO] Perda de conexão com o servidor!");
                System.out.println("[SISTEMA] Tentando reconectar...");
                conectarAoServidor(); // Tolerância a falhas
            }
        }

        try {
            if (corretora != null) corretora.removerClienteCallback(callback);
            System.out.println("Desconectado com sucesso.");
        } catch (Exception e) {
            // Ignora erro ao fechar se o servidor já estiver fora do ar
        }
        System.exit(0);
    }

    private static void conectarAoServidor() {
        boolean conectado = false;
        while (!conectado) {
            try {
                Registry registry = LocateRegistry.getRegistry(IP_SERVIDOR, 1099);
                corretora = (Icorretora) registry.lookup("ServicoCorretora");

                if (callback == null) {
                    callback = new ClienteCallback();
                }
                corretora.registrarClienteCallback(callback);

                System.out.println("[OK] Conectado à Corretora com sucesso!");
                conectado = true;

            } catch (Exception e) {
                System.err.println("[FALHA] Não foi possível conectar ao servidor. Tentando novamente em 5 segundos...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) { }
            }
        }
    }

    private static void processarOpcao(int opcao, Scanner scanner) throws Exception {
        switch (opcao) {
            case 1:
                // Atualiza o prompt antes de ler o ticker
                ultimoPrompt = "Digite o Ticker da ação (ex: BTC): ";
                System.out.print(ultimoPrompt);
                String ticker = scanner.nextLine();
                ultimoPrompt = "";

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
                Map<String, Double> acoesAtuais = corretora.listarAcoes();

                ultimoPrompt = "Digite o Ticker da ação a ser atualizada: ";
                System.out.print(ultimoPrompt);
                String tickerUpdate = scanner.nextLine().toUpperCase();
                ultimoPrompt = "";

                if(tickerUpdate.isEmpty() || !acoesAtuais.containsKey(tickerUpdate)) {
                    System.out.println("Ticker " + tickerUpdate + " não encontrado");
                    System.out.println("Tickers disponíveis: " + acoesAtuais.keySet());
                } else {
                    ultimoPrompt = "Digite o novo preço: ";
                    System.out.print(ultimoPrompt);
                    double novoPreco = Double.parseDouble(scanner.nextLine());
                    ultimoPrompt = "";

                    corretora.atualizarPreco(tickerUpdate, novoPreco);
                    System.out.println("Preço atualizado com sucesso no servidor!");
                }
                break;

            case 4:
                ultimoPrompt = "Digite o Ticker da nova ação: ";
                System.out.print(ultimoPrompt);
                String newTicker = scanner.nextLine().toUpperCase();
                ultimoPrompt = "";

                if(newTicker.isEmpty() || corretora.listarAcoes().containsKey(newTicker)) {
                    System.out.println("Ticker não pode ser vazio ou já existe!.");
                    break;
                } else {
                    ultimoPrompt = "Digite o preço inicial da ação " + newTicker + ": ";
                    System.out.print(ultimoPrompt);
                    double initialPrice = Double.parseDouble(scanner.nextLine());
                    ultimoPrompt = "";

                    corretora.cadastrarAcao(newTicker, initialPrice);
                    System.out.println("Ação " + newTicker + " cadastrada no servidor com sucesso!");
                }
                break;

            case 5:
                ultimoPrompt = "Digite o Ticker da ação a ser removida: ";
                System.out.print(ultimoPrompt);
                String tickerRemove = scanner.nextLine();
                ultimoPrompt = "";

                corretora.removerAcao(tickerRemove);
                System.out.println("Ação " + tickerRemove + " removida do servidor com sucesso!");
                break;

            case 0:
                System.out.println("Saindo...");
                Thread.sleep(2500);
                break;

            default:
                System.out.println("Opção inválida.");
        }
    }
}