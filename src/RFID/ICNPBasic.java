/**
 * Define some useful data structures and functions common to Protocol 1 and Protocol 2
 */
package RFID;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;



//import icnptool.icnptool;
//import com.mathworks.toolbox.javabuilder.*;

public class ICNPBasic {
		
		static boolean flag_key_missing = true;
		
		int totalMissingNumber_identified; //���ռ�����CIDs��Ŀ
	
		HashSet<Tag> serverset = new HashSet<Tag>(); //multi-category, �������ı�ǩ��
	
		HashSet<Tag> targetset = new HashSet<Tag>(); //KEY missing 
		HashSet<Tag> nontargetset = new HashSet<Tag>(); // KEY missing
		HashSet<Tag> localtagsset = new HashSet<Tag>(); // KEY missing
		
		int TOTAL_bits;   // KEY missing �ܵĴ�����ء�
		
		HashSet<Tag> targetsetbackup = new HashSet<Tag>();
		HashSet<Tag> nontargetsetbackup = new HashSet<Tag>();
		HashSet<Tag> tempremove = new HashSet<Tag>();
		HashSet<Tag> targetcollected = new HashSet<Tag>(); //�����Ѿ��ռ���Ϣ��target��ǩ
		HashSet<Tag> result = new HashSet<Tag>();
		
		HashSet<Tag> RQ_PM_Result = new HashSet<Tag>();
		HashSet<Tag> nontargetcollected = new HashSet<Tag>(); //�����Ѿ��ų���notarget��ǩ
		
		//FEAT ���� E-FEAT
		double ttime_pre_selection; //E-FEAT ��һ�׶�
		double ttime_tag_verify; //FEAT ���� E-FEAT �ڶ��׶�
		double ttime_hopping_selection; //FEAT ���� E-FEAT �����׶�
		
		double tbits_pre_selection; //E-FEAT ��һ�׶�  ������
		double tbits_tag_verify; //FEAT ���� E-FEAT �ڶ��׶�    ������
		double tbits_hopping_selection; //FEAT ���� E-FEAT �����׶� ������
		
		double total_rounds; //ִ����
		
		
		double ttime_filtering; // the total time for filtering ordinary tags in missing key tag identification
		
		//double ttimeVectorN; // the total time for broadcasting the notification vector V_n in PMI
		
		double tcollection; // collection time in one round
		double sumtcollection; //�ռ�����target��ǩ��ʱ��ͣ������ܵ�target��ǩ����ƽ���ռ�ÿ��target��ǩ��ʱ�䡣
		double eachcolortime; //ÿ�ֵ�������ʱ��
		double totalcollection;//the total time in the information process
		static int infLength=1; // ����missingʶ��Ĭ����Ϣ����Ϊ1
		int collecttotalnumber; //���ռ�����CIDs��Ŀ
		
		
		
		double TimeMMTI;
		double totalTimeMMTI;
		
		double TimeFirstPhase;
		double TimeSecondPhase;
		
		double TotalTimeFirstPhase;
		double TotalTimeSecondPhase;
		//double TotalTimeFirstPhaseTIC;
		//double TotalTimeSecondPhaseTIC;
		
		//����������ӣ���Ҫ��
		public static Random rand = new Random();//Random number generator
		public static Random randGauss = new Random();//Random number generator

		
		static int categoryBitNum =32; //PMI�����ռ��bit,Ĭ��32��
		//static int groupNum_Basic;
		static int categoryNum_Basic;//PMI,�����
		static int[] ActualCategoryNumArr;
		
		static double  alpha = 0.05;//the false positive rate
		HashSet<Tag> finalresult = new HashSet<Tag>();
		String ptname = "";  //Э�������
		
		/**
		 * Just initialize the matlab program;
		 */
		public ICNPBasic(){
		
		}
		public void reset(){
			
			targetset.clear();
			nontargetset.clear();
			localtagsset.clear();
			targetsetbackup.clear();
			nontargetsetbackup.clear();
			tempremove.clear();
			
			result.clear();
			
			finalresult.clear();
			
		}
		/*********************** Commonly used functions **************************/
		
