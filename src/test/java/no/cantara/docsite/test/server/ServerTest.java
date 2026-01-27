package no.cantara.docsite.test.server;

import io.undertow.Undertow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(TestServerExtension.class)
public class ServerTest {

    @Inject
    private TestServer server;

    @Test
    public void thatTestServerStartsMainWithRunningUndertowListener() {
        List<Undertow.ListenerInfo> listenerInfo = server.application.getServer().getListenerInfo();
        Undertow.ListenerInfo info = listenerInfo.iterator().next();
        assertEquals(info.getProtcol(), "http");
    }

}
