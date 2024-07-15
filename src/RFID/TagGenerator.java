package RFID;
import java.util.*;
public class TagGenerator {
	private static Hashtable<Integer,Tag> tagPool = new Hashtable<Integer,Tag>();
	private static Random rand = new Random();
	private static int curidx = -1;
	/**
	 * Generate n tags that are scattered in a xrange X yrange area
	 * @param ntag
	 * @param xrange
	 * @param yrange
	 * @return
	 */
	public static HashSet<Tag> getnewTags(int n, int xrange, int yrange){
		int tagcount = 0;
		HashSet<Tag> vret = new HashSet<Tag>();
		while(tagcount < n){
			Integer key = new Integer(rand.nextInt(Integer.MAX_VALUE));
			if(tagPool.containsKey(key))
				continue;
			else{
				double x = rand.nextDouble()*xrange;
				double y = rand.nextDouble()*yrange;
				Tag t = new Tag(key,x,y);
				vret.add(t);
				tagPool.put(key, t);
				tagcount++;
			}
		}
		return vret;
	}
	/**
	 * ��������
	 */
	
	public static HashSet<Tag> getnewTags(int n, int xrange, int yrange, int fingerLength){
		int tagcount = 0;
		HashSet<Tag> vret = new HashSet<Tag>();
		while(tagcount < n){
			Integer key = new Integer(rand.nextInt(Integer.MAX_VALUE));
			if(tagPool.containsKey(key))
				continue;
			else{
				double x = rand.nextDouble()*xrange;
				double y = rand.nextDouble()*yrange;
				//Tag t = new Tag(key,x,y);
				Tag t = new Tag(key,x,y,fingerLength); //���ô�ָ�Ƴ��ȵĹ��캯��
				vret.add(t);
				tagPool.put(key, t);
				tagcount++;
			}
		}
		return vret;
	}
	
	/**
	 * 
	 */
	public static HashSet<Tag> getnewTags(int n, int xrange, int yrange, int fingerLength, int lambda){
		int tagcount = 0;
		HashSet<Tag> vret = new HashSet<Tag>();
		while(tagcount < n){
			Integer key = new Integer(rand.nextInt(Integer.MAX_VALUE));
			if(tagPool.containsKey(key))
				continue;
			else{
				double x = rand.nextDouble()*xrange;
				double y = rand.nextDouble()*yrange;
				//Tag t = new Tag(key,x,y);
				Tag t = new Tag(key,x,y,fingerLength); //���ô�ָ�Ƴ��ȵĹ��캯��
				
				vret.add(t);
				tagPool.put(key, t);
				tagcount++;
			}
		}
		return vret;
	}
	
	public static void gettotalTags(int n, int xrange, int yrange){
		int tagcount = 0;
		int add = n - tagPool.size();
//		System.out.println("add is " + add);
		while(tagcount < add){
			Integer key = new Integer(rand.nextInt(Integer.MAX_VALUE));
			if(tagPool.containsKey(key))
				continue;
			else{
				double x = rand.nextDouble()*xrange;
				double y = rand.nextDouble()*yrange;
				Tag t = new Tag(key,x,y);
				tagPool.put(key, t);
				tagcount++;
			}
		}
	}
	/**
	 * ������ָ�Ƶı�ǩ
	 * @param n
	 * @param xrange
	 * @param yrange
	 * @param fingerLength
	 */
	public static void gettotalTags(int n, int xrange, int yrange, int fingerLength){
		int tagcount = 0;
		int add = n - tagPool.size();
//		System.out.println("add is " + add);
		while(tagcount < add){
			Integer key = new Integer(rand.nextInt(Integer.MAX_VALUE));
			if(tagPool.containsKey(key))
				continue;
			else{
				double x = rand.nextDouble()*xrange;
				double y = rand.nextDouble()*yrange;
				//Tag t = new Tag(key,x,y);
	
				Tag t = new Tag(key,x,y,fingerLength); //���ô�ָ�Ƴ��ȵĹ��캯��
				tagPool.put(key, t);
				tagcount++;
			}
		}
	}
	
	public static void gettotalTags(int n, int xrange, int yrange, int fingerLength, int lambda, int[] groupNumArr, int categoryBitLength, boolean CIDrandom){
		
		//����CID����
		int[] CIDArr;
		if(CIDrandom){//����������category ID
			CIDArr = categoryIDGenerator(categoryBitLength, groupNumArr.length);
		}else{
			System.out.println("*********** ��������category ID******************");
			//��������
			CIDArr = 	new int[groupNumArr.length]; 
			for (int i = 0; i < CIDArr.length; i++) {
				CIDArr[i] = i; //��ʼ��ȫΪ-1
			}
		}
		
		
		System.out.println("n:"+n);
		for (int i = 0; i < groupNumArr.length; i++) {
			System.out.println(""+i+"-----"+ groupNumArr[i]);
		}
		int tagcount = 0;
		int i=0;
		int add = n - tagPool.size();
//		System.out.println("add is " + add);
		while(tagcount < add){
			Integer key = new Integer(rand.nextInt(Integer.MAX_VALUE));
			if(tagPool.containsKey(key))
				continue;
			else{
				//���ȷ�����ǩ��Ŀ��
				if(groupNumArr[i]<=0){//
					i++;
				}
				double x = rand.nextDouble()*xrange;
				double y = rand.nextDouble()*yrange;
				//Tag t = new Tag(key,x,y);
	
				Tag t = new Tag(key,x,y,fingerLength, lambda, CIDArr[i], categoryBitLength); //���ô�ָ�Ƴ��ȵĹ��캯��
				tagPool.put(key, t);
				tagcount++;
				
				groupNumArr[i]--; //�����ı�ǩ��Ŀ����1
				
				if(groupNumArr[lambda-1]==0)
					break;
			}
		}
	}
	