		static void setActualCategoryNumArr(int categoryNum, int[] categoryNumArr){
			ActualCategoryNumArr = new int[categoryNum];
			for (int i = 0; i < categoryNumArr.length; i++) {
				ActualCategoryNumArr[i] = categoryNumArr[i];
			}
			
		}
		
		
		/**
		 * Set the target tag set W 
		 * Store a copy of T in searchbackup
		 */
		void settargetset( boolean flag){
			
			targetset.clear();
			if(flag){
				
				targetsetbackup.removeAll(targetcollected); //�Ƴ��õ������Ѿ��ռ���target��ǩ��
				targetset.addAll(targetsetbackup);
				System.out.println("�����һ���������ռ��ı�ǩ��Ŀ:"+targetcollected.size()); //�����һ���������ռ��ı�ǩ��Ŀ
				targetcollected.clear();
				
			}else{
				targetset.addAll(targetsetbackup);
				
			}
			
		}
		
		
		void setNontargetset( boolean flag){
			
			nontargetset.clear();
			if(flag){
				
				nontargetsetbackup.removeAll(nontargetcollected); //�Ƴ��õ������Ѿ��ռ���target��ǩ��
				nontargetset.addAll(nontargetsetbackup);
				nontargetcollected.clear();
				
			}else{
				
				nontargetset.addAll(nontargetsetbackup);
				
			}
			
		}

		void setServerset(HashSet<Tag> sset){
			
			serverset.clear();
			serverset.addAll(sset);
		}
		
		//�������� - ÿ����������ɺ�ɾ���Ѿ��ռ���Ŀ���ǩ ( Ӧ����PIC )
		void settargetset(HashSet<Tag> sset){
			targetset.clear();
			targetset.addAll(sset);
		}
		
		void setnontargetset(HashSet<Tag> sset){
			nontargetset.clear();
			nontargetset.addAll(sset);
		}
		
		/**
		 * Set local tag set N
		 * Store a copy of N in localbackup
		 */
		void setlocalset(HashSet<Tag> lset){
			localtagsset.clear();
			localtagsset.addAll(lset);
		}
		
		int[] getLogReply(int f, int seed, HashSet<Tag> tset){
			int[] ret = new int[f];
			for(int i = 0; i < f; i++){
				ret[i] = 0;
			}
			for(Tag t:tset){
				t.getLogSlot(f, seed);
				ret[t.slotidx]++;
			}
			return ret;
		}
		
		
		/**
		 * compute the reply vector when frame size is f and random number is seed, where tset is the tag set
		 * @param f
		 * @param seed
		 * @param tset
		 * @return
		 */
		int[] getReply(int f, int seed, HashSet<Tag> tset){
			int[] ret = new int[f];
			for(int i = 0; i < f; i++){
				ret[i] = 0;
			}
			for(Tag t:tset){
				t.getSlot(f, seed);
				ret[t.slotidx]++;
			}
			return ret;
		}
		
		/**
		 * second slot selection for IIP protocol
		 * @param f
		 * @param seed
		 * @param tset
		 * @return
		 */
		int[] getReply2(int f, int seed, HashSet<Tag> tset){
			int[] ret = new int[f];
			for(int i = 0; i < f; i++){
				ret[i] = 0;
			}
			for(Tag t:tset){
				t.getSlot2(f, seed);
				ret[t.slotidx2]++;
			}
			return ret;
		}
		
		
		/**
		 * �������ID��hash�������ͬ��ʱ϶��ͬ��
		 * @param f
		 * @param seed
		 * @param tset
		 * @return
		 */
		int[] getCategoryReply(int f, int seed, HashSet<Tag> tset){
			int[] ret = new int[f];
			for(int i = 0; i < f; i++){
				ret[i] = 0;
			}
			for(Tag t:tset){
				t.getCategorySlot(f, seed);
				ret[t.categoryslotidx]++;
			}
			return ret;
		}
		
		
		/**
		 * ���˷������ϵ�nontarget��ǩ
		 * @param ntset  	nontarget��ǩ����
		 * @param replyW 	target��ǩ��Ԥӳ��
		 * @param replyN 	nontarget��ǩ��Ԥӳ��
		 * @param replyL 	���ر�ǩ�ظ�����
		 * @return ���ط������ų���nontarget�ܱ�ǩ��Ŀ
		 */
		int filternontarget(HashSet<Tag> ntset,int[] replyW,int[] replyL){
			
			tempremove.clear();
			//����nontraget��ǩ���ַ�ʽ�� 1����ǩӳ��ı��ػظ�Ϊ0֤�����ڡ� 2�����ػظ�Ϊ1���������ų�ʱ϶��
			for(Tag t2:ntset){
				if(replyL[t2.slotidx]==0){ 
					tempremove.add(t2);
				}
			}
			for(Tag t2:ntset){
				if(replyW[t2.slotidx]==0){ //û��Ŀ���ǩӳ��
					tempremove.add(t2);  //����������set����Ԫ�ز����ظ�
				}
			}
			
			//System.out.println("before remove nontarget:"+ntset.size());
			ntset.removeAll(tempremove);
			//System.out.println("after remove nontarget:"+ntset.size());
			return tempremove.size();
		}
		
		
		/**
		 * Remove those tags whose mapped slots are single in replyptn, 
		 * because the tag who hashed in the single slots can be collection. i.e., remove t if replyptn[t.collectidx] == 1 
		 * @param tset
		 * @param replyptn
		 * @return
		 */
		int filterSingle(HashSet<Tag> tset, int[] replyptn){
			//toremove is a global variable, to save space
			tempremove.clear();
			for(Tag t:tset){
				if(replyptn[t.slotidx]==1){
					tempremove.add(t);
				}
			}
			tset.removeAll(tempremove);
			return tempremove.size();
		}
		
