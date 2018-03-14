package com.github.kaeluka.cflat.test.datastructures;

import com.github.kaeluka.cflat.Matrix;
import com.github.kaeluka.cflat.storage.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class MatrixTest {
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
        ret.add(new NamedSupplier<>(() -> new Matrix(5000,5000, new Storage2D(new ArrayStorage<>(), new ArrayStorage<>())), "ArrayStorage"));
        ret.add(new NamedSupplier<>(() -> new Matrix(5000,5000,  new Storage2D(new HashMapStorage<>(), new HashMapStorage<>())), "HashMapStorage"));
        ret.add(new NamedSupplier<>(() -> new Matrix(5000,5000,  new Storage2D(new ChunkedStorage<>(), new ChunkedStorage<>())), "ChunkedStorage (coarse)"));
        return ret;
    }

    @Test
    public void initThenGetRowMajor() throws Exception {
        final int SIZE = 500;
        Matrix matr = this.seqSupplier.get();

        for (int i=0; i<SIZE; ++i) {
            if (i%(SIZE/10) == 0) {
                System.out.println(i * 50 / SIZE+"%");
            }
            for (int j=0; j<SIZE; ++j) {
                matr = matr.put(i, j, 1.0);
            }
        }
        for (int i=0; i<SIZE; ++i) {
            if (i%(SIZE/10) == 0) {
                System.out.println(50 + (i * 50 / SIZE)+"%");
            }
            double gotten = 0;
            for (int j=0; j<SIZE; ++j) {
                gotten = matr.get(i, j);
                assertThat(gotten, is(1.0));
                assert(matr.get(i, j) == 1);
            }
        }
        System.out.println("100%");
        try {
            System.out.println(matr.storage.bytesUsed() / (1024 * 1024) + "MB");
        } catch (Throwable ignore) {}
    }

    @Test
    public void initThenGetColMajor() throws Exception {
        final int SIZE = 500;
        Matrix matr = this.seqSupplier.get();

        for (int j=0; j<SIZE; ++j) {
            if (j%(SIZE/10) == 0) {
                System.out.println(j * 50 / SIZE+"%");
            }
            for (int i=0; i<SIZE; ++i) {
                matr = matr.put(i, j, 1.0);
            }
        }
        for (int j=0; j<SIZE; ++j) {
            if (j%(SIZE/10) == 0) {
                System.out.println(50 + (j * 50 / SIZE)+"%");
            }
            for (int i=0; i<SIZE; ++i) {
                assertThat(matr.get(i, j), is(1.0));
            }
        }
        System.out.println("100%");
        try {
            System.out.println(matr.storage.bytesUsed() / (1024 * 1024) + "MB");
        } catch (Throwable ignore) {}
    }

//    @Test
//    public void addThenGet() throws Exception {
//        final int ADD_N = 50000;
//        final List<Object> seq = this.seqSupplier.get();
//
//        for (int i=0; i<ADD_N; ++i) {
//            seq.add(i);
//        }
//        for (int i=0; i<ADD_N; ++i) {
//            assertThat(seq.get(i), is(i));
//        }
//
//    }
//
//    @Test
//    public void addThenRemoveFromStart() throws Exception {
//        final int ADD_N = 1000;
//        final List<Object> seq = seqSupplier.get();
//        for (int i=0; i<ADD_N; ++i) {
//            seq.add(i);
//        }
//        for (int i=0; i<ADD_N; ++i) {
//            seq.remove(0);
//        }
//        assertThat(seq.size(), is(0));
//    }
//
//    @Test
//    public void addThenRemoveFromEnd() throws Exception {
//        final int ADD_N = 5000;
//        final List<Object> seq = seqSupplier.get();
//        for (int i=0; i<ADD_N; ++i) {
//            seq.add(i);
//        }
//        for (int i=0; i<ADD_N; ++i) {
//            seq.remove(seq.size()-1);
//
//        }
//    }

}