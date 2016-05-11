import org.jetbrains.annotations.NotNull;

import java.net.URL;

/*
 * Download task
 *
 * @author 1
 */
public class DownloadTask {
    @NotNull
    private final URL url;
    private final long depth;
    private final long depthLimit;

    public DownloadTask(@NotNull URL url, long depth, long depthLimit) {
        this.url = url;
        this.depth = depth;
        this.depthLimit = depthLimit;
    }

    @NotNull
    public URL getUrl() {
        return url;
    }

    public long getDepth() {
        return depth;
    }

    public long getDepthLimit() {
        return depthLimit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DownloadTask that = (DownloadTask) o;

        if (depth != that.depth) return false;
        if (depthLimit != that.depthLimit) return false;
        return url.equals(that.url);

    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + (int) (depth ^ (depth >>> 32));
        result = 31 * result + (int) (depthLimit ^ (depthLimit >>> 32));
        return result;
    }
}
