import com.amazon.sqs.javamessaging.AmazonSQSMessagingClientWrapper;
import com.amazonaws.ClientConfiguration;

import static org.junit.Assert.*;

import org.junit.Test;

import javax.jms.JMSException;
import java.util.ArrayList;

import static org.hamcrest.Matchers.equalTo;

public class SQSOperationsTest {

    SQSOperations operations = new SQSOperations();
    AmazonSQSMessagingClientWrapper client = operations.newConnection().getWrappedAmazonSQSClient();

    @Test
    public void cliConfig() {

        ClientConfiguration cliMetodo = operations.clientConfig();          // Resultado do método
        ClientConfiguration cliExpected = new ClientConfiguration();        // Resultado esperado

        // Setando resultado esperado
        cliExpected.setProxyHost("proxylatam.indra.es");
        cliExpected.setProxyPort(8080);
        cliExpected.setProxyUsername("wlpawlak");
        cliExpected.setProxyPassword("W30yg22l");

        assertThat(cliMetodo.getProxyHost(), equalTo(cliExpected.getProxyHost()));
        assertThat(cliMetodo.getProxyPort(), equalTo(cliExpected.getProxyPort()));
        assertThat(cliMetodo.getProxyUsername(), equalTo(cliExpected.getProxyUsername()));
        assertThat(cliMetodo.getProxyPassword(), equalTo(cliExpected.getProxyPassword()));
    }

    @Test
    public void getListQueueURL() {

        ArrayList<String> listaMetodo = operations.getListQueueURL();
        ArrayList<String> listaExpected = new ArrayList<String>();

        listaExpected.add("https://sqs.sa-east-1.amazonaws.com/860125766054/TestQueue.fifo");

        assertEquals(listaExpected, listaMetodo);
    }

    @Test
    public void criarFilaSQS() {

        String myQueue = "FilaExemplo.fifo";                 // Nome da fila de teste
        String resultado = operations.criarFilaSQS(myQueue);    // Executando método de criação

        try {
            String expected = client.getQueueUrl(myQueue).toString();
            assertEquals(expected, resultado);
            operations.deletarFilaSQS(myQueue);    // Deletando fila exemplo
        }catch (JMSException e){
            System.out.println(e);
        }

    }

    @Test
    public void criarFilaSQSExistente(){

        operations.criarFilaSQS("FilaTesteCriarRepetido.fifo");                      // Executando método e criando fila exemplo
        String result = operations.criarFilaSQS("FilaTesteCriarRepetido.fifo");      // Guardando return da segunda execução

        assertNull(result);

        operations.deletarFilaSQS("FilaTesteCriarRepetido.fifo");                    // Deletando fila criada
    }

    @Test
    public void deletarFilaSQS() {

        String myQueue = "FilaExemplo.fifo";
        operations.criarFilaSQS(myQueue);                  // Criando fila exemplo para ser deletada

        String expected = "Fila "+myQueue+" deletada";
        String result = operations.deletarFilaSQS(myQueue);

        assertEquals(expected, result);
    }

    @Test
    public void deletarFilaSQSInexistente() {

        String expected = "A fila não existe";
        String result = operations.deletarFilaSQS("FilaTesteDeletarInexistente.fifo");

        assertEquals(expected, result);
    }
}