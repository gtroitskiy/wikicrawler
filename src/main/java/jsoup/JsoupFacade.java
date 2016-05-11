package jsoup;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.net.URL;

/**
 * Facade for Jsoup
 *
 * @author 1
 */
public class JsoupFacade {

    /**
     * Connects to specified url
     *
     * @param url to connec
     * @return connection object
     */
    public Connection connect(URL url) {
        return Jsoup.connect(url.toExternalForm());
    }
}
