package RFID;
import java.util.*;
import java.io.*;

import coloring.Dsatur;
public class ReaderGenerator {
	public static double unit = 1; //又是什么
	public static int scale = 1; //是什么意思
	static Random rand = new Random();
	Reader[] ra = null;  //所有Reader的数目
	Reader[] coverset = null;
	int colors = -1;//the number of schedule rounds needed to schedule all readers
	int length = -1, width = -1;//the length and width of the deployment region
	double rcom = -1;  // communication range of readers
	double rinfer = -1; //interference range of readers
	double avglocalsize = -1;
	/**
	 * Specifies the width and length of the deployment region in which the readers are deployed
	 * @param l : length
	 * @param w : width
	 * @param rc: communication range of readers
	 * @param rf: interference range of readers
	 */
	public ReaderGenerator(int l, int w, double rc, double rf){
		length = l;
		width = w;	
		rcom = rc;
		rinfer = rcom*1.5;
		Utils.debug("length\t" + l + "\twidth " + w + "\trcom " + rcom + "\trinfer " + rinfer);
	}
	/**
	 * Generate randomly distributed readers that 
	 *    GUARANTEES that the union of coverage regions of all the readers can overlap the whole deployment region
	 *    @param nreader: the number of readers
	 */
	public void GenerateRdsRandom(int nreader){  //随机生成分布阅读器
		int times = 1;
		for(;;){
			ra = new Reader[nreader];
			int x=0,y=0;
			Reader rd = null;
			for(int i = 0; i < nreader; i++){
				x = rand.nextInt(length+1);
				y = rand.nextInt(width+1);
				rd = new Reader(i+1,x,y);
				ra[i] = rd;
			}
			if(cancover(ra,length,width))//test whether these readers covers the whole deployment region
				break;
			else{
				System.out.println("not full covered! Generating next " + times);
				times++;
			}
		}
	}
	/**
	 * Generated regular deployed readers, 
	 * This function first computes the number of readers needed to cover the deployment region
	 * Then gerenrate readers one by one by putting them on the grid points
	 * @param nreader
	 * @param regular
	 */
	public void GenerateRdsRegular(){ //正常部署阅读器
		//first calculate the separation distance between two adjacent sensors
		//and determine how many readers should be put on a row and on a column
		int step = (int)(Math.sqrt(2)*rcom*0.5); //floor
		int xnum = (int)(Math.ceil(length*0.5/step));
		int ynum = (int)(Math.ceil(width*0.5/step));
		ra = new Reader[xnum*ynum]; //总共部署的阅读器个数
		int x=0,y=0;
		Reader rd = null;
		for(int i = 1; i <= xnum; i++){
			for(int j = 1; j <= ynum; j++){
				int id = (i-1)*ynum + (j-1)+ 1;
				x = (2*i-1)*step;
				y = (2*j-1)*step;
				rd = new Reader(id,x,y);
				ra[id-1] = rd;
			}
		}
	}
	public Reader[] getReaders(){
		return ra;
	}
	/**
	 * Test whether readers in ra can cover the whole region 
	 * @param ra
	 * @param xrange
	 * @param yrange
	 * @return
	 */
	boolean cancover(Reader[] ra, int length, int width){
		double unit = 0.1;
		int scale = 10;
		
		Point pt = new Point(-1,-1);
		boolean regioncovered = true,ptcovered = false;
		for(int i=0; i <length*scale+1; i++){  //把覆盖区域长宽都放大10倍，这样每次步长为0.1 变成步长为1.
			for(int j = 0; j < width*scale+1; j++){
				//check if point(x,y) is covered by at least one reader
				pt.setPos(unit*i, unit*j);
				ptcovered = false;
				for(int k = 0; k < ra.length; k++){
					if(Utils.distance(ra[k], pt) < rcom){ //如果某一点到阅读器的距离小于通信距离说明在该阅读器覆盖范围下。
						ptcovered = true;
						break;
					}
				}
				if(ptcovered)
					continue;
				else{
					regioncovered = false;
//					System.out.println("("+pt.x+","+pt.y+") is not covered");
					continue;
				}
			}
		}
//		regioncovered = true;
		return regioncovered;
	}
	/**
	 * Partition the deployment region into small grids, and uses the combination of IDs of readers that
	 * can cover a grid as the signature of that grid, if there is no reader covers that grid, we give
	 * that grid a special signature "x"
	 * A grid with signature "x" means that it is not covered by any reader
	 * Grid sidelength is unit = 0.1
	 * @param ra
	 * @param xrange
	 * @param yrange
	 * @param scale
	 */
	Region[][] GenerateRegion(Reader[] ra, int xrange, int yrange){
		Region[][] regs = new Region[xrange*scale+1][yrange*scale+1]; //scale 是什么意思
		StringBuffer regsig = new StringBuffer();
		Region regtmp = null;
		Point pt = new Point(-1,-1);
		for(int i=0; i <xrange*scale+1; i++){
			for(int j = 0; j < yrange*scale+1; j++){
				//check if point(x,y) is covered by at least one reader
				regtmp = new Region();
				if(regsig.length() > 0)
					regsig.delete(0, regsig.length()); //delete(int start, int end) 移除此序列的子字符串中的字符。
				pt.setPos(unit*i, unit*j);  //设置点的位置
				for(int k=0; k < ra.length; k++){ 
					if(Utils.distance(ra[k], pt) < rcom){
						regsig.append(ra[k].rid+".");
					}
				}
				if(regsig.length() == 0){
					regsig.append("x");
				}
				regtmp.setSig(regsig.toString());
				regs[i][j]=regtmp;
			}
		}
		return regs;
	}
	/**
	 * Combine regions that have the same signature, which means that they are in the same atomic sub region
	 * @param rgs
	 * @return
	 */
	Vector<Subelement> getSubelements(Region[][] regs){
//		System.out.println("Geneaating subelements...");
		Vector<Subelement> vSubs = new Vector<Subelement>();
		while(true){
			int[] idxs = getNextRegion(regs);
			if(idxs[0] == -1) break;
			Subelement sub = new Subelement();
			sub.setSig(regs[idxs[0]][idxs[1]].sig);
//			for(int i =idxs[0]; i < regs.length; i++){
//				for(int j = idxs[1]; j <regs[i].length; j++){
//					if(regs[i][j] != null && sub.sig.equals(regs[i][j].sig)){
//						regs[i][j] = null;
//					}
//				}
//			}
			for(int i = 0; i < regs.length; i++){
				for(int j = 0; j <regs[i].length; j++){
					if(regs[i][j] != null && sub.sig.equals(regs[i][j].sig)){
						regs[i][j] = null;
					}
				}
			}
			vSubs.add(sub);
//			System.out.println("Readers of subelement " + sub.id + " is " + sub.sig + ", i is " + idxs[0] + ", j is " + idxs[1]);
		}
		return vSubs;
	}
	/**
	 * Relate the readers to their covered subelements
	 * @param subs
	 */
	public void associateReader(Vector<Subelement> subs){
//		System.out.println("Associating readers with subelements");
		String token = null;
		for(Subelement sub:subs){
			String rdstr = sub.sig;
			StringTokenizer st = new StringTokenizer(rdstr,".");
			while(st.hasMoreTokens()){
				token = st.nextToken();
				if(token.equals("x"))
					continue;
				int rid = Integer.parseInt(token);
				for(Reader rd:ra){
					if(rd.rid == rid){
//						rd.vSub.put(sub.id, sub);
						rd.vSub.add(sub);
					}
				}
			}
		}
		for(Reader rd:ra){
			rd.numUncovered = rd.vSub.size();
		}
	}
	public void clearTags(){
		for(Reader rd:ra){
			rd.vTags.clear();
			rd.resultCollect.clear();
			rd.resultBcast.clear();
			rd.resultCATS.clear();
			rd.resultStep.clear();
			rd.resultStepadv.clear();
		}
	}
	/**
	 * Compute the minimum covering reader subset
	 * @param rds
	 * @return
	 */
	
