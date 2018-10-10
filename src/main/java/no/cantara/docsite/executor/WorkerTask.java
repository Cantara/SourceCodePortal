package no.cantara.docsite.executor;

import java.lang.annotation.Annotation;

/**
 * The worker task is a wrapper class that wraps the event data object
 *
 * @param <T> Wrapped data
 */
public class WorkerTask<T> {

    private Class<Annotation> eventType;
    private T source;

    public WorkerTask(Class<Annotation> eventType, T source) {
        this.eventType = eventType;
        this.source = source;
    }

    public Class<Annotation> getEventType() {
        return eventType;
    }

    public T getSource() {
        return source;
    }

}
