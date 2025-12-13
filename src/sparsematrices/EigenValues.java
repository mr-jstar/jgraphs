package sparsematrices;

/**
 *
 * @author jstar
 */
public class EigenValues {

    public static double powerIteration(SparseMatrix A, double tol, double[] eigenvectorOut) {
        // oblicza pierwszą wartość własną i pierwszy wektor własny dla rzadkiej macierzy A
        int n = A.size();
        int maxIter = n;
        double[] x = new double[n];
        double[] y = new double[n];

        // losowy wektor startowy
        java.util.Random rand = new java.util.Random(123);
        for (int i = 0; i < n; i++) {
            x[i] = rand.nextDouble();
        }

        normalize(x);

        double lambda = 0.0;

        for (int iter = 0; iter < maxIter; iter++) {
            // y = A * x
            A.multiply(x, y);

            // przybliżona wartość własną Rayleigha:
            // lambda ≈ (x^T y) / (x^T x) = (x^T y), bo x znormalizowane
            double num = 0.0;
            for (int i = 0; i < n; i++) {
                num += x[i] * y[i];
            }
            double lambdaNew = num;

            // norma różnicy wektora (po normalizacji)
            normalize(y);
            double diff = 0.0;
            for (int i = 0; i < n; i++) {
                double d = y[i] - x[i];
                diff += d * d;
            }

            // przepisz
            System.arraycopy(y, 0, x, 0, n);
            lambda = lambdaNew;
            System.out.println("it " + iter + ": " + lambda);

            if (Math.sqrt(diff) < tol) {
                break;
            }
        }

        System.arraycopy(x, 0, eigenvectorOut, 0, n);
        return lambda;
    }

    public static double powerIterationSecondEigen(SparseMatrix A, double tol, double[] v1, double[] eigenvectorOut) {
        // Oblicza drugi wektor własny, v1 to już znany, znormalizowany pierwszy wektor własny
        int n = A.size();
        int maxIter = n;

        double[] x = new double[n];
        double[] y = new double[n];

        java.util.Random rand = new java.util.Random(456);
        for (int i = 0; i < n; i++) {
            x[i] = rand.nextDouble();
        }
        // od razu ortogonalizujemy startowy wektor do v1
        projectOut(x, v1);
        normalize(x);

        double lambda = 0.0;

        for (int iter = 0; iter < maxIter; iter++) {
            // y = A * x
            A.multiply(x, y);

            // wyrzuć składową w kierunku v1 (deflacja)
            projectOut(y, v1);

            // lambda ≈ x^T y
            double num = 0.0;
            for (int i = 0; i < n; i++) {
                num += x[i] * y[i];
            }
            double lambdaNew = num;

            normalize(y);

            double diff = 0.0;
            for (int i = 0; i < n; i++) {
                double d = y[i] - x[i];
                diff += d * d;
            }

            System.arraycopy(y, 0, x, 0, n);
            lambda = lambdaNew;
            System.out.println("it " + iter + ": " + lambda);

            if (Math.sqrt(diff) < tol) {
                break;
            }
        }

        System.arraycopy(x, 0, eigenvectorOut, 0, n);
        return lambda;
    }

    private static void normalize(double[] v) {
        double norm = 0.0;
        for (double t : v) {
            norm += t * t;
        }
        norm = Math.sqrt(norm);
        if (norm == 0.0) {
            return;
        }
        for (int i = 0; i < v.length; i++) {
            v[i] /= norm;
        }
    }

    public static double dot(double[] x, double[]y) {
        double dot = 0.0;
        for (int i = 0; i < x.length; i++) {
            dot += x[i] * y[i];
        }
        return dot;
    }

    private static void projectOut(double[] y, double[] v) {
        double dot = dot(y,v);
        // y <- y - dot * v
        for (int i = 0; i < y.length; i++) {
            y[i] -= dot * v[i];
        }
    }
}
