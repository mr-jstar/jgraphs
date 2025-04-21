package graphs;

/**
 * Shortest paths with respect to a single source
 *
 * @author jstar
 */
public class AllToAllGraphPaths {

    public double[][] d;
    public int[][] p;
    public double minDist, maxDist;
    public int sMin, dMin, sMax, dMax;

    public AllToAllGraphPaths(double[][] d, int[][] p) {
        if (d.length < 1 || d.length != p.length || d[0].length != p[0].length) {
            throw new IllegalArgumentException("AllGraphPaths constructor: distance and precedessor arrays are not compatible!");
        }
        this.d = d;
        this.p = p;
        minDist = d[0][1];
        maxDist = dMin;
        sMin = sMax = 0;
        dMin = dMax = 1;
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                double x = d[i][j];
                if (i != j && x != Double.POSITIVE_INFINITY) {
                    if (x < minDist) {
                        minDist = x;
                        sMin = i;
                        dMin = j;
                    }
                    if (x > maxDist) {
                        maxDist = x;
                        sMax = i;
                        dMax = j;
                    }
                }
            }
        }
        System.out.println("Min: (" + sMin + "-" + dMin + ")=" + minDist);
        System.out.println("Max: (" + sMax + "-" + dMax + ")=" + maxDist);
    }

    public SingleSourceGraphPaths getSSPaths(int n) {
        double[] dn = new double[d[n].length];
        int[] pn = new int[p[n].length];
        System.arraycopy(d[n], 0, dn, 0, dn.length);
        System.arraycopy(p[n], 0, pn, 0, pn.length);
        return new SingleSourceGraphPaths(dn, pn);
    }
}
