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

public class ProtocolBasic {
	//number of tags to be searched(X), number of tags in the local interrogation region(Y), number of tags in (X INTERSEC Y) 
		HashSet<Tag> searchset = new HashSet<Tag>();//store the current T, may vary during the execution of searching protocol
		HashSet<Tag> localset = new HashSet<Tag>();//store the current M
		HashSet<Tag> searchbackup = new HashSet<Tag>();//store a copy of the original T and M, restore T and M for the second protocol
		HashSet<Tag> localbackup = new HashSet<Tag>();
		HashSet<Tag> toremove = new HashSet<Tag>();// a global variable to temporally store tags to be removed from T or M
		HashSet<Tag> result = new HashSet<Tag>();
		public static Random rand = new Random();//Random number generator
		//static icnptool gd = null;//A matlab program to compute the optimal framesize in the second protocol
		boolean mcanremove = true;//indicating whether M can be removed as fast as expected. When the removing ratio of M
								  //is significantly smaller than expected, stop removing M with Porotocol 2
		double stopthreshold = 0.5;//the threshold to control when to stop removing M with Protocol 2, 

		double tsearch;//search time in one round
		double totaltsearch;//the total time in the searching process, is the summarry of the largest tsearch 
							//all the rounds
		double totalbitssend;//the total number of bits transmitted from tags in the system
		double totalbitsrecv;//the total number of bits received by tags in the system
		static double  alpha;//the false positive rate
		HashSet<Tag> finalresult = new HashSet<Tag>();
		String ptname = "";
		static int terminatek = 5;//for Protocol 1 and Protocol 2
		/**
		 * Just initialize the matlab program gd;
		 */
		public ProtocolBasic(){
//			if(gd == null){
//				try{
//					gd = new icnptool();
//				}catch(Exception e){
//					e.printStackTrace();
//				}
//			}
		}
		public void reset(){
			searchset.clear();
			localset.clear();
			searchbackup.clear();
			localbackup.clear();
			toremove.clear();
			result.clear();
			mcanremove = true;
			finalresult.clear();
			tsearch = totaltsearch = totalbitssend = totalbitsrecv = -1;
		}
		/*********************** Commonly used functions **************************/
		/**
		 * Set the searching tag set T 
		 * Store a copy of T in searchbackup
		 */
		void setsearchtags(HashSet<Tag> sset){
			searchset.clear();
			searchset.addAll(sset);
		}
		/**
		 * Set local tag set M
		 * Store a copy of M in localbackup
		 */
		void setlocaltags(HashSet<Tag> lset){
			localset.clear();
			localset.addAll(lset);
		}
		/**
		 * Restore T and M from backup copy before the second the protocol
		 */
		void resetoreTagset(){
			searchset.clear();
			searchset.addAll(searchbackup);
			localset.clear();
			localset.addAll(localbackup);
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
		 * Remove those tags whose mapped slots are empty in replyptn, i.e., remove t if replyptn[t.collectidx] == 0 
		 * @param tset
		 * @param replyptn
		 * @return
		 */
		int filter(HashSet<Tag> tset, int[] replyptn){
			//toremove is a global variable, to save space
			toremove.clear();
			for(Tag t:tset){
				if(replyptn[t.slotidx]==0){
					toremove.add(t);
				}
			}
			tset.removeAll(toremove);
			return toremove.size();
		}
		/**
		 * Stat how many tags are actually found
		 * @return
		 */
		double getmissratio(){
//			System.out.println("result is " + searchset.size());
			int nactual = 0;
			for(Tag t:localbackup){
				if(searchbackup.contains(t)){
					nactual++;
				}
			}
//			System.out.println("result is " + searchset.size() + ", actual result should be " + nactual);
			return 0;
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
		public void logdata(Params para){
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
		public void logdataitsp(Params para){
			String prefix = para.getPrefix();
			String data = totaltsearch + "\t" + totalbitssend + "\t" + totalbitsrecv + "\t" + finalresult.size();
			try{
				File dir = new File(para.paraname);
				dir.mkdirs();
				File datafile = new File(dir.getAbsolutePath(),ptname + "0001.txt");
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
		}
}
