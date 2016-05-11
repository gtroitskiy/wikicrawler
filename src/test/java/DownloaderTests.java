import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import org.jsoup.Connection;
import jsoup.JsoupFacade;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;
import java.sql.DriverManager;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/*
 * Tests for downloader
 *
 * @author 1
 */
public class DownloaderTests {
    // TODO: complete
    @Test
    @Ignore
    public void shouldAddNewTaskForSubLInk() throws IOException, InterruptedException {
        final String rawUrl = "http://somelink";
        final int depthLimit = 123;
        final int depth = 1;
        final DownloadTask task = new DownloadTask(new URL(rawUrl), depth, depthLimit);

        final JsoupFacade jsoup = mock(JsoupFacade.class);
        final Connection connection = mock(Connection.class);
        final Document document = mock(Document.class);
        final Element element = mock(Element.class);

        final Elements elements = mock(Elements.class);
        final Element sublink = mock(Element.class);
        final String subLinkUrl = "http://sublink";
        when(sublink.attr(any())).thenReturn(subLinkUrl);
        when(elements.subList(anyInt(), anyInt())).thenReturn(Collections.singletonList(sublink));
        when(document.select(any())).thenReturn(elements);
        when(document.body()).thenReturn(element);
        when(connection.get()).thenReturn(document);
        when(jsoup.connect(any())).thenReturn(connection);

        final LinkedBlockingDeque<DownloadTask> queue = new LinkedBlockingDeque<>();
        final Downloader downloader = new Downloader(
                queue,
                Multimaps.synchronizedMultimap(HashMultimap.create()),
                new ConcurrentHashMap<>(),
                jsoup
        );
        queue.add(task);

        final Thread downloadThread = new Thread(downloader);
        downloadThread.start();
        final DownloadTask subLinkTask = queue.poll();
//        assertTrue(new DownloadTask(new URL(subLinkUrl), depth + 1, depthLimit).equals(subLinkTask));

        downloader.stop();
        downloadThread.join();
    }
}
