
package RFID;
import java.util.*;


public class BasicTIC extends ICNPBasic{
	//the following values are for EPC Global C1G2 tags
	String fname = "HTIC";  //�����õ��ļ�
	String collectname = "HTIC-data-1";
	int eliT = 0,eliNT=0,eliL =0; //���ֿ��������ı�ǩ
	//boolean firstcolflag = true; //��ǵ�һ����ǩ�ռ���ʱ�䡣
	HashSet<Tag> zz = null;  //��ű���Ŀ���ǩ����
	int round;  //��Ա�����Զ���ʼ��Ϊ0
	int roundReader;
	public BasicTIC(){
		ptname = new String("HTIC");
	}
	
	double execute(){
		roundReader++;
		TimeFirstPhase = 0; //��ʼ��
		TimeSecondPhase = 0;
		
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
		//System.out.println("TIC "+"targetset.size():"+targetset.size()+" "+"localtagsset.size():"+localtagsset.size());
		//zz�Ķ������ж���Ŀ���ǩ
		zz =new HashSet<Tag>();
		zz.addAll(localtagsset);
		zz.retainAll(targetset);
		System.out.println("�Ķ������ж���Ŀ���ǩ-zz.size():"+zz.size());
				
		boolean flagcoll = false;
		while(targetset.size()>0){
			round++;
			int[][]tworet =  eliminatePhase(targetset.size(),nontargetset.size(),localtagsset.size());
			if(isCollect(tworet)){
				singleSeedCollection(targetset,localtagsset,tworet);
			}
		}
		
		//Utils.logAppend("collecttotalnumber-HTIC.txt", "HTIC "+collecttotalnumber+"\n");
		return tcollection;
	}
	
