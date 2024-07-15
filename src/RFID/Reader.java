package RFID;
import java.util.*;
/**
 * Defines the reader
 * @author liuxuan
 *
 */
public class Reader{
	int rid;
	int x,y;//position
	int color;//the color of this reader after coloring 
	double weight = 1.0;
	int numUncovered = 0;
	HashSet<Subelement>  vSub = new HashSet<Subelement>(); //Subelement定义的是阅读器调用的子集，有多少个子集。
	HashSet<Tag> vTags = new HashSet<Tag>();//store the tags in this reader's region
	HashSet<Tag> resultStep = new HashSet<Tag>();//search result of basic Step
	HashSet<Tag> resultStepadv = new HashSet<Tag>();//search result of Protocol 2
	HashSet<Tag> resultCATS = new HashSet<Tag>();//search result of the Bloom filter based approach
	HashSet<Tag> resultCollect = new HashSet<Tag>();
	HashSet<Tag> resultBcast = new HashSet<Tag>();
	HashSet<Tag> resultItsp = new HashSet<Tag>();
 	int[] bloomfilter;
//	HashSet<Subelement> subs = new HashSet<Subelement>();
//	Hashtable<Integer,Subelement> vSub = new Hashtable<Integer,Subelement>();
//	Hashtable<Integer,Subelement> vCovered = new Hashtable<Integer,Subelement>();
	public Reader(int pid, int px, int py){
		rid = pid;
		x = px;
		y = py;
	}
	public String toString(){
		return "("+rid+","+x+","+y+")";
	}
	public String getStr(){
		return rid + "\t" + x + "\t" + y;
	}
	/**
	 * Record the search result for different protocols 
	 * @param algname
	 * @param rlt
	 */
	public void setResult(String algname, HashSet<Tag> rlt){
		if(algname.equals("Collect")){
			if(resultCollect == null){
				resultCollect = new HashSet<Tag>();
			}
			resultCollect.clear();
			resultCollect.addAll(rlt);
		}else if(algname.equals("Bcast")){
			if(resultBcast == null){
				resultBcast = new HashSet<Tag>();
			}
			resultBcast.clear();
			resultBcast.addAll(rlt);
		}else if(algname.equals("CATS")){
			if(resultCATS == null){
				resultCATS = new HashSet<Tag>();
			}
			resultCATS.clear();
			resultCATS.addAll(rlt);
		}else if(algname.equals("Step")){
			if(resultStep == null){
				resultStep = new HashSet<Tag>();
			}
			resultStep.clear();
			resultStep.addAll(rlt);
		}else if(algname.equals("Stepadv")){
			if(resultStepadv == null){
				resultStepadv = new HashSet<Tag>();
			}
			resultStepadv.clear();
			resultStepadv.addAll(rlt);
		}else if(algname.equals("ITSP")){
			if(resultItsp == null){
				resultItsp = new HashSet<Tag>();
			}
			resultItsp.clear();
			resultItsp.addAll(rlt);
		}
	}
	/**
	 * Test whether this reader contains tag t
	 * used in the bloom filter based searching protocol
	 * @param t
	 * @return
	 */
	public boolean contains(Tag t){
		int[] slots = t.slots;
		for(int i = 0; i < slots.length; i++){
			if(bloomfilter[slots[i]]!=1)
				return false;
		}
		return true;
	}	
}
