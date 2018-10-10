package no.cantara.docsite.util;

import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.logging.Level;
import java.util.logging.LogManager;

public class JavaUtilLoggerBridge {

    private static class Initializer {
        static {
            LogManager.getLogManager().reset();
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();
        }

        private static void configure() {
            // method exists to allow access to static initializer
        }
    }

    public static final void installJavaUtilLoggerBridgeHandler(Level level) {
        Initializer.configure();
        LogManager.getLogManager().getLogger("").setLevel(level);
    }
}
