 package RFID;

public class TimingScheme {
	
//	public static double onebittime = 0.2;//0.2ms
	//below is the time used in the original submission/////
//	public static double shorttime = 0.4;
//	public static double singleidtime = 2.4;
//	public static double collisiontime = 2.4;
	/**
	 * this is the time used in revised version
	 */
	/*public static double emptytime = 0.184;//0.184ms;
	public static double singleidtime = 2.42;
	public static double shorttime = 0.2;
	public static double acktime = 0.064;
	public static double collisiontime = 0.44;
	public static double inftime= 0.3;*/
	
	/**
	 * ICNP-jiangjin 
	 * Philips I-Codespecification
	 * separated by a time interval: 0.302ms.
	 * reader-to-tag: 26.5Kb/sec
	 * tag-to-reader: 53Kb/sec
	 * 
	 */
	/**
	public static double interval= 0.302; // 0.302ms
	public static double t_id= 3.927; // 3.9272ms(reader-tag)
	public static double onebit= 0.019; // 0.01888ms
	public static double t_s= 0.321; // nonempty slot
	public static double t_e= 0.302; // empty slot
	public static double t_long= 0.492; // long slot (10 bits)
	public static double onebit_reader2tag= 0.038; // one bit(reader-tag)
	*/
	
	//40kb/s both reader--tag
	public static double onebit= 0.025; // 一比特时间
	public static double t_cid= 1.2; // cid 32bits
	public static double t_id= 2.8; // 96bit tid
	public static double t_e= 0.4; // 空时隙
	public static double interval= 0.4; // 和空时隙一样
	public static double t_lambda= 2*t_id; // 初始化frame的时间。
	//public static double onebit_reader2tag= 0.025;
	
	public static double t_s= t_e + onebit; // nonempty slot
	
	/**
	 * EPC C1G2 -  symmetrical transmission - 62.5Kb/s.  0.016 bit/ms 
	 * 
	 */
	
	/*public static double interval= 0.266; // 0.302ms
	public static double t_id= 1.802; // 1.802ms(reader-tag)
	public static double onebit= 0.016; // 0.01888ms
	public static double t_s= 0.282; // nonempty slot
	public static double t_e= 0.266; // empty slot
	public static double onebit_reader2tag= 0.016; // one bit(reader-tag)
*/	
	
}
