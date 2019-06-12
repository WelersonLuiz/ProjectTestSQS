## Configurações e Conexão

#### Credenciais AWS
> Essa variável guarda as credenciais do usuário AWS

    private BasicAWSCredentials awsCreds = new BasicAWSCredentials(
        "AKIA4QQ35UGTCW6QFPUG",
        "Wp3TTbBRtufBiQFbJa4nAnmdL6kFTOAnBL0Fga9Q"
    );
    
#### Configuração de proxy
> Esse método cria uma configuração de cliente e será utilizado no Client Builder

    public ClientConfiguration proxyConfig (){

        ClientConfiguration cli_config = new ClientConfiguration();
        cli_config.setProxyHost("proxylatam.indra.es");
        cli_config.setProxyPort(8080);

        return cli_config;
    }
    
#### Client builder 
> Declaração do cliente com região, credenciais e proxy

<pre>
private AmazonSQS client = AmazonSQSClientBuilder.standard()
        .withRegion(Regions.US_EAST_2)
        .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
        .withClientConfiguration(<b><span style="color:red">proxyConfig()</span></b>)
        .build();
</pre>

#### Nova conexão
> Método criando nova conexão SQS com o cliente construido

    public SQSConnection newConnection (){
    
        try { 
            return new SQSConnectionFactory(
                    new ProviderConfiguration(), client
            ).createConnection();
        }catch (JMSException e){
            return null;
        }
    }
    
#### Declaração nova conexão

    private AmazonSQSMessagingClientWrapper connection = newConnection()
            .getWrappedAmazonSQSClient();
        
> Com a nova conexão criada, podemos criar diversos métodos fazendo as operações de SQS.
    
## Métodos

### Criar Fila FIFO

    public String criarFilaFifoSQS(String queueName){
    
        try {
            if (!connection.queueExists(queueName)) {
            
                // Definindo atributos da fila
                final Map<String, String> attributes = new HashMap<String, String>();
                attributes.put("FifoQueue", "true");
                attributes.put("ContentBasedDeduplication", "true");

                final CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName)
                        .withAttributes(attributes);

                final String myQueueUrl = connection
                        .createQueue(createQueueRequest)
                        .getQueueUrl();

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

### Excluir Fila

    public String deletarFilaSQS(String queueName){
    
        try {
            // Caso a fila exista ela será deletada
            if (connection.queueExists(queueName)) {

                String myQueueUrl = connection.getQueueUrl(queueName).getQueueUrl();

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

### Listar Filas
> Esse método não utiliza a variável **connection**, mas sim o **client** com os métodos **listQueues()** e **getQueueUrls()** para retornar todas as URLs das filas do cliente

    public ArrayList<String> getListQueueURL(){

        ArrayList<String> list = new ArrayList<String>();

        System.out.println("\nLista de filas na sua conta:\n");
        
        for (final String queueUrl : client.listQueues().getQueueUrls()) {
            System.out.println("Url: " + queueUrl);
            list.add(queueUrl);
        }
        return list;
    }

