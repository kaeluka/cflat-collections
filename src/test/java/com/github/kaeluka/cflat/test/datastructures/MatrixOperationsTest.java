package com.github.kaeluka.cflat.test.datastructures;

import com.github.kaeluka.cflat.Matrix;
import com.github.kaeluka.cflat.NativeMatrix;
import com.github.kaeluka.cflat.Sequence;
import com.github.kaeluka.cflat.storage.NestedStorage;
import com.github.kaeluka.cflat.storage.Storage;
import com.github.kaeluka.cflat.util.Storages;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class MatrixOperationsTest {
    @Parameterized.Parameter()
    public Supplier<NestedStorage<Double>> mkStorage;

    private static int M = 2;
    private static int N = 3;

    @SuppressWarnings("unchecked")
    @Parameterized.Parameters(name="{0}")
    public static Collection<Supplier<NestedStorage<Double>>> storages() {
        return Storages.nestedStorages();
    }

    @Test
    public void A_times_x() {
        final int M = 2;
        final int N = 3;

        final AtomicInteger seq = new AtomicInteger(1);
        final Matrix A = Matrix.from(M, N, seq::getAndIncrement, mkStorage.get());
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
        final Matrix A = Matrix.from(2, 3, seq::getAndIncrement, mkStorage.get());
        final Matrix I = Matrix.identity(3, mkStorage.get());

        final Matrix A_I = A.multiply(I);
        assertTrue("must equal:\n" +
                "A=\n"+A.pretty()+"\n" +
                "A*I=\n"+ A_I.pretty(), A.equalTo(A_I));
//        assertTrue(I.equalTo(I.multiply(I)));
    }

    @Test
    public void identity_square() {
        final Matrix I = Matrix.identity(3, mkStorage.get());
        final Matrix I2 = I.multiply(I);
        assertTrue(I.equalTo(I2, 0.00001));
    }

    @Test
    public void A_times_B() {

        final AtomicInteger seq = new AtomicInteger(1);
        final Matrix A = new Matrix(M, N, mkStorage.get())
                .initialize(seq::getAndIncrement);
        final Matrix B = new Matrix(N, M, mkStorage.get())
                .initialize(seq::getAndIncrement);

        System.out.println(A.pretty()+"\n * \n"+B.pretty());

//        final Matrix A = Matrix.from(2, 3, seq::getAndIncrement,
//                new Storage2D<>(new ArrayStorage<>(), new ArrayStorage<>()));
//        final Matrix B = Matrix.from(3, 2, seq::getAndIncrement,
//                new Storage2D<>(new ArrayStorage<>(), new ArrayStorage<>()));

        final Matrix A_B = A.multiply(B);

        System.out.println("\n =\n"+A_B.pretty());

        assertThat(A_B.getRows(), is(2));
        assertThat(A_B.getCols(), is(2));
        assertThat(A_B.get(0,0), is(58.0));
        assertThat(A_B.get(0,1), is(64.0));
        assertThat(A_B.get(1,0), is(139.0));
        assertThat(A_B.get(1,1), is(154.0));
    }

    @Test
    public void rowSetTest() {
        final AtomicInteger seq = new AtomicInteger(N*M);
        final Matrix A = new Matrix(M,N, mkStorage.get())
                .initialize(seq::getAndDecrement);

        System.out.println(A.pretty());
        final Storage<Double> row = A.getRow(0);
        row.setRange(0, 1.0, N);
        System.out.println(A.pretty());

        for (int i = 0; i < N; i++) {
            assertThat(A.get(0, i), is(1.0));
        }
    }

    @Test
    public void colSetTest() {
        final AtomicInteger seq = new AtomicInteger(N*M);
        final Matrix A = new Matrix(M, N, mkStorage.get())
                .initialize(seq::getAndDecrement);

        System.out.println(A.pretty());
        final Storage<Double> colS = A.getCol(1);
        colS.setRange(0, 10.0, M);
        System.out.println(A.pretty());

        int v = N*M;
        for (int row = 0; row < M; row++) {
            assertThat(A.get(row, 0), is(1.0*v--));
            assertThat(A.get(row, 1), is(10.0)); v--;
            assertThat(A.get(row, 2), is(1.0*v--));
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void rowAssertionTest() {
        //create an NxN identity matrix:
        final Matrix A = Matrix.identity(N, mkStorage.get());
        // we're trying to shift a value outside the allowed range;
        // this must fail:
        System.out.println(A.pretty());
        A
                .getRow(2)
                .set(N, 10.0);
    }

    @Test
    public void rowSortTest() {
        final AtomicInteger seq = new AtomicInteger(N*M);
        final Matrix A = new Matrix(M, N, mkStorage.get())
                .initialize(seq::getAndDecrement);

        System.out.println(A.pretty());
        final Sequence<Double> row = new Sequence<>(A.getRow(0));
        Collections.sort(row);
        System.out.println(A.pretty());
        System.out.println(A.storage.getClass());

        for (int i = 0; i < N; i++) {
            assertThat(A.get(0, i), is(4.0 + i));
            assertThat(A.get(1, i), is(3.0 - i));
        }
    }

    @Test
    public void transpose() {
        final int M = 2;
        final int N = 3;
        final AtomicInteger seq = new AtomicInteger(1);
        final Matrix A = new Matrix(M, N, mkStorage.get())
                .initialize(seq::getAndIncrement);
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
        final Matrix A = Matrix.from(N,N, seq::next, mkStorage.get());
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
        final Matrix A = Matrix.from(N, N, () -> random.nextDouble()*99.0, mkStorage.get());
        final Matrix[] LU = A.LUDecomposition();
        final Matrix L = LU[0];
        final Matrix U = LU[1];

        assertTrue(L.multiply(U).equalTo(A, 0.00001));
    }
}
