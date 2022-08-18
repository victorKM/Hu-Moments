public class DistanceFunctions {
    public DistanceFunctions() {
    }

    static public double Manhattan(double[] q, double []p) {
        double sum = 0.0;
        for (int i = 0; i < 7; ++i){
            sum += Math.abs(q[i] - p[i]);
        }

        return sum;
    }

    static public double Euclide(double[] q, double []p) {
        double sum = 0.0;
        double sub = 0.0;
        for (int i = 0; i < 7; ++i){
            sub = Math.abs(q[i] - p[i]);
            sum += Math.pow(sub,2);
        }

        return Math.sqrt(sum);
    }

    static public double Chebychev(double[] q, double []p) {
        double max = Math.abs(q[0] - p[0]);
        double sub = 0.0;
        for (int i = 1; i < 7; ++i){
            sub = Math.abs(q[i] - p[i]);
            if (sub > max) {
                max = sub;
            }
        }
        return max;
    }
}
