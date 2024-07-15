
package RFID;
import java.util.*;


public class ETIC4_8 extends ICNPBasic{
	//the following values are for EPC Global C1G2 tags
	String fname = "ICNPATIC";  //�����õ��ļ�
	String collectname = "ICNPATIC-data";
	//boolean firstcolflag = true; //��ǵ�һ����ǩ�ռ���ʱ�䡣
	int[] seeds = new int[8];
	int Lr = 4;
	public ETIC4_8(){
		ptname = new String("ICNPATIC4_8");
	}
	

	double execute(){
		
		tcollection = 0;
		//System.out.println("start TIC");
		//�˴���������
		HashSet<Tag> local = new HashSet<Tag>();
		local.addAll(targetset);
		local.addAll(nontargetset);
		localtagsset.retainAll(local); //����һ����ǩ���Ա�����Ķ������ǣ���Щ��ǩ�����Ѿ��ռ���Ϣ�ˡ����������Ĳ���
		if(localtagsset.size()==0){
			return 0;
		}
	
		//boolean flagcoll = false;
		while(targetset.size()>0){
			int[][]tworet =  eliminatePhase(); 
			MultiSeedCollection(tworet);

		}
		//Utils.logAppend("collecttotalnumber-ICNPATIC.txt", "ICNPATIC "+collecttotalnumber+"\n");
		return tcollection;
	}
	
	/**
	 * �ų��׶Σ���ȡ1bit�ظ����ų���ǩ
	 * @return
	 */
	int[][] eliminatePhase(){
		
		ttime_filtering +=TimingScheme.t_lambda; //ͳ�ƹ��˽׶ε�ʱ��
		
		tcollection+=TimingScheme.t_lambda; //����query��ʱ��
		
		
		int [][] tworet =new int[3][];
		double[] ret = new double[]{0,0,0,0};
		//int f = localtagsset.size();
		int f = (int) getOptimalFrame_eta(targetset.size(),nontargetset.size(),localtagsset.size())[0];   //֡����Ҫ��matlab�����ԡ�
		if(f<20){
			f=20;
		}
		int seed = rand.nextInt();
		
		
		//�ظ�����
		int[] replyW = getReply(f,seed,targetset);//expected reply vector when W is mapped
		int[] replyN = getReply(f,seed,nontargetset);//expected reply vector when N is mapped
		int[] replyL = getReply(f,seed,localtagsset);//expected reply vector when L is mapped;local tags
		
		//time in collecting replies from local tags
		for(int i = 0; i < replyL.length; i++){
			if(replyL[i] == 0){
				ret[0] += TimingScheme.t_e;
			}else{
				ret[0] += TimingScheme.t_s;
			}
		}
		
		ret[1] = filter(targetset, replyL); //�ų������Ķ����µ�target���յ����ػظ�Ϊ0.
		ret[2] = filternontarget(nontargetset, replyW, replyL);
		ret[3] = filter(localtagsset, replyW);   //replyWΪ0��ʾ���ų�ʱ϶��������ر�ǩӳ���ų�ʱ϶�������ų�

		System.out.println("targetset:"+targetset.size()+" nontargetset: "+nontargetset.size()+"  localtagsset:"+localtagsset.size());
		
		tcollection += ret[0]; //�ռ�ʱ����д������
		
		ttime_filtering += ret[0]; //ͳ�ƹ��˽׶ε�ʱ��
		
		tworet[0] = replyW;
		tworet[1] = replyN;
		tworet[2] = replyL;

		return tworet;
	}
	
	
	
	public double[] getOptimalFrame_eta(int W, int N, int L) {
		// TODO Auto-generated method stub
		double e = Math.E;
	
		double[] eta_arr = new double[N+W];
		for (int f = 0; f < eta_arr.length; f++) {
			
			//����
			double Xi = N*Math.pow(e, -1.0*W/f) +  N*Math.pow(e, -1.0*L/f)*(1-Math.pow(e, -1.0*W/f));
			double Yi =  W*Math.pow(e, -1.0*L/f);
			double fenzi_Neli = Xi+ Yi;
			
			//��ĸ
			double P0 = Math.pow(e, -1.0*L/f);
			double fenmu_Ti = f*(P0*TimingScheme.t_e + (1-P0)*TimingScheme.t_s);
			
			eta_arr[f] = fenzi_Neli/fenmu_Ti; //��������е�etaֵ��
			
		}
		double max_eta = 0;
		int opt_f = 0;
		for (int i = 2; i < eta_arr.length; i++) {
			if(eta_arr[i]>max_eta){
				max_eta = eta_arr[i];
				opt_f =i;
			}
		}
		
		double []optf_maxeta = new double[2];
		optf_maxeta[0] = opt_f;
		optf_maxeta[1] = max_eta;
		return optf_maxeta;
	}
	
	
	/**
	 * ��������
	 */
	HashSet<Tag> canReconciledMultiSeed(HashSet<Tag> thset,HashSet<Tag> nhset, int[] seeds, int Lr){
		
		HashSet<Tag> tp = new HashSet<Tag>();
		tp.clear();
		
		HashSet<Tag> MaxT = new HashSet<Tag>();
		MaxT.clear();
		
		HashSet<Tag> tset = new HashSet<Tag>(); //Ϊ�˲��޸�ԭ����ʱ϶. �ռ�����ȫҲ����������ط������⡣
		HashSet<Tag> nset = new HashSet<Tag>();
		tset.addAll(thset);
		nset.addAll(nhset);
		
		int num = Lr;  //�ܹ���Lr��ʱ϶
		for (int i = 0; i < seeds.length; i++) {
			
			int[] reply1 = getReply(num,seeds[i],tset); //target��ǩʱ϶
			int[] reply2 = getReply(num,seeds[i],nset); //target��ǩʱ϶
			for(Tag t: tset){
				if(reply2[t.slotidx]==0&&reply1[t.slotidx]==1){
					tp.add(t);
				}
			}
			if(tp.size()>MaxT.size()){
				MaxT.clear();
				MaxT.addAll(tp);
			}
			tp.clear();
		}
		return MaxT;

	}
		

