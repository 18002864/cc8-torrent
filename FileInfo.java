import java.util.ArrayList;
import java.util.List;

public class FileInfo {
    private int totalLength = 0;
    private final List<String> chunks = new ArrayList<>();

    public int getTotalLength() {
        return totalLength;
    }

    public void addTotalLength(int totalLength) {
        this.totalLength += totalLength;
    }

    public List<String> getChunks() {
        return chunks;
    }

    public void addChunk(String chunk) {
        this.chunks.add(chunk);
    }
}
