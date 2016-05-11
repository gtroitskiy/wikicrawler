import com.google.common.collect.Multimap;
import jsoup.JsoupFacade;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/*
 * Download worker
 *
 * @author 1
 */
@ThreadSafe
public class Downloader implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Downloader.class);

    private final BlockingQueue<DownloadTask> queue;
    // TODO: distributed index
    @GuardedBy("index") private final Multimap<String, WikiPage> index;
    private final ConcurrentHashMap<String, WikiPage> wikiPages;
    private final JsoupFacade jsoup;
    private volatile boolean isRunning;

    public Downloader(
            BlockingQueue<DownloadTask> queue,
            Multimap<String, WikiPage> index,
            ConcurrentHashMap<String, WikiPage> wikiPages,
            JsoupFacade jsoup
    ) {
        this.queue = queue;
        this.index = index;
        this.wikiPages = wikiPages;
        this.jsoup = jsoup;
        this.isRunning = true;
    }

    @Override
    public void run() {
        while (isRunning && !Thread.currentThread().isInterrupted()) {
            final DownloadTask downloadTask;
            try {
                downloadTask = queue.poll(1, TimeUnit.SECONDS);
                if (downloadTask == null) {
                    continue;
                }
            } catch (InterruptedException e) {
                logger.warn("Executor service has been interrupted", e);
                continue;
            }

            // skip if url is already being processed
            final URL url = downloadTask.getUrl();
            final String rawUrl = url.toString();
            if (wikiPages.contains(rawUrl)) {
                logger.debug("Wiki page for \"" + url + "\" is already processed");
                continue;
            }
            logger.info("Processing \"" + url);

            // add dummy page to decrease possibility of downloaders working on the same url
            wikiPages.put(rawUrl, WikiPage.DUMMY_WIKI_PAGE);

            final Connection connection = jsoup.connect(url);
            final Document document;
            try {
                document = connection.get();
            } catch (IOException e) {
                logger.warn("Failed to get document for \"" + connection + "\" connection", e);
                continue;
            }

            final String text = document.body().text();
            final WikiPage wikiPage = new WikiPage(url, text);
            final WikiPage addedPage = wikiPages.put(rawUrl, wikiPage);
            if (!WikiPage.DUMMY_WIKI_PAGE.equals(addedPage)) {
                logger.debug("Wiki page for \"" + url + "\" has been already added");
                continue;
            }

            final StringTokenizer tokenizer = new StringTokenizer(text);
            while (tokenizer.hasMoreElements()) {
                final String token = tokenizer.nextToken();
                // TODO: make index distributed over downloaders
                index.put(token, wikiPage);
            }

            // skip links, if depth limit reached
            final long depth = downloadTask.getDepth();
            final long limit = downloadTask.getDepthLimit();
            if (depth == limit) {
                continue;
            }

            final Elements links = document.select("a[href]");
            // to prevent live locks: aggregate links before adding to the queue
            final LinkedList<DownloadTask> newLinkUrls = new LinkedList<>();
            for (Element link : links.subList(0, links.size())) {
                final String rawLinkUrl = link.attr("abs:href");

                // check url validity prior to adding to the queue
                final URL subUrl;
                try {
                    subUrl = new URL(rawLinkUrl);
                } catch (MalformedURLException e) {
                    logger.warn("Failed to extract valid URL from \"" + rawLinkUrl + "\" link", e);
                    continue;
                }

                if (wikiPages.contains(rawLinkUrl)) {
                    logger.debug("Already processed \"" + subUrl + "\" link");
                    continue;
                }

                final String host = subUrl.getHost();
                if (!host.endsWith("wikipedia.org")) {
                    logger.debug("Non-wiki url \"" + subUrl + "\" has been ignored");
                    continue;
                }

                newLinkUrls.add(new DownloadTask(subUrl, depth + 1, limit));
            }

            queue.addAll(newLinkUrls);
        }
    }

    public void stop() {
        isRunning = false;
    }
}
