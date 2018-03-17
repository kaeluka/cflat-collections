package com.github.kaeluka.cflat.test.datastructures;

import com.github.kaeluka.cflat.Matrix;
import com.github.kaeluka.cflat.storage.NestedStorage;
import com.github.kaeluka.cflat.util.Storages;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class SparseMatrixTest {
    private final static int SIZE = 8000;

    @Parameterized.Parameter()
    public Supplier<NestedStorage<Double>> seqSupplier;

    @SuppressWarnings("unchecked")
    @Parameterized.Parameters(name="{0}")
    public static <V> List<Supplier<NestedStorage<V>>> storages() {
        return Storages.nestedStorages();
    }

    @Test
    public void initThenGetInOrder() throws Exception {
        final int batches = 20;
        final Random seeds = new Random(12345);
        for (int i = 0; i < batches; i++) {
            System.out.println("=== batch " + (i + 1) + "/" + batches);

            final long SEED = seeds.nextLong();
            Matrix matr = new Matrix(SIZE, SIZE, this.seqSupplier.get());
            Random r = new Random(SEED);
            int colMajIdx = 0;
            int totalValues = 0;

            final int maxStep = 400;
            while (colMajIdx < (SIZE*SIZE-maxStep)) {
                colMajIdx += r.nextInt(maxStep);
                final int x = colMajIdx / SIZE;
                final int y = colMajIdx % SIZE;
                totalValues++;
                matr = matr.put(x, y, x);
            }

            r = new Random(SEED);
            colMajIdx = 0;

            while (colMajIdx < (SIZE*SIZE-maxStep)) {
                colMajIdx += r.nextInt(maxStep);
                final int x = colMajIdx / SIZE;
                final int y = colMajIdx % SIZE;
                assertThat(matr.get(x, y), is(1.0*x));
            }

            System.out.println("total values: " + totalValues);
            try {
                System.out.println(matr.storage.bytesUsed() / (1024 * 1024) + "MB");
            } catch (Throwable ignore) {}
        }
    }

    @Test
    public void initThenGet() throws Exception {
        final int BATCHES = 5;
        final Random seeds = new Random(12345);
        for (int batch = 0; batch < BATCHES; batch++) {
            System.out.println("=== batch " + (batch + 1) + "/" + BATCHES);

            final long SEED = seeds.nextLong();
            final int NONZERO = (int) (SIZE * SIZE * 0.005);
            Matrix matr = new Matrix(SIZE, SIZE, this.seqSupplier.get());
            Random r = new Random(SEED);
            int totalValues = 0;
            int maxrow = -1;

            for (int i = 0; i < NONZERO; ++i) {
                if (i % 100000 == 0) {
                    System.out.println(Math.round(100.0 * i / (NONZERO - 1)) + "% ");
                }
                final int row = r.nextInt(SIZE);
                final int col = r.nextInt(SIZE);
                matr = matr.put(row, col, col);
                assertThat("immediately checking [" + row + ", " + col + "]",
                        matr.get(row, col), is(1.0 * col));
                maxrow = Math.max(maxrow, row);
                totalValues++;
            }
            System.out.println("initialised " + matr.storage.getClass().getSimpleName());
            assertThat(matr.storage.maxIdx(), is(maxrow + 1));
            assertThat(matr.storage.maxIdxOverapproximation(), greaterThanOrEqualTo(maxrow + 1));

            r = new Random(SEED);
            for (int i = 0; i < NONZERO; ++i) {
                final int row = r.nextInt(SIZE);
                final int col = r.nextInt(SIZE);
                assertThat("checking [" + row + ", " + col + "]",
                        matr.get(row, col), is(1.0 * col));
            }

            System.out.println("total values: " + totalValues);
            try {
                System.out.println(matr.storage.bytesUsed() / (1024 * 1024) + "MB");
            } catch (Throwable ignore) {
            }
        }
    }
}