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
import java.util.concurrent.TimeUnit;

public class SQSOperations {
    SQSOperations(){}

    // Declarando credenciais
    private BasicAWSCredentials awsCreds = new BasicAWSCredentials(
            "AKIA4QQ35UGTCW6QFPUG",
            "Wp3TTbBRtufBiQFbJa4nAnmdL6kFTOAnBL0Fga9Q"
    );



    // Declarando o builder do cliente, para acessar o AWS
    private AmazonSQS client = AmazonSQSClientBuilder.standard()
            .withRegion(Regions.US_EAST_2)                                      // Configurando região
            .withCredentials(new AWSStaticCredentialsProvider(awsCreds))        // Configurando credenciais
            .withClientConfiguration(proxyConfig())                             // Configurando o cliente (proxy), com o método clientConfiguration
            .build();

    // Obtem o client para o SQS
    private AmazonSQSMessagingClientWrapper connection = newConnection()
            .getWrappedAmazonSQSClient();



    // Método para configurações do client
    public ClientConfiguration proxyConfig (){

        ClientConfiguration cli_config = new ClientConfiguration();
        cli_config.setProxyHost("proxylatam.indra.es");
        cli_config.setProxyPort(8080);

        return cli_config;
    }



    // Método do para criar conexão
    public SQSConnection newConnection (){
        try {
            // Cria nova conexão com o sqsBuilder
            return new SQSConnectionFactory(
                    new ProviderConfiguration(), client
            ).createConnection();
        }catch (JMSException e){
            return null;
        }
    }



    // Método que retorna as listas existentes
    public ArrayList<String> getListQueueURL(){

        ArrayList<String> list = new ArrayList<String>();

        System.out.println("\nLista de filas na sua conta:\n");
        for (final String queueUrl : client.listQueues().getQueueUrls()) {
            System.out.println("Url: " + queueUrl);
            list.add(queueUrl);
        }
        return list;
    }



    // Método para criar uma nova fila
    public String criarFilaFifoSQS(String queueName){
        try {

            if (!connection.queueExists(queueName)) {                                       // Caso a fila não exista ela será criada

                final Map<String, String> attributes = new HashMap<String, String>();   // Variável para os atributos da fila
                attributes.put("FifoQueue", "true");                                    // Define a fila do tipo FIFO
                attributes.put("ContentBasedDeduplication", "true");                    // Define eliminação de duplicatas

                final CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName)
                        .withAttributes(attributes);    // Request de criação de fila

                final String myQueueUrl = connection
                        .createQueue(createQueueRequest)
                        .getQueueUrl();                 // Obtendo url da fila criada

                System.out.println("\nNova Fila: " + queueName + "\nURL: " + myQueueUrl);

                return myQueueUrl;

            }else{
                System.out.println("\nA fila "+ queueName + " já foi criada.");
                return null;
            }
        }catch (JMSException e) {
            System.out.println(e);
        }
        return null;
    }



    // Método que exclui uma fila
    public String deletarFilaSQS(String queueName){
        try {
            // Caso a fila exista ela será deletada
            if (connection.queueExists(queueName)) {

                String myQueueUrl = connection.getQueueUrl(queueName).getQueueUrl();   // Obtendo url da fila

                System.out.println("Deleting the test queue.\n");
                client.deleteQueue(new DeleteQueueRequest(myQueueUrl));

                return "Fila "+ queueName + " deletada";
            }else {
                return "A fila não existe";
            }
        }catch (JMSException e) {
            System.out.println("JSMException");
        }
        return null;
    }


}
