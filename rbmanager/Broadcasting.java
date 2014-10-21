package rbmanager;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class Broadcasting extends BroadcastReceiver{
	private static int pState = TelephonyManager.CALL_STATE_IDLE;
	

	@Override
	public void onReceive(final Context context, Intent intent) {
		// TODO Auto-generated method stub
		TelephonyManager mTelephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		mTelephonyManager.listen(new PhoneStateListener(){
			public void onCallStateChanged(int state, String incomingNumber){
				if(state != pState){
					if(state == TelephonyManager.CALL_STATE_IDLE){
						Log.i("state", "IDLE");
						if(pState == TelephonyManager.CALL_STATE_OFFHOOK){
							Intent mIntent = new Intent(context, BackgroundService.class);
							context.stopService(mIntent);
							
							SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);        
                    		String ppState = pref.getString("ppState", "");
                    		if (!ppState.equals("RINGING")){
                    			// 통화 종료
                    			
                    		}
							
                    		SharedPreferences.Editor editor = pref.edit();
                            editor.putString("ppState", "IDLE");
                            editor.commit();
							
						}
					}else if(state == TelephonyManager.CALL_STATE_OFFHOOK){
						Log.i("state", "OFFHOOK");
						if(pState == TelephonyManager.CALL_STATE_IDLE){
							Log.i("state", "out call !!");
							SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);        
							String use = pref.getString("use", "");
							if(use.equals("false")){
								
							}else{
								// This is out call !! 
								Intent mIntent = new Intent(context, BackgroundService.class);
								context.startService(mIntent);
							}
							
						}						
					}else if(state == TelephonyManager.CALL_STATE_RINGING){
                        Log.i("Phone","RINGING");
                        SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);        
                		SharedPreferences.Editor editor = pref.edit();
                        editor.putString("ppState", "RINGING");
                        editor.commit();
                    }
					pState = state;
				}
			}
		}, PhoneStateListener.LISTEN_CALL_STATE);
		
	}

}
