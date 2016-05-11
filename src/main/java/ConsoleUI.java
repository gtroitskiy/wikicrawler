import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.LinkedBlockingQueue;

/*
 * Simple console user interface
 *
 * @author 1
 */
public class ConsoleUI {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleUI.class);
    private static final int N_THREADS = 10;
    private static final SearchEngine engine = new SingleWordNonRankedSearchEngine(N_THREADS, new LinkedBlockingQueue<>());

    public static void main(String[] args) throws IOException {
        final Scanner scanner = new Scanner(System.in);
        commandLoop:
        while (true) {
            final String rawCommand = scanner.nextLine();
            final StringTokenizer tokenizer = new StringTokenizer(rawCommand);
            if (!tokenizer.hasMoreElements()) {
                continue;
            }
            final String command = tokenizer.nextToken();
            switch (command) {
                case ":crawl":
                    if (!tokenizer.hasMoreElements()) {
                        System.out.println("error: no URL specified");
                        continue;
                    }
                    final String rawUrl = tokenizer.nextToken();
                    if (!tokenizer.hasMoreElements()) {
                        System.out.println("error: no depth value specified");
                        continue;
                    }
                    final String rawDepth = tokenizer.nextToken();
                    final long depth;
                    try {
                        depth = Long.parseLong(rawDepth);
                    } catch (NumberFormatException e) {
                        logger.warn("Failed to get valid depth out of specified \"" + rawDepth + "\" value", e);
                        System.out.println("error: invalid depth value");
                        continue;
                    }
                    engine.crawl(rawUrl, depth);
                    break;
                case ":search":
                    if (!tokenizer.hasMoreElements()) {
                        System.out.println("error: no search term specified");
                        continue;
                    }
                    @NotNull final Collection<WikiPage> pages = engine.search(tokenizer.nextToken());
                    for (WikiPage page : pages) {
                        System.out.println(page.getUrl());
                    }

                    break;
                case ":stop":
                    engine.stop();
                    break commandLoop;
                case ":help":
                    printHelp();
                    break;
                default:
                    System.out.println("error: unknown command");
                    printHelp();
            }
        }
    }

    private static void printHelp() {
        System.out.println("Type\n" +
                ":crawl <url> <level>  -  to index pages starting from <link> with depth <level>\n" +
                ":search <term>        -  to find pages with <term>\n" +
                ":stop                 -  to exit\n" +
                ":help                 -  to print this help\n"
        );
    }
}