		/**
		 * Remove those tags whose mapped slots are empty in replyptn, i.e., remove t if replyptn[t.collectidx] == 0 
		 * @param tset
		 * @param replyptn
		 * @return
		 */
		int filter(HashSet<Tag> tset, int[] replyptn){
			//tempremove is a global variable, to save space
			tempremove.clear();
			for(Tag t:tset){
				if(replyptn[t.slotidx]==0){
					tempremove.add(t);
				}
			}
			//System.out.println("û��remove��"+tset.size());
			
			tset.removeAll(tempremove);
			//System.out.println("remove ��"+tset.size());
			
			return tempremove.size();
		}
		
		public void logdatasingle(String dirname, String fname, String data){
			try{
				File dir = new File(dirname);
				dir.mkdirs();
				File datafile = new File(dir.getAbsolutePath(),fname + ".txt");
				datafile.createNewFile();	
				Utils.debug(fname + "\t" + data);
				BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(datafile,true));
				os.write(data.getBytes(),0,data.length());
				os.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		
		public void logdata(ICNPBasic icnpbasic, Params para){
			String prefix = para.getPrefix2(); //ǰ׺
			String filename = icnpbasic.ptname;
			String data = prefix+" "+icnpbasic.totalcollection+"\t "+icnpbasic.TOTAL_bits+"\t "+icnpbasic.categoryNum_Basic +"\t Protocol_Missing: "
					+icnpbasic.totalMissingNumber_identified+"\t " +icnpbasic.ttime_filtering+"\t " 
					+icnpbasic.ttime_pre_selection+"\t "+icnpbasic.ttime_tag_verify+"\t "  
					+icnpbasic.ttime_hopping_selection+"\t "+icnpbasic.tbits_pre_selection+"\t "  
					+icnpbasic.tbits_tag_verify+"\t "+icnpbasic.tbits_hopping_selection+"\t "  
					+icnpbasic.total_rounds+"\t "  +"\n";
			
			
			try{
				//File dir = new File(filename);
				File dir = new File(para.paraname);
				dir.mkdirs();
				File datafile = new File(dir.getAbsolutePath(),ptname + ".txt");
				datafile.createNewFile();				  
				//data = prefix + "\t" +  data +"\n";
				Utils.debug(ptname + "\t" + data);
				BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(datafile,true));
				os.write(data.getBytes(),0,data.length());
				os.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
//			System.out.println(ptname + "\t" + totaltsearch + "\t" + totalbitssend + "\t" + totalbitsrecv + "\t" + finalresult.size());
		}
		
		
		/*ublic void logdata(Params para){
			String prefix = para.getPrefix();
			String data = totaltsearch + "\t" + totalbitssend + "\t" + totalbitsrecv + "\t" + finalresult.size();
			try{
				File dir = new File(para.paraname);
				dir.mkdirs();
				File datafile = new File(dir.getAbsolutePath(),ptname + ".txt");
				datafile.createNewFile();				  
				data = prefix + "\t" +  data +"\n";
				Utils.debug(ptname + "\t" + data);
				BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(datafile,true));
				os.write(data.getBytes(),0,data.length());
				os.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
//			System.out.println(ptname + "\t" + totaltsearch + "\t" + totalbitssend + "\t" + totalbitsrecv + "\t" + finalresult.size());
		}
		*/
		
		
		
		HashSet<Tag> findSlotTagSet(HashSet<Tag> hs, int i){
			HashSet<Tag> st = new HashSet<Tag>();
			for(Tag t: hs){
				if(t.slotidx == i){
					st.add(t);
				}
			}
			return st;
		}
		
}
