package no.cantara.docsite.task;

import no.cantara.docsite.github.PushPageEvent;

public class FetchPageTask implements Runnable {

    private PushPageEvent event;

    public FetchPageTask(PushPageEvent event) {
        this.event = event;
    }


    @Override
    public void run() {
    }
}