	/**
	 * �ų��׶Σ���ȡ1bit�ظ����ų���ǩ
	 * @return
	 */
	int[][] eliminatePhase(int W, int N, int L){
		
		ttime_filtering +=TimingScheme.t_lambda; //ͳ�ƹ��˽׶ε�ʱ��
		
		tcollection+=TimingScheme.t_lambda; //����query��ʱ��
		
		//Utils.logAppend(collectname,  targetset.size()+"\t "+ nontargetset.size()+"\t ");
		double timeFirstPhase = 0;
		
		int [][] tworet =new int[3][];
		
		double[] ret = new double[]{0,0,0,0};

		//int f = localtagsset.size();
		int f = (int)getOptimalFrame_eta(W, N ,L)[0];   //֡��
		//System.out.println("����֡��Ϊ��"+f);
		
		if(f<10){
			f=10;
		}
		
		//String data1 = targetset.size()+"\t "+localtagsset.size()+"\t "+nontargetset.size()+"\t "+f+"\r";
		//Utils.logAppend(fname, data1);
			
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
		//���ظ�����������Ŀ
		ret[1] = filter(targetset, replyL); //�ų������Ķ����µ�target���յ����ػظ�Ϊ0.
		ret[2] = filternontarget(nontargetset, replyW, replyL);
		ret[3] = filter(localtagsset, replyW);   //replyWΪ0��ʾ���ų�ʱ϶��������ر�ǩӳ���ų�ʱ϶�������ų�
		
		eliT = (int) ret[1];
		eliNT = (int) ret[2];
		eliL = (int) ret[3];
		
		
		timeFirstPhase = ret[0];
		tcollection += timeFirstPhase;
		
		TimeFirstPhase += timeFirstPhase;//ֱ�Ӹ�ֵ
		
		ttime_filtering += timeFirstPhase; //ͳ�ƹ��˽׶ε�ʱ��
		
		String data = " Firstphase" + " \t " + round + " \t " + timeFirstPhase+" \t "+eliT+" \t "+eliNT+" \t "+eliL+" \r\n ";
		if(roundReader==1){
			Utils.logAppend(collectname, data);
		}
		
		
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

	boolean canReconciledsingSeed(HashSet<Tag> hset, int seed){
		
		int num = hset.size(); 
		int[] reply = getReply(num,seed,hset); //��ʱ���ûظ��ġ�
		if((reply[0]==1)&&(reply[1]==1)){  //�����Ƕ���ͻ��������ͻ���������ø�����
			return true;
		}
		return false;
	}


	
void singleSeedCollection(HashSet<Tag> tset,HashSet<Tag> lset,int[][] tworet ){
		
		double time2thPhase = tcollection; //��ʱ���������ý׶ε�ʱ��
	
		HashSet<Tag> qualifiedtarget = new HashSet<Tag>();  //ͬ��target��ǩ
		qualifiedtarget.clear();
		
		int[] replypW = tworet[0];
		int[] replypN = tworet[1];
		int[] replypL = tworet[2];
		
		//����˵��Ҫд��ICNPBasic�С�
		for(Tag t:tset){
			int j = t.slotidx;
			if((replypL[j] >0)&&(replypN[j] == 0)&&(replypW[j] <= 3)){
				qualifiedtarget.add(t);
			}
		}
		String sendVector = ""; //���͵�2bitvector
		int countslot = 0; //��¼����Ҫ��ʱ϶��Ŀ
		int ReconciledNum = 0;
		
		HashSet<Tag> temp = new HashSet<Tag>();
		HashSet<Tag> tempT = new HashSet<Tag>();


		int seed2 = rand.nextInt();  //���������ݽ���Ӧ�úõ�
		//�����ж�̫���ӣ�����ʱ�临�Ӷȸߡ����Ǻ�����Ҫ����ʱ϶��. 
		for(int i=0;i<replypL.length;i++){
			if((replypL[i] >0)&&(replypN[i] == 0)&&(replypW[i] <= 3)){//Ԥ��С��3��ͬ��ʱ϶�����յ��ظ�
				if(replypW[i]==1){ //�����ظ�Ϊ1
					for(Tag t1:qualifiedtarget){
						if(t1.slotidx==i){
							t1.slotidx = countslot; //���ûظ���ʱ϶
							temp.add(t1);
							countslot +=1;
						}
					}
					sendVector += "01";

					
				}else if(replypW[i]==2){ //Ԥ��Ϊ2��ͬ��ʱ϶���յ��ظ�
					for(Tag t2:qualifiedtarget){
						if(t2.slotidx==i){
							tempT.add(t2);
						}
					}
					if(!canReconciledsingSeed(tempT,seed2)){//�������Э��
						sendVector += "00";
					}else{
						for(Tag actualslot: tempT){
							actualslot.slotidx = actualslot.slotidx + countslot;
						}
						countslot += 2;
						ReconciledNum += 1; 
						sendVector += "10";
						temp.addAll(tempT);
						
					}
					
					tempT.clear();

				}else{
					for(Tag t3:qualifiedtarget){
						if(t3.slotidx==i){
							tempT.add(t3);
						}
					}	
					if(!canReconciledsingSeed(tempT,seed2)){
						sendVector += "00";
					}else{
						for(Tag actualslot: tempT){
							actualslot.slotidx = actualslot.slotidx + countslot;
						}
						countslot += 3;
						ReconciledNum += 1;
						sendVector += "11";
						temp.addAll(tempT);

					}
					tempT.clear();

				}
			}else{
				sendVector += "00"; //��������������00
			}
		}

		int f = replypL.length;
		tcollection += Math.ceil(2*f/96.0)*TimingScheme.t_id; //����allocated vector��ʱ��
		
		for(int i=0;i<temp.size();i++){  //��Ҫ��ô��slots
			for(Tag t: temp){
				if(t.slotidx==i){
					
					if(lset.contains(t)){
						
						targetcollected.add(t); //���뵽�Ѿ��ռ��ı�ǩ������
						tcollection += (TimingScheme.interval+TimingScheme.onebit*infLength);
						collecttotalnumber +=1;
						sumtcollection += tcollection + eachcolortime; //�ռ���ʱ��
						
					}else{
						tcollection += TimingScheme.t_e;
					}
					
					break; //�ҵ��󼴿��˳������ñ�������
				}
				
			}
		}
		
		time2thPhase = tcollection - time2thPhase ;
		String data = " Secondphase" + " \t " + round + " \t " + time2thPhase+" \t "+temp.size()+" \r\n ";
		if(roundReader==1){
			Utils.logAppend(collectname, data);
		}
		
		TimeSecondPhase += time2thPhase; //ֱ�Ӹ�ֵ
		
		tset.removeAll(temp); //target set��ɾ���Ѿ��ռ��ı�ǩ
		lset.removeAll(temp);
		
	}

	
boolean isCollect(int[][] tworet){ 
	

	double flag = 100;
	int[] replyW = tworet[0];
	int[] replyN = tworet[1];
	int[] replyL = tworet[2];
	int Lastf = replyW.length;
	int ss1=0,ss2=0,ss3=0;
	for (int i = 0; i < replyL.length; i++) {
		if((replyL[i] >0)&&(replyW[i]==1)&&(replyN[i]==0)){
			ss1 +=1;
		}
		if((replyL[i] >0)&&(replyW[i]==2)&&(replyN[i]==0)){
			ss2 +=1;
		}
		if((replyL[i] >0)&&(replyW[i]==3)&&(replyN[i]==0)){
			ss3 +=1;
		}
	}
	
	int framesize = replyW.length;
	
	
	//1. ��������Ϣ�ɼ�
	double Y_ic = ss1 + ss2*2/2 + 3*ss3*(6.0/27);
	
	double T_ic = 2*framesize*TimingScheme.t_id/96;
	
	double eta_ic = Y_ic/T_ic;
	
	//2. �����ɼ���Ϣ��ֱ�ӽ�����һ�׶�
	
	//�����Ѿ�ִ����һ�ֵ��ų��׶Σ�ʣ�µ� W_i_1, N_i_1, �� L_i_1 ���� ֱ�ӻ�ã�����Ҫ������һ��ȥ�����ˡ�
	int W_i_1 = targetset.size();
	int N_i_1 =  nontargetset.size();
	int L_i_1 = localtagsset.size(); //�������ܹ�׼ȷ���ơ�
	
	double eta_i_1 =  getOptimalFrame_eta(W_i_1, N_i_1, L_i_1)[1];
	

	if(eta_ic < eta_i_1){ 
		return false; //���ռ���Ϣ
	}else{
		return true;
	}
	
}

}
