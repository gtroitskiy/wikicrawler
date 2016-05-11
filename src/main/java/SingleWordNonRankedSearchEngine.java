import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import jsoup.JsoupFacade;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/*
 * Simple search engine for single-word and non-ranked searching
 *
 * @author 1
 */
public class SingleWordNonRankedSearchEngine implements SearchEngine {
    private static final Logger logger = LoggerFactory.getLogger(SingleWordNonRankedSearchEngine.class);

    private final ExecutorService executorService;
    private final BlockingQueue<DownloadTask> queue;
    private final List<Downloader> downloaders = new LinkedList<>();

    private final Multimap<String, WikiPage> index = Multimaps.synchronizedMultimap(HashMultimap.create());
    private final ConcurrentHashMap<String, WikiPage> wikiPages = new ConcurrentHashMap<>();

    public SingleWordNonRankedSearchEngine(int threadCount, BlockingQueue<DownloadTask> queue) {
        this.queue = queue;
        this.executorService = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final Downloader downloader = new Downloader(queue, index, wikiPages, new JsoupFacade());
            downloaders.add(downloader);
            executorService.submit(downloader);
        }
    }

    @Override
    public void crawl(String rawUrl, long depth) {
        try {
            queue.add(new DownloadTask(new URL(rawUrl), 1, depth));
        } catch (MalformedURLException e) {
            logger.warn("Failed to get valid URL for specified \"" + rawUrl + "\" link", e);
        }
    }

    @NotNull
    @Override
    public Collection<WikiPage> search(String term) {
        final Collection<WikiPage> wikiPages = index.get(term);
        return wikiPages == null ? Collections.emptyList() : wikiPages;
    }

    @Override
    public void stop() {
        try {
            executorService.shutdown();
            downloaders.forEach(Downloader::stop);
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            logger.warn("Failed to properly stop executor service", e);
        }
    }
}
