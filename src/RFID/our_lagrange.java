package RFID;

/**
 *  lagrange 插值算法
 * @author jiangjin Yin
 *
 */

public class our_lagrange {

	
	public static double[] lagrange_allValue(double[]x_labels, double[]y_labels, double[] all_points){
		
		int total_points = all_points.length; //[0-1, 默认设置为100个插值点吧,间隔设置为0.01]
		double []results = new double[total_points];
		
		
		for (int i = 0; i < results.length; i++) {
			results[i] = lagrange( x_labels,  y_labels, all_points[i]);
		}
		
		return results;
	}
	
	
	
	public static double lagrange(double[]x_labels, double[]y_labels, double one_value){
		
	    int j,k,m,n;
	    double fz,fm,x,y =0,t,A[],B[];

        m = x_labels.length;
        n = y_labels.length;
        x = one_value;
        A = x_labels;
        B = y_labels;

        for(k=0;k<m;k++){
            fz=1;
            fm=1;
            for(j=0;j<n;j++){
                if(j!=k)
                {
                    fz=fz*(x-A[j]);
                    fm=fm*(A[k]-A[j]);
                }
            }
            t = B[k]*fz/fm;
            y = y+t;
        }
        return y;
	}
	
	
}
