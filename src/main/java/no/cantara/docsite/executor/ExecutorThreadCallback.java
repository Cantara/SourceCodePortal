package no.cantara.docsite.executor;

@FunctionalInterface
public interface ExecutorThreadCallback<V> {

    V call(WorkerTask<?> event) throws Exception;

}
