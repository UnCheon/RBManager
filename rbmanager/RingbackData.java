package rbmanager;

public class RingbackData {
	boolean sktTone(int freq){
		if(freq > 38 && freq < 45)
			return true;
		else
			return false;
	}
		
	boolean basicTone(int freq){
		if((max_x > 8 && max_x < 19) || (max_x > 25 && max_x < 49) || (max_x > 54 && max_x < 58) || (max_x > 68 && max_x < 77) || (max_x > 82 && max_x < 85))
			return true;
		else
			return false;
	}
}