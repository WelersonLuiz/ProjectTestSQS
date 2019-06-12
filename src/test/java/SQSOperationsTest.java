import com.amazon.sqs.javamessaging.AmazonSQSMessagingClientWrapper;
import com.amazonaws.ClientConfiguration;

import static org.junit.Assert.*;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import netscape.javascript.JSObject;
import org.junit.Test;

import javax.jms.JMSException;
import java.util.ArrayList;

import static org.hamcrest.Matchers.equalTo;

public class SQSOperationsTest {

    SQSOperations operations = new SQSOperations();
    AmazonSQSMessagingClientWrapper client = operations.newConnection().getWrappedAmazonSQSClient();

    @Test
    public void proxyConfig() {

        ClientConfiguration cliMetodo = operations.proxyConfig();           // Resultado do método
        ClientConfiguration cliExpected = new ClientConfiguration();        // Resultado esperado

        // Setando resultado esperado
        cliExpected.setProxyHost("proxylatam.indra.es");
        cliExpected.setProxyPort(8080);

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
    public void criarFilaFifoSQS() {

        String myQueue = "CriarFila.fifo";

        try {
            // Executando método de criação
            String resultado = new JSONObject(operations.criarFilaFifoSQS(myQueue)).getString("QueueUrl");
            String expected = client.getQueueUrl(myQueue).toString();
            assertEquals(expected, resultado);
        }catch (JMSException e){
            System.out.println(e);
        }catch (JSONException e){
            System.out.println(e);
        }

        operations.deletarFilaSQS(myQueue);
    }

    @Test
    public void criarFilaSQSExistente(){

        operations.criarFilaFifoSQS("FilaTesteCriarRepetido.fifo");                      // Executando método e criando fila exemplo
        String result = operations.criarFilaFifoSQS("FilaTesteCriarRepetido.fifo");      // Guardando return da segunda execução

        assertNull(result);

        operations.deletarFilaSQS("FilaTesteCriarRepetido.fifo");                    // Deletando fila criada
    }

    @Test
    public void deletarFilaSQS() {

        String myQueue = "FilaExemplo2.fifo";
        operations.criarFilaFifoSQS(myQueue);                  // Criando fila exemplo para ser deletada

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