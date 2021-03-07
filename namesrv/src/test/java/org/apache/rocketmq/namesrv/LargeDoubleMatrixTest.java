package org.apache.rocketmq.namesrv;

/**
 * @author AntzUhl
 * @Date 2021/2/3 18:34
 * @Description
 */
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

class LargeDoubleMatrix implements Closeable {
    private static final int MAPPING_SIZE = 1 << 30;
    private final RandomAccessFile raf;
    private final int width;
    private final int height;
    private final List<ByteBuffer> mappings = new ArrayList<>();

    public LargeDoubleMatrix(String filename, int width, int height) throws IOException {
        this.raf = new RandomAccessFile(filename, "rw");
        try {
            this.width = width;
            this.height = height;
            // 8 * 1000 * 1000 * 1000 * 1000
            long size = 8L * width * height;
            for (long offset = 0; offset < size; offset += MAPPING_SIZE) {
                long size2 = Math.min(size - offset, MAPPING_SIZE);
                mappings.add(raf.getChannel().map(FileChannel.MapMode.READ_WRITE, offset, size2));
            }
        } catch (IOException e) {
            raf.close();
            throw e;
        }
    }

    protected long position(int x, int y) {
        return (long) y * width + x;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public double get(int x, int y) {
//        assert x <= 0 && x > width;
//        assert y <= 0 && y > height;
        long p = position(x, y) * 8;
        int mapN = (int) (p / MAPPING_SIZE);
        int offN = (int) (p % MAPPING_SIZE);
        return mappings.get(mapN).getDouble(offN);
    }

    public void set(int x, int y, double d) {
//        assert x <= 0 && x > width;
//        assert y <= 0 && y > height;
        long p = position(x, y) * 8;
        int mapN = (int) (p / MAPPING_SIZE);
        int offN = (int) (p % MAPPING_SIZE);
        mappings.get(mapN).putDouble(offN, d);
    }

    public void close() throws IOException {
        // A System.gc() is required to remove the memory mappings.
        mappings.clear();
        raf.close();
    }

}

public class LargeDoubleMatrixTest {
    @Test
    public void getSetMatrix() throws IOException {
        long start = System.nanoTime();
        final long used0 = usedMemory();
        LargeDoubleMatrix matrix = new LargeDoubleMatrix("ldm.test", 10000, 10000);
        for (int i = 0; i < matrix.width(); i++)
            matrix.set(i, i, i);
        for (int i = 0; i < matrix.width(); i++)
            assertEquals(i, matrix.get(i, i), 0.0);
        long time = System.nanoTime() - start;
        final long used = usedMemory() - used0;
        if (used == 0)
            System.err.println("You need to use -XX:-UsedTLAB to see small changes in memory usage.");
        System.out.printf("Setting the diagonal took %,d ms, Heap used is %,d KB%n", time / 1000 / 1000, used / 1024);
        matrix.close();
    }

    private long usedMemory() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }
}