# Teste de Java RMI para sistemas distribuídos.

Primeiro Fizemos  duas interface uma para definir qual função obrigatória do cliente e funções obrigatória do corretora da corretora sendo
* **Icorretora** = com métodos consultarpreco, listaracoes, atualizaracoes, registarclientecallback e removerclientecallback.


```java
public interface ICorretora extends Remote {
    double consultarPreco(String ticker) throws RemoteException;
    Map<String, Double> listarAcoes() throws RemoteException;
    void atualizarPreco(String ticker, double novoPreco) throws RemoteException;
    
    // Callbacks para atualização em tempo real
    void registrarClienteCallback(IClienteCallback cliente) throws RemoteException;
    void removerClienteCallback(IClienteCallback cliente) throws RemoteException;
}
````
* **IclienteCallback** = com método notificaratualizacaodepreco.

```java
public interface IClienteCallback extends Remote {
    void notificarAtualizacaoPreco(String ticker, double novoPreco) throws RemoteException;
}
```

## Classe que implementa logíca do servidor.

* A primeira coisa a ser feita foi transformar a classe em um objeto remoto isso é o que faz a classe ficar visivel na rede, implementar a classe Icorretora.

```java
public class CorretoraImpl extends UnicastRemoteObject implements ICorretora{}

```

* **ConcurrentHashMap** = magine dois clientes (dois notebooks diferentes) tentando atualizar o preço do Bitcoin (BTC) exatamente no mesmo milissegundo. Se usássemos um **HashMap** normal, a memória do servidor poderia se corromper, e o programa travaria.
````java
private Map<String, Double> acoes; // Inicializado como ConcurrentHashMap
````
 
O ConcurrentHashMap resolve isso gerenciando travas (locks) internamente. Ele permite que dezenas de clientes leiam e atualizem preços ao mesmo tempo, de forma totalmente segura e rápida. 

* **CopyOnWriteArrayList** = Quando o preço muda, o servidor faz um for (loop) nessa lista para avisar todo mundo. Mas e se, bem no meio do loop, um novo cliente se conectar? Ou pior, e se um cliente fechar o notebook e for removido da lista? Em listas normais (**ArrayList**), isso gera um erro fatal chamado **ConcurrentModificationException**, derrubando o servidor.

````java
private List<IClienteCallback> clientesConectados; // Inicializado como CopyOnWriteArrayList
````

A CopyOnWriteArrayList cria uma "cópia" segura da lista toda vez que alguém entra ou sai. Assim, quem está lendo (o loop de notificação) nunca é interrompido por quem está escrevendo (um novo cliente se conectando).

* **Tolerância a Falhas Básica (queda do Cliente).**

  O servidor tenta chamar o método no cliente. Se o notebook do seu do outro usuario perder o Wi-Fi ou ele fechar o programa, o RMI vai disparar uma RemoteException.
````java
private void notificarTodosClientes(String ticker, double novoPreco) {
    for (IClienteCallback cliente : clientesConectados) {
        try {
            cliente.notificarAtualizacaoPreco(ticker, novoPreco);
        } catch (RemoteException e) {
            clientesConectados.remove(cliente); // Remove o "morto"
        }
    }
}
````
O bloco try/catch captura esse erro silenciosamente. Em vez do servidor inteiro travar porque um cliente sumiu, ele simplesmente aceita que a conexão caiu e remove esse cliente da lista de notificações. O sistema continua rodando perfeitamente para os outros clientes conectados.


## Mudando a logica do main.

Para inicializar corretamente a classe implementada.

````java
public class ServidorMain {
    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", "192.168.0.10");
            
            ICorretora corretora = new CorretoraImpl();
            
            Registry registry = LocateRegistry.createRegistry(1099);
            
            registry.rebind("ServicoCorretora", corretora);

            System.out.println("[OK] Servidor RMI da Corretora no ar!");
            System.out.println("[INFO] Rodando no IP: " + System.getProperty("java.rmi.server.hostname"));
            System.out.println("[INFO] Aguardando clientes...");

        } catch (Exception e) {
            System.err.println("[ERRO CRÍTICO] O servidor falhou ao iniciar:");
            e.printStackTrace();
        }
    }}
````
### *Pontos de resalvas.*

* **LocateRegistry.createRegistry(1099):** nós fizemos no próprio código Java abrir a porta 1099.
* **rebind("ServicoCorretora", corretora):** Esse é o nome que os clientes vão ter que procurar para achar a sua corretora. Se o cliente procurar por "CorretoraDistribuida", vai dar erro. Tem que ser o nome exato!
* **Controle de IP:** Você precisa abrir o CMD do Windows, digitar ipconfig, pegar o endereço IPv4 do notebook que vai ser o servidor e colar no System.setProperty. Se não fizer isso, os outros notebooks não conectam.

## Agora a parte do cliente.

Seguimos o mesmo passo de tornar a classe cliente em objeto remoto e implementar a interface.

````java
public class ClienteCallbackImpl extends UnicastRemoteObject implements IClienteCallback {

    public ClienteCallbackImpl() throws RemoteException {
        super();
    }
    
    @Override
    public void notificarAtualizacaoPreco(String ticker, double novoPreco) throws RemoteException {
        System.out.println("\n[ALERTA DE MERCADO] -> O ativo " + ticker + " mudou de preço! Novo valor: " + novoPreco);
        System.out.print("Escolha uma opção (1-Consultar, 2-Listar, 3-Atualizar, 0-Sair): "); // Reimprime o prompt para não bagunçar a tela
    }
}
````

## Menu cliente.

````java
public class ClienteMain {
    
    private static ICorretora corretora;
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
                corretora = (ICorretora) registry.lookup("ServicoCorretora");
                
                // Cria o callback e avisa o servidor: "Me avise quando mudar algo!"
                if (callback == null) {
                    callback = new ClienteCallbackImpl();
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
````
### *Pontos do codigo.

* **Transparência de Acesso:** Note que na linha corretora.consultarPreco(), o Cliente chama o método como se a Corretora estivesse na própria máquina dele. Ele não sabe que isso está viajando pela rede.
* **Atualização em tempo real:** Se você abrir dois clientes (duas telas no CMD) e atualizar o preço no Cliente A, o Cliente B vai receber o alerta automaticamente via ClienteCallback.
* **Tolerância a Falhas:** Se ovocê fechar a aba do Servidor enquanto o Cliente está rodando e você tentar consultar um preço, o catch (Exception e) vai pegar o erro e chamar o conectarAoServidor(), que vai ficar tentando se conectar de 5 em 5 segundos até você ligar o servidor de volta!


