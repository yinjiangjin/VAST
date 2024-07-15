package RFID;

import java.util.ArrayList;

public class lagrange_arraylist {
    /*��ֵ
    1.�������ղ�ֵ��
    2.ѡȡ���ʵĽڵ�
    */    
	static private double lagrangeInterpolate(final ArrayList<Double> x, final ArrayList<Double> y, double test) {//�������ղ�ֵ��
        /*��ֵ
         * ArrayList<Double> x ӳ���ϵ�Ա���
         * ArrayList<Double> y ӳ���ϵ�Ա���
         * test ����ֵλ��*/
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
