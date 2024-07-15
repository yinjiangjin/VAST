package RFID;

import java.util.ArrayList;

public class lagrange_arraylist {
    /*插值
    1.拉格朗日插值法
    2.选取合适的节点
    */    
	static private double lagrangeInterpolate(final ArrayList<Double> x, final ArrayList<Double> y, double test) {//拉格朗日插值法
        /*插值
         * ArrayList<Double> x 映射关系自变量
         * ArrayList<Double> y 映射关系自变量
         * test 待插值位置*/
        double result = 0;
        for (int i = 0; i < x.size(); ++i) {
            double term = y.get(i);
            for (int j = 0; j < x.size(); ++j) {
                if (j != i)
                    term = term * (test - x.get(j)) / (x.get(i) - x.get(j));
            }
            result += term;
        }
        return result;
    }
 
}
