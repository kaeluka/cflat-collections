package com.github.kaeluka.cflat;

import com.github.kaeluka.cflat.annotations.Cflat;
import com.github.kaeluka.cflat.storage.Storage;
import com.github.kaeluka.cflat.util.Mutable;

import java.util.function.DoubleSupplier;

@Cflat("*(step)->ok")
public class Matrix {
    private final int rows;
    private final int cols;
    public final Storage<Storage<Double>> storage;

    public Matrix(final int rows, final int cols, final Storage<Storage<Double>> storage) {
        assert(storage != null);
        this.storage = storage;
        this.rows = rows;
        this.cols = cols;
    }

    public int getRows() {
        return this.rows;
    }

    public int getCols() {
        return this.cols;
    }

    public Matrix initialize(final int rows,
                           final int cols,
                           DoubleSupplier f) {
        Matrix ret = this;
        for (int row=0; row<rows; ++row) {
            for (int col=0; col<cols; ++col) {
                ret = ret.put(row, col, f.getAsDouble());
            }
        }
        return ret;
    }

    public static Matrix from(final int rows,
                                final int cols,
                                DoubleSupplier f,
                                Storage<Storage<Double>> st) {
        Matrix m = new Matrix(rows, cols, st);
        for (int row=0; row<rows; ++row) {
            for (int col=0; col<cols; ++col) {
                m = m.put(row, col, f.getAsDouble());
            }
        }
        return m;
    }

    public double get(int i, int j) {
        Double v = storage
                .get(new MatrixIdx().step_nth(i).ok())
                .get(new MatrixIdx().step_nth(j).ok());
        if (v == null) {
            return 0.0;
        } else {
            return v;
        }
    }

    public Matrix put(int i, int j, double val) {
        storage.get(i).set(j, val);
        return this;
    }

    public Matrix multiply(final double x) {
        Matrix res = new Matrix(this.rows, this.cols, this.storage.emptyCopy());
        this.storage.foreachNonNull(row ->
                this.storage.get(row).foreachNonNull(col ->
                        res.put(row, col, this.get(row, col)*x))
        );
        return res;
    }

    public Matrix multiply(Matrix other) {
        Matrix res = new Matrix(this.rows, other.cols, this.storage.emptyCopy());
        if (this.cols != other.rows) {
            throw new IllegalArgumentException("matrix dimensions don't match!");
        }
        this.storage.foreachNonNull(i -> {
            other.storage.foreachNonNull(j -> {
                Mutable<Double> cell = new Mutable<>(0.0);
                this.storage.get(i).foreachNonNull(k -> {
                    cell.x += this.get(i, k) * other.get(k, j);
                });
                res.put(i, j, cell.x);
            });
        });
        return res;
    }

    public Matrix transpose() {
        final Matrix ret = new Matrix(this.cols, this.rows, storage.emptyCopy());
        storage.foreachNonNull(row ->
                storage.get(row).foreachNonNull(col ->
                        ret.put(col, row, this.get(row, col))));
        return ret;
    }

    public String pretty() {
        StringBuilder ret = new StringBuilder();
        for (int row = 0; row < rows; ++row) {
            StringBuilder line = new StringBuilder();
            for (int col = 0; col < cols; ++col) {
                String item = String.format("%g", get(row, col));
                if (item.length() > 30) {
                    item = item.substring(0,28)+"..";
                } else if (item.length() < 30) {
                    item = String.format("%10s", item);
                }
                line.append(item);
                line.append("\t");
            }
            line.append("\n");
            ret.append(line);
        }
        return ret.toString();
    }

    public Matrix[] LUDecomposition2() {
        assert(rows == cols);
        final int N = rows;
        Matrix L = Matrix.identity(N, this.storage.emptyCopy());
        Matrix U = this.copy();

        for (int n=0; n<N-1; ++n) {
            final double a_nn = U.get(n,n);
            final double a_n1n = U.get(n+1, n);
            final double multiplier = a_n1n/a_nn;
            for (int i=n; i<N; ++i) {
                U.put(n+1,i,
                        U.get(n+1,i)-multiplier*U.get(n,i));
                L.put(n+1,n, multiplier);
            }
        }

        return new Matrix[]{ L, U };
    }

    public Matrix[] LUDecomposition() {
        assert(rows == cols);
        final int N = rows;
        Matrix L = Matrix.identity(N, this.storage.emptyCopy());
        Matrix U = this.copy();

        for (int n=0; n<N-1; ++n) {
//            System.out.println(U.pretty());
            for (int r=n+1; r<N; ++r) {
                final double a_nn = U.get(n,n);
                final double a_n1n = U.get(r, n);
                final double multiplier = a_n1n/a_nn;
                for (int i = n; i < N; ++i) {
                    U.put(r, i,
                            U.get(r, i) - multiplier * U.get(n, i));
                    L.put(r, n, multiplier);
                }
            }
        }

        return new Matrix[]{ L, U };
    }

    @Override
    public String toString() {
        return "Matrix("+this.rows+" x "+this.cols+")\n"+
                this.storage;
    }

    public static Matrix identity(final int N, final Storage<Storage<Double>> st) {
        final Matrix ret = new Matrix(N, N, st);
        for (int i=0; i<N; ++i) {
            ret.put(i,i, 1);
        }
        return ret;
    }

    public boolean equalTo(final Matrix other) {
        if (this.rows != other.rows || this.cols != other.cols) {
            return false;
        }
        for (int i=0; i<this.rows; ++i) {
            for (int j=0; j<this.cols; ++j) {
                if (get(i,j) != other.get(i,j)) {
                    return false;
                }
            }
        }
        return true;
    }
    public boolean equalTo(final Matrix other, double precision) {
        if (this.rows != other.rows || this.cols != other.cols) {
            return false;
        }
        for (int i=0; i<this.rows; ++i) {
            for (int j=0; j<this.cols; ++j) {
                if (Math.abs(get(i,j) - other.get(i,j)) > precision) {
                    return false;
                }
            }
        }
        return true;
    }

    public Matrix copy() {
        return new Matrix(rows, cols, storage.copy());
    }
}