	HashSet<Tag> canReconciledMultiSeed(HashSet<Tag> hset, int[] seeds, int Lr){
		
		HashSet<Tag> tp = new HashSet<Tag>();
		tp.clear();
		
		HashSet<Tag> MaxT = new HashSet<Tag>();
		MaxT.clear();
		int maxN = 0;
		
		int num = Lr; 
		HashSet<Tag> tset = new HashSet<Tag>();
		tset.addAll(hset);
		
		
		for (int i = 0; i < seeds.length; i++) {
			
			int[] reply = getReply(num,seeds[i],tset); //��ʱ���ûظ��ġ�
			int flag =0;
			for(Tag t:tset){
				if(reply[t.slotidx]==1){
					flag +=1;
					tp.add(t);
				}
			}
			if(flag>maxN){
				MaxT.clear();
				maxN = flag;
				MaxT.addAll(tp);
			}
			tp.clear();
		
		}
		return MaxT;
	}
	
void MultiSeedCollection(int[][] tworet ){
		
	 for(int i = 0; i < seeds.length; i++){
			seeds[i] = rand.nextInt();
		}
	 
		int[] replyW = tworet[0];
		int[] replyN = tworet[1];
		int[] replyL = tworet[2];
		int countslot = 0; //��¼����ʱ϶��Ŀ
		int ReconciledNum = 0;  //��¼����seed����Ŀ��

		HashSet<Tag> temp = new HashSet<Tag>();
		temp.clear();
		HashSet<Tag> tempT = new HashSet<Tag>();
		HashSet<Tag> tempNT = new HashSet<Tag>();
		
		HashSet<Tag> tempTC = new HashSet<Tag>();
		
		for (int i = 0; i < replyL.length; i++) {
			if((replyL[i] >0)&&(replyW[i] >0)){//Ԥ��С��3��ͬ��ʱ϶�����յ��ظ�
				 if( replyN[i]!=0 ){
					tempT = findSlotTagSet(targetset,i);
					tempNT = findSlotTagSet(nontargetset,i);
					tempTC.addAll( canReconciledMultiSeed(tempT,tempNT,seeds,Lr) );
					
					if(!tempTC.isEmpty()){
				
						ReconciledNum += 1;
						temp.addAll(tempTC);

					}
					
					tempT.clear();
					tempNT.clear();
					tempTC.clear();
					
				}else if(replyN[i]==0){
					tempT = findSlotTagSet(targetset,i);
					
					tempTC.addAll( canReconciledMultiSeed(tempT,seeds,Lr) );
					if(!tempTC.isEmpty()){
	
						ReconciledNum +=1;

						temp.addAll(tempTC);
					
					}

					tempT.clear();
					tempTC.clear();
				}
			}
			
		}
		
		int Lastf = replyW.length;
		int colNum = temp.size();  //�����ռ���target��ǩ����Ŀ
		int Vc = (int) (ReconciledNum*(Math.log(seeds.length)/Math.log(2)));//seed ��������bit��Ŀ
		
		
		
		int framesize = replyW.length;
		
		double eta_c = 1.0*colNum/(Lr*framesize/96.0)*TimingScheme.t_id + (Vc/96.0)*TimingScheme.t_id;
		
		//�����Ѿ�ִ����һ�ֵ��ų��׶Σ�ʣ�µ� W_i_1, N_i_1, �� L_i_1 ���� ֱ�ӻ�ã�����Ҫ������һ��ȥ�����ˡ�
		int W_i_1 = targetset.size();
		int N_i_1 =  nontargetset.size();
		int L_i_1 = localtagsset.size(); //�������ܹ�׼ȷ���ơ�
		double eta_i_1 =  getOptimalFrame_eta(W_i_1, N_i_1, L_i_1)[1];
		
		
		/*String data1 = targetset.size()+"\t "+nontargetset.size()+"\t "+localtagsset.size()+"\t ";
		data1 += framesize+"\t "+colNum+"\t "+Vc +"\t "+flag+"\r";
		 Utils.logAppend(collectname, data1);*/
	
		 
		 if(colNum<1){
				return;
			}
		 
		if( eta_c >  eta_i_1 || nontargetset.size()==0){
		
			int f = replyL.length;
			tcollection += (Lr*f/96.0)*TimingScheme.t_id; //����allocated vector��ʱ��
			tcollection += (Vc/96.0)*TimingScheme.t_id;
						
			//�ռ���ǩʱ��  temp, temp2
			for(Tag t: temp){
				if(localtagsset.contains(t)){
					targetcollected.add(t);  //���뵽�ռ���Ϣ�ļ�����
					tcollection += (TimingScheme.interval+TimingScheme.onebit*infLength);
					collecttotalnumber +=1;
					sumtcollection += tcollection + eachcolortime; //�ռ���ʱ��
				}else{
					tcollection += TimingScheme.t_e;
				}
			}
			
			//System.out.println("temp:"+temp.size());
			localtagsset.removeAll(temp); 
			targetset.removeAll(temp);
			
		//	System.out.println("targetset:"+targetset.size()+" nontargetset: "+nontargetset.size()+"  localtagsset:"+localtagsset.size());
			
		}
		temp.clear();
		
	}
}
