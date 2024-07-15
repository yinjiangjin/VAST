package RFID;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.Random;

public class Utils {
	public static String param = "default";
	
	public static double distance(Reader r1, Reader r2){
		double tmp = 0;
		tmp += (r1.x - r2.x)*(r1.x - r2.x) + (r1.y - r2.y)*(r1.y - r2.y);
		return Math.sqrt(tmp);
	}
	public static double distance(Reader r, Point p){
		double tmp = 0;
		tmp += (r.x - p.x)*(r.x-p.x) + (r.y-p.y)*(r.y-p.y);
		return Math.sqrt(tmp);
	}
	public static double distance(Reader r, Tag t){
		double tmp = 0;
		tmp += (r.x-t.x)*(r.x-t.x) + (r.y- t.y)*(r.y-t.y);
		return Math.sqrt(tmp);
	}
	public static void logAppend(String fname, String data){
		try{
			System.out.println(data);
			BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(fname,true));
			os.write(data.getBytes(),0,data.length());
			os.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * Hash function, using the md5 algorithm to product uniformly distributed hash values
	 * @param str
	 * @return
	 * @throws Exception
	 */
	public static long md5Hash(String str) throws Exception{
		MessageDigest md = MessageDigest.getInstance("MD5");  
        md.update(str.getBytes());  
        byte[] b = md.digest();  
        int i;  
        StringBuffer buf = new StringBuffer("");  
        for (int offset = 0; offset < b.length; offset++){  
                i = b[offset];  
                if (i < 0)  
                i += 256;  
                if (i < 16)  
                buf.append("0");  
                buf.append(Integer.toHexString(i));  
        }  
      //  System.out.println("buf:"+buf);//输入时128bit(16进制，32位)
        String md64 = buf.toString().substring(9,24);//
     //   System.out.println("md64:"+md64);
        long ret = Long.parseLong(md64,16);
      //  System.out.println("ret:"+ret);
        return ret;
	}
	
	
	public static long md5Hash_36(String str) throws Exception{
		MessageDigest md = MessageDigest.getInstance("MD5");  
        md.update(str.getBytes());  
        byte[] b = md.digest();  
        int i;  
        StringBuffer buf = new StringBuffer("");  
        for (int offset = 0; offset < b.length; offset++){  
                i = b[offset];  
                if (i < 0)  
                i += 256;  
                if (i < 16)  
                buf.append("0");  
                buf.append(Integer.toHexString(i));  
        }  
        String md64 = buf.toString().substring(9,18); //8位16进制数。
       // System.out.println("md64: "+md64);
        long ret = Long.parseLong(md64,16);
        return ret;
	}
	
	public static long md5Hash_32(String str) throws Exception{
		MessageDigest md = MessageDigest.getInstance("MD5");  
        md.update(str.getBytes());  
        byte[] b = md.digest();  
        int i;  
        StringBuffer buf = new StringBuffer("");  
        for (int offset = 0; offset < b.length; offset++){  
                i = b[offset];  
                if (i < 0)  
                i += 256;  
                if (i < 16)  
                buf.append("0");  
                buf.append(Integer.toHexString(i));  
        }  
        String md64 = buf.toString().substring(9,17); //8位16进制数。
      //  System.out.println("md64: "+md64);
        long ret = Long.parseLong(md64,16);
        return ret;
	}
	
	public static long md5Hash_60(String str) throws Exception{
		MessageDigest md = MessageDigest.getInstance("MD5");  
        md.update(str.getBytes());  
        byte[] b = md.digest();  
        int i;  
        StringBuffer buf = new StringBuffer("");  
        for (int offset = 0; offset < b.length; offset++){  
                i = b[offset];  
                if (i < 0)  
                i += 256;  
                if (i < 16)  
                buf.append("0");  
                buf.append(Integer.toHexString(i));  
        }  
        String md60 = buf.toString().substring(10,25); //32比特即可。
        
        long ret = Long.parseLong(md60,16);//15位16进制数
        //System.out.println("md60: "+md60);
        //System.exit(0);
        return ret;
	}
	
	/**
	 * 产生指纹
	 * @param str：标签的ID
	 * @param length: 指纹的长度
	 * @return
	 * @throws Exception
	 */
	public static int produceFingerprint( int length) {
		
		int max =(int) Math.pow(2, length);
		Random rand = new Random();
		int randSeed = rand.nextInt(max); //tag fingerprint
        return randSeed ;
	}
	
	public static void debug(String msg){
		System.out.println("/*********************************************************/");
		System.out.println(msg);
	}
}
