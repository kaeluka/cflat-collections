package com.github.kaeluka.cflat.test.datastructures;

import com.github.kaeluka.cflat.Matrix;
import com.github.kaeluka.cflat.NativeMatrix;
import com.github.kaeluka.cflat.annotations.Cflat;
import com.github.kaeluka.cflat.storage.*;
import com.github.kaeluka.cflat.util.NamedSupplier;
import com.github.kaeluka.cflat.util.Storages;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import scala.Int;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class MatrixOperationsTest {
    @Parameterized.Parameter()
    public Supplier<Matrix> matrixSupplier;

    private static int M = 2;
    private static int N = 3;

    @SuppressWarnings("unchecked")
    @Parameterized.Parameters(name="{0}")
    public static Collection<Supplier<Matrix>> storages() {
        final ArrayList<Supplier<Matrix>> ret = new ArrayList<>();
        for (final Supplier<Storage> s : Storages.genericStorages()) {
            ret.add(new NamedSupplier<>(() -> new Matrix(M, N, new Storage2D(s.get(), s.get())), "Matrix(" + s + ")"));
        }
        ret.add(new NamedSupplier<>(() -> new Matrix(M, N, SparseStorage.getFor(SparseStorage.USAGE.SIZE)), "CSR"));
        return ret;
    }

    @Test
    public void A_times_x() {
        final int M = 2;
        final int N = 3;

        final AtomicInteger seq = new AtomicInteger(1);
        final Matrix A = Matrix.from(M, N, seq::getAndIncrement,
                new Storage2D<>(new ArrayStorage<>(), new ArrayStorage<>()));
        final Matrix A_x = A.multiply(2);
        assertThat(A_x.get(0,0), is(2.0));
        assertThat(A_x.get(0,1), is(4.0));
        assertThat(A_x.get(0,2), is(6.0));
        assertThat(A_x.get(1,0), is(8.0));
        assertThat(A_x.get(1,1), is(10.0));
        assertThat(A_x.get(1,2), is(12.0));
    }

    @Test
    public void identity_mult() {
        final AtomicInteger seq = new AtomicInteger(1);
        final Matrix A = Matrix.from(2, 3, seq::getAndIncrement,
                new Storage2D<>(new ArrayStorage<>(), new ArrayStorage<>()));
        final Matrix I = Matrix.identity(3,
                new Storage2D<>(new ArrayStorage<>(), new ArrayStorage<>()));

        final Matrix A_I = A.multiply(I);
        assertTrue(A.equalTo(A.multiply(I)));
        assertTrue(I.equalTo(I.multiply(I)));
    }

    @Test
    public void A_times_B() {

        final AtomicInteger seq = new AtomicInteger(1);
        final Matrix A = matrixSupplier.get()
                .initialize(M, N, seq::getAndIncrement);
        final Matrix B = matrixSupplier.get()
                .transpose()
                .initialize(N, M, seq::getAndIncrement);

//        final Matrix A = Matrix.from(2, 3, seq::getAndIncrement,
//                new Storage2D<>(new ArrayStorage<>(), new ArrayStorage<>()));
//        final Matrix B = Matrix.from(3, 2, seq::getAndIncrement,
//                new Storage2D<>(new ArrayStorage<>(), new ArrayStorage<>()));

        final Matrix A_B = A.multiply(B);
        assertThat(A_B.getRows(), is(2));
        assertThat(A_B.getCols(), is(2));
        assertThat(A_B.get(0,0), is(58.0));
        assertThat(A_B.get(0,1), is(64.0));
        assertThat(A_B.get(1,0), is(139.0));
        assertThat(A_B.get(1,1), is(154.0));
    }

    @Test
    public void transpose() {
        final int M = 2;
        final int N = 3;
        final AtomicInteger seq = new AtomicInteger(1);
        final Matrix emptyMatrix = matrixSupplier.get();
        final Matrix A = emptyMatrix.initialize(M, N, seq::getAndIncrement);
//        final Matrix A = Matrix.from(M,N, seq::getAndIncrement,
//                new Storage2D<>(new ArrayStorage<>(), new ArrayStorage<>()));

        final Matrix At = A.transpose();
        assertThat(At.getRows(), is(N));
        assertThat(At.getCols(), is(M));
        assertThat(At.get(0,0), is(1.0));
        assertThat(At.get(0,1), is(4.0));
        assertThat(At.get(1,0), is(2.0));
        assertThat(At.get(1,1), is(5.0));
        assertThat(At.get(2,0), is(3.0));
        assertThat(At.get(2,1), is(6.0));
    }

    @Test
    public void LUDecomposition() {
        final int N = 3;
        final Iterator<Integer> seq = Arrays.stream(new int[] {
                8, 1, 6,
                4, 9, 2,
                1, 5, 7}).iterator();
        final Matrix A = Matrix.from(N,N, seq::next,
                new Storage2D<>(new ArrayStorage<>(), new ArrayStorage<>()));
        final Matrix[] LU = A.LUDecomposition();
        final Matrix L = LU[0];
        final Matrix U = LU[1];
        System.out.println("L=\n" + L.pretty());
        System.out.println("U=\n" + U.pretty());

        assertTrue(L.multiply(U).equalTo(A, 0.00001));
    }

    @Test
    public void NativeLUDecompositionLarge() {
        final int N = 50;
        final Random random = new Random(12345);
//        final Iterator<Integer> seq =
//                new Storage2D<>(ArrayStorage.class));
        final NativeMatrix A = NativeMatrix.from(N, N, () -> random.nextDouble()*99.0);
        final NativeMatrix[] LU = A.LUDecomposition();
        final NativeMatrix L = LU[0];
        final NativeMatrix U = LU[1];

        assertTrue(L.multiply(U).equalTo(A, 0.00001));
    }
    @Test
    public void LUDecompositionLarge() {
        final int N = 50;
        final Random random = new Random(12345);
//        final Iterator<Integer> seq =
//                new Storage2D<>(ArrayStorage.class));
        final Matrix A = Matrix.from(N, N, () -> random.nextDouble()*99.0,
                new Storage2D<>(new ArrayStorage<>(), new ArrayStorage<>()));
        final Matrix[] LU = A.LUDecomposition();
        final Matrix L = LU[0];
        final Matrix U = LU[1];

        assertTrue(L.multiply(U).equalTo(A, 0.00001));
    }
}
