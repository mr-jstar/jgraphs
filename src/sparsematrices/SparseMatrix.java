package sparsematrices;

/**
 *
 * @author jstar
 */
public interface SparseMatrix {

    double get(int i, int j);

    void set(int i, int j, double v);

    int size();

    default public double[] multiply(double[] x, int start, int end) {
        int n = size();
        double[] result = new double[end - start];
        for (int row = start; row < end; row++) {
            result[row] = 0.0;
            for (int col = 0; col < n; col++) {
                result[row] += get(row, col) * x[col];
            }
        }
        return result;
    }

    default public void multiply(double[] x, double[] result) {
        int n = size();
        for (int row = 0; row < n; row++) {
            result[row] = 0.0;
            for (int col = 0; col < n; col++) {
                result[row] += get(row, col) * x[col];
            }
        }
    }

    public void multiply4IS(double[] x, double[] xp, double[] b, int start, int end);

}
