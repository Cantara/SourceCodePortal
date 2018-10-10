package no.cantara.docsite.test.server;

import io.undertow.Undertow;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.List;

import static org.testng.Assert.assertEquals;

@Listeners(TestServerListener.class)
public class ServerTest {

    @Inject
    private TestServer server;

    @Test
    public void thatTestServerStartsMainWithRunningUndertowListener() {
        List<Undertow.ListenerInfo> listenerInfo = server.application.getUndertowServer().getListenerInfo();
        Undertow.ListenerInfo info = listenerInfo.iterator().next();
        assertEquals(info.getProtcol(), "http");
    }

}
