package RFID;

import java.util.Scanner;

public class lagrange_1 {
    public static void main(String args[]){
        Scanner reader =new Scanner(System.in);
        System.out.println("�������������ݳ���:");
        int N = reader.nextInt();
        double xi[] = new double[N];
        double yi[] = new double[N];
        System.out.println("��������������Ĳ�ֵ��xi:");
        for(int i = 0;i < xi.length;i++)
        {
            xi[i] = reader.nextDouble();
        }
        System.out.println("���������������ֵ���Ӧ�ĺ���ֵyi:");
        for(int j = 0;j < yi.length;j++)
        {
            yi[j] = reader.nextDouble();
        }
        double x,x2;
 
        System.out.println("�����������ղ�ֵ�����:");
        for(x=xi[0];x<=xi[xi.length-1];x+=0.01)
        {
            Lagrange M;
            M=new Lagrange(xi,yi,x);
            System.out.printf("f(%4.2f)=%f\t",x,M.pt());
        }
        System.out.println();
        System.out.println("�����뵥�������ֵ��ĿΪ:");
        int Num = reader.nextInt();
        System.out.println("Ҫ���xֵΪ:");
        double x3[]=new double[Num];
        for(int i=0;i<Num;i++){
            x3[i] = reader.nextDouble();
        }
        for(int j=0;j<Num;j++){
            double Num_x=x3[j];
            Lagrange L = new Lagrange(xi,yi,Num_x);
            System.out.println("f("+Num_x+")="+L.pt());
        }
    }
}
 
class Lagrange{
    int j,k,m,n;
    double fz,fm,x,y =0,t,A[],B[];
    Lagrange(double a[],double b[],double c) {
        m = a.length;
        n = b.length;
        x = c;
        A = a;
        B = b;
    }
    double pt(){
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