	public Vector<Reader> getMinCoveringReader(Reader[] rds, Vector<Subelement> subs){
//		System.out.println("Generating minimum covering reader set");
		Hashtable<Integer, Subelement> uset = new Hashtable<Integer,Subelement>();
		Hashtable<Integer, Subelement> covered = new Hashtable<Integer, Subelement>();
		Hashtable<Integer, Reader> rdset = new Hashtable<Integer, Reader>();
		Vector<Reader> coveringRd = new Vector<Reader>();
		for(Subelement s:subs){
			uset.put(s.id, s);
		}
		for(Reader rd:rds){
			rdset.put(rd.rid, rd);
		}
		int totalSub = subs.size();
		while(covered.size() != totalSub){
			//find the set that has least weight for covering per subelement
			Reader r = findMosteffective(rdset,uset);
			updateCovered(covered,r);
			coveringRd.add(r);
			updateuset(uset,r);
			rdset.remove(new Integer(r.rid));
		}
		return  coveringRd;
	}
	void updateCovered(Hashtable<Integer, Subelement> covered, Reader rd){
		for(Subelement sub:rd.vSub){
			covered.put(sub.id, sub);
		}
	}
	/**
	 * Remove subelements covered by rd from uset
	 * @param uset
	 * @param rd
	 */
	void updateuset(Hashtable<Integer, Subelement> uset, Reader rd){
		for(Subelement sub:rd.vSub){
			uset.remove(new Integer(sub.id));
		}
	}
	Reader findMosteffective(Hashtable<Integer, Reader> rdset, Hashtable<Integer,Subelement> uset){
		double price = 10000, curprice=price;
		Enumeration<Reader> rds = rdset.elements();
		Reader rdselected = null;
		while(rds.hasMoreElements()){
			Reader currd = rds.nextElement();
			double count = 0;
			for(Subelement sub:currd.vSub){
				if(uset.containsKey(sub.id))
					count++;
			}
			if(count!=0){
				curprice = currd.weight/count;
				if(price > curprice){
					price = curprice;
					rdselected = currd;
				}
			}
		}
		return rdselected;
	}
	int[] getNextRegion(Region[][] regs){
		int[] ret = new int[2];
		ret[0] = ret[1] = -1;
		for(int i = 0; i < regs.length; i++){
			for(int j = 0; j<regs[i].length; j++){
				if(regs[i][j] != null){
					ret[0] = i;
					ret[1] = j;
					return ret;
				}
			}
		}
		return ret;
	}
	void GetConflict(Reader[] ra, String filename){
		//get conflict graph of the readers 
		int[][] conn = new int[ra.length][ra.length];
		for(int i = 0; i < ra.length; i++){
			for(int j = 0; j < ra.length; j++){
				conn[i][j]=0;
			}
		}
		int m = 0;
		for(int i = 0; i < ra.length; i++){
			for(int j = i+1; j<ra.length; j++){
				if(Utils.distance(ra[i], ra[j]) < rcom*2 || Utils.distance(ra[i], ra[j]) < rinfer){
					conn[i][j] = 1;
					m++;
				}
			}
		}
		//write the conflict graph into a file
		StringBuffer sb = new StringBuffer();
		sb.append("p edge " + ra.length + " " + m + "\n");
		for(int i = 0; i < ra.length; i++){
			for(int j = i+1; j<ra.length; j++){
				if(conn[i][j] == 1){
					sb.append("e " + (i+1) + " " + (j+1) + "\n");
				}
			}
		}
		String data = sb.toString();
		try{
	    	BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(filename, false));
	
	    	os.write(data.getBytes(), 0, data.length());
	    	os.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	void logCovering(Reader[] ra){
		String data = "";
		for(Reader rd:ra){
			data += rd.getStr() + "\n";
		}
		try{
			BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream("covering",true));
			os.write(data.getBytes(),0,data.length());
			os.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	void getColorsAll(){
		GetConflict(ra,"allreader.col");
		try{
			Dsatur.coloring("allreader.col");
//			prefix = prefix + Dsatur.colornum + "\n";
//			Utils.logAppend("allcolor", prefix);
			System.out.println("all colors is " + Dsatur.colornum);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	int getColorsMin(){
		Region[][] regs= GenerateRegion(ra, length, width);
		Vector<Subelement> vSubs = getSubelements(regs);
//		System.out.println("There are totally " + vSubs.size() + " subelements");
		associateReader(vSubs);
		Vector<Reader> covering = getMinCoveringReader(ra, vSubs);
//		System.out.println("There are " + covering.size() + " readers in the covering subset");
		coverset = new Reader[covering.size()];
		int i = 0;
		for(Reader rd:covering){
//			System.out.println(rd.toString());
			coverset[i] = rd;
			i++;
		}
//		logCovering(coverset);
		GetConflict(coverset, "covering.col");
//		String prefix = Utils.getprefix() + ra.length + "\t";
		try{
			int[] colors = Dsatur.coloring("covering.col");
			if(colors.length != coverset.length)
				System.out.println("Error!");
			for(int m = 0; m < colors.length; m++){
				coverset[m].color = colors[m];
			}
//			prefix = prefix + Dsatur.colornum + "\n";
//			Utils.logAppend("mincover", prefix);
//			System.out.println("color number is " + Dsatur.colornum);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return Dsatur.colornum;
	}
	public int assoTagsReader(HashSet<Tag> vtags){
//		Utils.debug("tag num is " + vtags.size());
		for(Reader rd:coverset){
			rd.vTags.clear();
			for(Tag t:vtags){
//				System.out.println("(" + rd.x + "\t" + rd.y + ")(" + t.x +"," + t.y + ")distance is " + Utils.distance(rd, t));
				if(Utils.distance(rd, t) <= rcom){
					rd.vTags.add(t);
				}
			}
//			Utils.debug(rd.rid + "\thas\t" + rd.vTags.size() + "\ttags");
		}
		int max = -1;
		int sum = 0;
		for(Reader rd:coverset){
			sum += rd.vTags.size();
			if(rd.vTags.size()> max ){
				max = rd.vTags.size();
			}
		}
//		avglocalsize = sum/coverset.length;
		Params.avglocalsize = sum/coverset.length;
		return max;
	}
	/**
	 * Return true if t is not found, false otherwise
	 * A tag is not found iff it fails in passing the bloomfilter of all readers in rds
	 * @param t
	 * @param Y
	 * @return
	 */
	public  boolean notfound(Tag t, int[] seeds){ 
		boolean exist = true;
		for(Reader rd:coverset){
			if(rd.contains(t))
				return false;
		}
		return true;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		int nreaders = 80;
		int length = 50, width = 50;
		int rcom = 10, rinfer = 15;
		ReaderGenerator rg = new ReaderGenerator(length,width,rcom,rinfer);
		rg.GenerateRdsRegular();//jiangjin 这两句话自己加上去的
		//rg.GenerateRdsRandom(80);//jiangjin
		Reader[] rds = rg.getReaders();  //返回成员变量Reader[] ra,此时是null
		rg.GetConflict(rds, "allreader.col"); //获得阅读器之间的冲突，把冲突写入到文件中。
		int[] colorsall = Dsatur.coloring("allreader.col");
		System.out.println("all readers need " + Dsatur.colornum + " colors");
		Region[][] regs= rg.GenerateRegion(rds, length, width);
		Vector<Subelement> vSubs = rg.getSubelements(regs);
		System.out.println("There are totally " + vSubs.size() + " subelements");
		rg.associateReader(vSubs);
		Vector<Reader> covering = rg.getMinCoveringReader(rds, vSubs);
		System.out.println("There are " + covering.size() + " readers in the covering subset");
		Reader[] coset = new Reader[covering.size()];
		int i = 0;
		for(Reader rd:covering){
//			System.out.println(rd.toString());
			coset[i] = rd;
			i++;
		}
		rg.logCovering(coset);
		rg.GetConflict(coset, "covering.col");
		int[] colorscovering = Dsatur.coloring("covering.col");
		System.out.println("covering readers need " + Dsatur.colornum + " colors");
	}

}
