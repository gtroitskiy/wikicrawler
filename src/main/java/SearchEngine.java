import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/*
 * Search engine
 *
 * @author 1
 */
public interface SearchEngine {
    /**
     * Crawl specified url
     *
     * @param rawUrl starting url
     * @param depth depth of crawling
     */
    void crawl(String rawUrl, long depth);

    /**
     * Search specified term
     *
     * @param term to find
     * @return wiki pages containing specified term
     */
    @NotNull
    Collection<WikiPage> search(String term);

    /**
     * Stop search engine
     */
    void stop();
}
