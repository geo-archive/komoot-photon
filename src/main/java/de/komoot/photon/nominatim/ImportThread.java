package de.komoot.photon.nominatim;

import de.komoot.photon.Importer;
import de.komoot.photon.PhotonDoc;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Worker thread for bulk importing data from a Nominatim database.
 */
public class ImportThread {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ImportThread.class);

    private static final int PROGRESS_INTERVAL = 50000;
    private static final Iterable<PhotonDoc> FINAL_DOCUMENT = Collections.EMPTY_LIST;
    private final BlockingQueue<Iterable<PhotonDoc>> documents = new LinkedBlockingDeque<>(100);
    private final AtomicLong counter = new AtomicLong();
    private final Importer importer;
    private final Thread thread;
    private final long startMillis;

    public ImportThread(Importer importer) {
        this.importer = importer;
        this.thread = new Thread(new ImportRunnable());
        this.thread.start();
        this.startMillis = System.currentTimeMillis();
    }

    /**
     * Adds the given document from Nominatim to the import queue.
     *
     * @param docs Fully filled nominatim document.
     */
    public void addDocument(Iterable<PhotonDoc> docs) {
        if (docs == null || !docs.iterator().hasNext()) {
            return;
        }

        while (true) {
            try {
                documents.put(docs);
                break;
            } catch (InterruptedException e) {
                LOGGER.warn("Thread interrupted while placing document in queue.");
                // Restore interrupted state.
                Thread.currentThread().interrupt();
            }
        }

        if (counter.incrementAndGet() % PROGRESS_INTERVAL == 0) {
            final double documentsPerSecond = 1000d * counter.longValue() / (System.currentTimeMillis() - startMillis);
            LOGGER.info("Imported {} documents [{}/second]", counter.longValue(), documentsPerSecond);
        }
    }

    /**
     * Finalize the import.
     *
     * Sends an end marker to the import thread and then waits for it to join.
     */
    public void finish() {
        while (true) {
            try {
                documents.put(FINAL_DOCUMENT);
                thread.join();
                break;
            } catch (InterruptedException e) {
                LOGGER.warn("Thread interrupted while placing document in queue.");
                // Restore interrupted state.
                Thread.currentThread().interrupt();
            }
        }
        LOGGER.info("Finished import of {} photon documents. (Total processing time: {}s)",
                    counter.longValue(), (System.currentTimeMillis() - startMillis)/1000);
    }

    private class ImportRunnable implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    final var docs = documents.take();
                    if (!docs.iterator().hasNext()) {
                        break;
                    }
                    int objectId = 0;
                    for (PhotonDoc doc : docs) {
                        importer.add(doc, objectId++);
                    }
                } catch (InterruptedException e) {
                    LOGGER.info("Interrupted exception", e);
                    // Restore interrupted state.
                    Thread.currentThread().interrupt();
                }
            }
            importer.finish();
        }
    }

}
