import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;

/*
 * Wikipedia page
 *
 * @author 1
 */
public class WikiPage {
    public static final WikiPage DUMMY_WIKI_PAGE = new WikiPage();

    @NotNull
    private final URL url;
    @NotNull
    private final String text;

    private WikiPage() {
        try {
            this.url = new URL("http://dummy.url");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Failed to initialize dummy wiki page", e);
        }
        this.text = "dummy";
    }

    public WikiPage(@NotNull URL url, @NotNull String text) {
        this.url = url;
        this.text = text;
    }

    @NotNull
    public URL getUrl() {
        return url;
    }

    @NotNull
    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WikiPage wikiPage = (WikiPage) o;

        if (!url.equals(wikiPage.url)) return false;
        return text.equals(wikiPage.text);

    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + text.hashCode();
        return result;
    }
}
