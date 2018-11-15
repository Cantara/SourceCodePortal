package no.cantara.docsite.cache;

import io.vavr.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

public class TupleTest {

    private static final Logger LOG = LoggerFactory.getLogger(TupleTest.class);

    @Ignore
    @Test
    public void testTuple() {
        String path = "/Cantara/SCP/master";

        String[] split = path.split("/");
        Tuple<String,String> t1 = new Tuple<>(split[0], split[1]);
        Tuple<String, String> t2 = new Tuple<>("Cantara", "SCP");
        LOG.trace("{} <- {} -- {}", t1.equals(t2), split[0], split[1]);
    }

    @Test
    public void testVavr() {
        String path = "/Cantara/SCP/master";
        String[] split = path.split("/");
        Tuple3<String,String,String> tuple3 = io.vavr.Tuple.of(split[0], split[1], split[2]);
        int test = tuple3.compareTo(io.vavr.Tuple.of("Cantara", "SCP", "master"));
        LOG.trace("{}", test);
    }
}