	/**
	 * �������category ID.
	 * @param categoryBitLength
	 * @param categoryNum
	 * @return
	 */
	static int[] categoryIDGenerator(int categoryBitLength, int categoryNum){
		
		int[] categoryArr = new int[categoryNum]; 
		for (int i = 0; i < categoryArr.length; i++) {
			categoryArr[i] = -1; //��ʼ��ȫΪ-1
		}
	
		int index = categoryBitLength;
		if(index>30){
			index = 30; //�³��������ܹ���ʾ�ķ�Χ��
		}
		int upperbound = (int) Math.pow(2, index);
		
		int i = 0;
		double seed1 = 0;
		int CID_i =-1;
		boolean flag = true;
		while(i < categoryNum){
			seed1 = rand.nextDouble();
		    CID_i = (int) Math.floor(seed1*upperbound);
			for (int j = 0; j < i; j++) {
				if( categoryArr[j] == categoryArr[i] ){
					flag = false;
					break;
				}
			}
			
			if(flag){ //���ɵ�CID�ɹ�����������һ��
				categoryArr[i] = CID_i;
				i++;
			}
		}
		return categoryArr;
		
	}
	
	
	public static void gettotalTags(int n, int xrange, int yrange, int fingerLength, int lambda, int[] groupNumArr){
		
		System.out.println("n:"+n);
		for (int i = 0; i < groupNumArr.length; i++) {
			System.out.println(""+i+"-----"+ groupNumArr[i]);
		}
		int tagcount = 0;
		int i=0;
		int add = n - tagPool.size();
//		System.out.println("add is " + add);
		while(tagcount < add){
			Integer key = new Integer(rand.nextInt(Integer.MAX_VALUE));
			if(tagPool.containsKey(key))
				continue;
			else{
				//���ȷ�����ǩ��Ŀ��
				if(groupNumArr[i]<=0){//Ӧ�ÿ���
					i++;
				}
				double x = rand.nextDouble()*xrange;
				double y = rand.nextDouble()*yrange;
				//Tag t = new Tag(key,x,y);
	
				Tag t = new Tag(key,x,y,fingerLength, lambda, i); //���ô�ָ�Ƴ��ȵĹ��캯��
				tagPool.put(key, t);
				tagcount++;
				
				groupNumArr[i]--; //�����ı�ǩ��Ŀ����1
				
				if(groupNumArr[lambda-1]==0)
					break;
			}
		}
	}
	
	public static void gettotalTags(int n, int xrange, int yrange, int fingerLength, int lambda){
		int tagcount = 0;
		int add = n - tagPool.size();
//		System.out.println("add is " + add);
		while(tagcount < add){
			Integer key = new Integer(rand.nextInt(Integer.MAX_VALUE));
			if(tagPool.containsKey(key))
				continue;
			else{
				double x = rand.nextDouble()*xrange;
				double y = rand.nextDouble()*yrange;
				//Tag t = new Tag(key,x,y);
	
				Tag t = new Tag(key,x,y,fingerLength, lambda); //���ô�ָ�Ƴ��ȵĹ��캯��
				tagPool.put(key, t);
				tagcount++;
			}
		}
	}
	
	/*
	 * ��̬����range
	 */
	static void generateDynamicRange(HashSet<Tag> hset, int lambda){
		
		for(Tag t:hset){
			t.getRangeIndicator(lambda);  // 
		}
	}
	
	
	
	public static void flush(){
		curidx = -1;
//		tagPool.clear();
	}
	public static void realflush(){
		curidx = -1;
		tagPool.clear();
	}
	public static void resetidx(){
		curidx = -1;
	}
	/**
	 * ��tagPool����n��tag
	 */
	public static HashSet<Tag> getTags(int n){
		int idx = 0;
		int added = 0;
		Enumeration<Tag> ite = tagPool.elements();
		HashSet<Tag> hsret = new HashSet<Tag>();
		while(ite.hasMoreElements()){
			if(idx >= curidx){
				hsret.add(ite.nextElement());
				added++;
				idx++;
				if(added == n ){
					break;
				}
			}else{
				ite.nextElement();
				idx++;
			}
		}
//		System.out.println("added is " + added + ", n is " + n);
	//	curidx = idx; //�Ұ���仰ע���ˡ���Ϊÿ���㷨��Ҫ�ñ�ǩ��������Ҫע�͵���仰��(STEP����ע����仰)
		return hsret;
	}
	
	/**
	 * ��tagPool����n��tag�� ������CID���
	 */
	public static HashSet<Tag> getTags(int n, int CID){
		int idx = 0;
		int added = 0;
		Enumeration<Tag> ite = tagPool.elements();
		HashSet<Tag> hsret = new HashSet<Tag>();
		System.out.println("CID:"+CID);
		while(ite.hasMoreElements()){
			if( (idx >= curidx) ){
				Tag t = ite.nextElement();
				if(t.categoryID==CID){
					hsret.add(t);
					added++;
					idx++;
					if(added == n ){
						break;
					}
				}
				
			}else{
				ite.nextElement();
				idx++;
			}
		}
//		System.out.println("added is " + added + ", n is " + n);
	//	curidx = idx; //�Ұ���仰ע���ˡ���Ϊÿ���㷨��Ҫ�ñ�ǩ��������Ҫע�͵���仰��(STEP����ע����仰)
		return hsret;
	}
	
	
}
