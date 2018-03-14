package com.github.kaeluka.cflat;

import com.github.kaeluka.cflat.util.Mutable;

import java.util.Arrays;
import java.util.function.DoubleSupplier;

public class NativeMatrix {
    private final int rows;
    private final int cols;
    private final Double[] data;

    public NativeMatrix(final int rows, final int cols) {
        this(rows, cols, new Double[rows*cols]);
    }
    public NativeMatrix(final int rows, final int cols, Double[] data) {
        this.rows = rows;
        this.cols = cols;
        this.data = data;
    }

    public int getRows() {
        return this.rows;
    }

    public int getCols() {
        return this.cols;
    }

    public static NativeMatrix from(final int rows,
                                    final int cols,
                                    DoubleSupplier f) {
        NativeMatrix m = new NativeMatrix(rows, cols);
        for (int row=0; row<rows; ++row) {
            for (int col=0; col<cols; ++col) {
                m = m.put(row, col, f.getAsDouble());
            }
        }
        return m;
    }

    public double get(int i, int j) {
        final Double ret = data[i * cols + j];
        if (ret != null) {
            return ret;
        } else {
            return 0.0;
        }
    }

    public NativeMatrix put(int i, int j, double val) {
        this.data[i*cols+j] = val;
        return this;
    }

    public NativeMatrix multiply(final double x) {
        NativeMatrix res = new NativeMatrix(this.rows, this.cols);
        for (int row=0; row<rows; ++row) {
            for (int col=0; col<cols; ++col) {
                res.put(row, col, this.get(row, col)*x);
            }
        }
        return res;
    }

    public NativeMatrix multiply(NativeMatrix other) {
        NativeMatrix res = new NativeMatrix(this.rows, other.cols);
        if (this.cols != other.rows) {
            throw new IllegalArgumentException("matrix dimensions don't match!");
        }
        for (int i=0; i<rows; ++i) {
            for (int j=0; j<other.rows; ++j) {
                Mutable<Double> cell = new Mutable<>(0.0);
                for (int k=0; k<cols; ++k) {
                    cell.x += this.get(i, k) * other.get(k, j);
                }
                res.put(i, j, cell.x);
            }
        }
        return res;
    }

    public NativeMatrix transpose() {
        final NativeMatrix ret = new NativeMatrix(this.cols, this.rows);
        for (int row=0; row<rows; ++row) {
            for (int col=0; col<cols; ++col) {
                ret.put(col, row, this.get(row, col));
            }
        }
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

    public NativeMatrix[] LUDecomposition() {
        assert(rows == cols);
        final int N = rows;
        NativeMatrix L = NativeMatrix.identity(N);
        NativeMatrix U = this.copy();

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

        return new NativeMatrix[]{ L, U };
    }

    @Override
    public String toString() {
        return "Matrix("+this.rows+" x "+this.cols+")";
    }

    public static NativeMatrix identity(final int N) {
        final NativeMatrix ret = new NativeMatrix(N, N);
        for (int i=0; i<N; ++i) {
            ret.put(i,i, 1);
        }
        return ret;
    }

    public boolean equalTo(final NativeMatrix other) {
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
    public boolean equalTo(final NativeMatrix other, double precision) {
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

    public NativeMatrix copy() {
        return new NativeMatrix(rows, cols, Arrays.copyOf(data, data.length));
    }
}
