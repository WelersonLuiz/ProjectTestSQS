import com.amazon.sqs.javamessaging.AmazonSQSMessagingClientWrapper;
import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;

import javax.jms.JMSException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SQSOperations {
    SQSOperations(){}   // Construtor

    BasicAWSCredentials awsCreds = new BasicAWSCredentials(         // Declarando credenciais
            "ID_CHAVE_ACESSO",
            "SENHA_CHAVE_ACESSO"
    );
    final AmazonSQS sqsBuilder = AmazonSQSClientBuilder.standard()  // Declarando o builder do cliente, para acessar o AWS
            .withRegion(Regions.SA_EAST_1)                                      // Configurando região
            .withCredentials(new AWSStaticCredentialsProvider(awsCreds))        // Configurando credenciais
            .withClientConfiguration(clientConfig())                            // Configurando o cliente (proxy), com o método clientConfiguration
            .build();

    // Método para configurações do client
    public ClientConfiguration clientConfig (){

        // Variável com as configurações do cliente
        ClientConfiguration cli_config = new ClientConfiguration();

        // Setando configurações de proxy
        cli_config.setProxyHost("proxy_address");
        cli_config.setProxyPort(8080);
        cli_config.setProxyUsername("usuário");
        cli_config.setProxyPassword("senha");

        return cli_config;
    }

    // Método do para criar conexão
    public SQSConnection newConnection (){
        try {
            return new SQSConnectionFactory(new ProviderConfiguration(), sqsBuilder).createConnection();    // Retorna nova conexão criada
        }catch (JMSException e){
            return null;    // Retorna nulo caso não tenha sido possivel criar a conexão
        }
    }

    // Método para criar uma nova fila
    public String criarFilaSQS(String queueName){
        try {
            SQSConnection connection = newConnection();                                             // Cria conexão com o método newConnection
            AmazonSQSMessagingClientWrapper client = newConnection().getWrappedAmazonSQSClient();   // Obtem wrapped client para o SQS

            // Caso a fila não exista ela será criada
            if (!client.queueExists(queueName)) {

                final Map<String, String> attributes = new HashMap<String, String>();   // Variável para os atributos da fila
                attributes.put("FifoQueue", "true");                                    // Define a fila do tipo FIFO
                attributes.put("ContentBasedDeduplication", "true");                    // Define eliminação de duplicatas

                // Request de criação de fila com os atributos escolhidos
                final CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName).withAttributes(attributes);
                final String myQueueUrl = client.createQueue(createQueueRequest).getQueueUrl(); // Obtendo url da fila criada

                System.out.println("\nNova Fila: " + queueName + "\nURL: " + myQueueUrl);

                return client.getQueueUrl(myQueueUrl).toString();
            }else {
                System.out.println("\nA fila "+ queueName + " já foi criada.");
                return null;
            }
        }catch (JMSException e) {
            System.out.println("JSMException");
        }
        return null;
    }

    // Método que exclui uma fila
    public String deletarFilaSQS(String queueName){
        try {
            SQSConnection connection = newConnection();                                             // Cria conexão com o método newConnection
            AmazonSQSMessagingClientWrapper client = newConnection().getWrappedAmazonSQSClient();   // Obtem wrapped client para o SQS

            // Caso a fila não exista ela será criada
            if (client.queueExists(queueName)) {

                String myQueueUrl = client.getQueueUrl(queueName).getQueueUrl();   // Obtendo url da fila

                System.out.println("Deleting the test queue.\n");
                sqsBuilder.deleteQueue(new DeleteQueueRequest(myQueueUrl));

                return "Fila "+ queueName + " deletada";
            }else {
                return "A fila não existe";
            }
        }catch (JMSException e) {
            System.out.println("JSMException");
        }
        return null;
    }

    // Método que retorna as listas existentes
    public ArrayList<String> getListQueueURL(){

        ArrayList<String> list = new ArrayList<String>();

        System.out.println("\nLista de filas na sua conta:\n");
        for (final String queueUrl : sqsBuilder.listQueues().getQueueUrls()) {
            System.out.println("Url: " + queueUrl);
            list.add(queueUrl);
        }
        return list;
    }

}
