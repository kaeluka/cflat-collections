package com.github.kaeluka.cflat.test.datastructures;

import com.github.kaeluka.cflat.Matrix;
import com.github.kaeluka.cflat.storage.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class SparseMatrixTest {
    final static int SIZE = 750;
    private static class NamedSupplier<T> implements Supplier<T> {
        private final Supplier<T> inner;
        private final String name;

        NamedSupplier(final Supplier<T> inner, final String name) {
            this.inner = inner;
            this.name = name;
        }
        @Override public T get() { return inner.get(); }
        @Override public String toString() { return this.name; }
    }

    @Parameterized.Parameter()
    public Supplier<Matrix> seqSupplier;

    @SuppressWarnings("unchecked")
    @Parameterized.Parameters(name="{0}")
    public static List<Supplier<Matrix>> storages() {
        final ArrayList<Supplier<Matrix>> ret = new ArrayList<>();

        ret.add(new NamedSupplier<>(() -> new Matrix(SIZE, SIZE, SparseStorage.getFor(SparseStorage.USAGE.CHANGEABLE)), "SparseStorage (CHANGEABLE)"));
        ret.add(new NamedSupplier<>(() -> new Matrix(SIZE, SIZE, SparseStorage.getFor(SparseStorage.USAGE.SIZE)), "SparseStorage (SIZE)"));
        ret.add(new NamedSupplier<>(() -> new Matrix(SIZE, SIZE, SparseStorage.getFor(SparseStorage.USAGE.INSERT_PERFORMANCE, SIZE, SIZE, 0.01)), "SparseStorage (INSERT)"));
        ret.add(new NamedSupplier<>(() -> new Matrix(SIZE, SIZE, SparseStorage.getFor(SparseStorage.USAGE.READ_PERFORMANCE, SIZE, SIZE, 0.01)), "SparseStorage (READ)"));
        ret.add(new NamedSupplier<>(() -> new Matrix(SIZE, SIZE, new Storage2D(new ArrayStorage<>(), new ArrayStorage<>())), "ArrayStorage"));
        ret.add(new NamedSupplier<>(() -> new Matrix(SIZE, SIZE,  new Storage2D(new HashMapStorage<>(), new HashMapStorage<>())), "HashMapStorage"));
        ret.add(new NamedSupplier<>(() -> new Matrix(SIZE, SIZE,  new Storage2D(new ChunkedStorage<>(), new ChunkedStorage<>())), "ChunkedStorage"));
        return ret;
    }

    @Test
    public void initThenGetInOrder() throws Exception {
        final long SEED = 12345;
        Matrix matr = this.seqSupplier.get();
        Random r = new Random(SEED);
        int colMajIdx = 0;
        int totalValues = 0;

        final int maxStep = 400;
        while (colMajIdx < SIZE*SIZE) {
            colMajIdx += r.nextInt(maxStep);
            final int x = colMajIdx / SIZE;
            final int y = colMajIdx % SIZE;
            totalValues++;
            matr = matr.put(x, y, x);
        }
//        System.out.println("initialised");
//        if (matr.storage instanceof Changable2DStorage) {
//            ((Changable2DStorage) matr.storage).change(SparseStorage.USAGE.SIZE);
//        }

        r = new Random(SEED);
        colMajIdx = 0;

        while (colMajIdx < SIZE*SIZE) {
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

    @Test
    public void initThenGet() throws Exception {
        final int NONZERO = (int)(SIZE*SIZE*0.005);
        final long SEED = 12345;
        Matrix matr = this.seqSupplier.get();
        Random r = new Random(SEED);
        int totalValues = 0;
        int maxrow = -1;

        for (int i=0; i<NONZERO; ++i) {
            if (i % 10000 == 0) {
                System.out.println(Math.round(100.0*i/(NONZERO - 1))+"% ");
            }
            final int row = r.nextInt(SIZE);
            final int col = r.nextInt(SIZE);
//            System.out.println("setting [" + row + ", " + col + "]");
            matr = matr.put(row, col, row);
            maxrow = Math.max(maxrow, row);
            totalValues++;
        }
        System.out.println("initialised "+matr.storage.getClass().getSimpleName());
        assertThat(matr.storage.maxIdx(), is(maxrow+1));
        assertThat(matr.storage.maxIdxOverapproximation(), greaterThanOrEqualTo(maxrow+1));
        if (matr.storage instanceof Changable2DStorage) {
            ((Changable2DStorage) matr.storage).change(SparseStorage.USAGE.SIZE);
        }

        r = new Random(SEED);
        for (int i = 0; i< NONZERO; ++i) {
            final int row = r.nextInt(SIZE);
            final int col = r.nextInt(SIZE);
//            System.out.println("checking [" + row + ", " + col + "]");
            assertThat(matr.get(row, col), is(1.0*row));
        }

        System.out.println("total values: " + totalValues);
        try {
            System.out.println(matr.storage.bytesUsed() / (1024 * 1024) + "MB");
        } catch (Throwable ignore) {}
    }
}