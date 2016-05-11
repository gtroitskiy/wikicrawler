import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/*
 * Tests for single-word and non-ranked search engine
 *
 * @author 1
 */
public class SingleWordNonRankedSearchEngineTests {
    @Test
    public void shouldAddTaskToTheQueue() throws IOException {
        final String rawUrl = "http://someurl";
        final int depth = 123;

        //noinspection unchecked
        final BlockingQueue<DownloadTask> queue = (BlockingQueue<DownloadTask>) mock(BlockingQueue.class);
        final SingleWordNonRankedSearchEngine engine = new SingleWordNonRankedSearchEngine(1, queue);
        engine.crawl(rawUrl, depth);

        verify(queue).add(eq(new DownloadTask(new URL(rawUrl), 1, depth)));
        engine.stop();
    }
}
