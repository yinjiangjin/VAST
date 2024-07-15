package RFID;
/**
 * 存放每个点()被多少个阅读器覆盖。sig 
 * @author Jiangjin
 *
 */
public class Region {
	String sig; //如果没有被阅读器覆盖为'x'
	public Region(){
		sig = null;
	}
	public void setSig(String psig){
		sig = psig;
	}
	public boolean isInSameSubelement(Region r1){
		if(sig.equals(r1.sig))
			return true;
		else
			return false;
	}
}
