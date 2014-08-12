/**
 *  Dialer for testing VoLTE network side KPIs.
 *  
 *   Copyright (C) 2014  Spinlogic
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as 
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package at.a1.volte_dialer.callmonitor;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;


/**
 * This service is used to monitor outgoing calls.
 * The client may include the following extras:
 * 
 * EXTRA_CLIENTMSGR			a binder to send messages back to the client 
 * 
 * @author Juan Noguera
 *
 */
public class CallMonitorService extends Service {
	private static final String TAG = "CallMonitorService";
	
	final static public String EXTRA_CLIENTMSGR 	= "client";			// binder to the client

	
	// Messages that this service can receive from clients
	static final int MSG_CLIENT_DISCONNECT = 1;	// The client has disconnected the call
	
	// Messages that this service passes to the clients
	static final int MSG_STATE_INSERVICE	= 0;	// The service state is STATE_IN_SERVICE
	static final int MSG_STATE_OUTSERVICE	= 1;	// The service state is STATE_EMERGENCY_ONLY, STATE_OUT_OF_SERVICE or STATE_POWER_OFF
	
	private boolean 				is_system;
	private boolean					is_client_diconnect;
	private CallMonitorReceiver 	mCallMonitorReceiver;
	private OutgoingCallReceiver 	mOutgoingCallReceiver;
	private CallDescription			mCallDescription;
	
	private Messenger mClient;		// provided by client to service
	final Messenger mService = new Messenger(new IncomingHandler());	// provided by service to client
	
	/**
     * Handler of incoming messages from clients.
     */
    @SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        	final String METHOD = "::IncomingHandler::handleMessage()  ";
            switch (msg.what) {
                case MSG_CLIENT_DISCONNECT:
                	is_client_diconnect = true;
                	Log.i(TAG + METHOD, "MSG_CLIENT_DISCONNECT received from client.");
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

	public CallMonitorService() {
		is_system				= false;
		is_client_diconnect		= false;
		mCallMonitorReceiver	= null;
		mOutgoingCallReceiver	= null;
		mCallDescription		= null;
		mClient					= null;
		CallLogger.initializeValues();		// init logging
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		final String METHOD = "::onStartCommand()  ";
		
		Log.i(TAG + METHOD, "Starting service.");
		Bundle extras = intent.getExtras();
		if(extras != null) {
			if(extras.containsKey(EXTRA_CLIENTMSGR)) {
				mClient = new Messenger(extras.getBinder(EXTRA_CLIENTMSGR));
			}
		}
		is_system	= isRunningAsSystem();
		mCallMonitorReceiver = new CallMonitorReceiver(this);
		TelephonyManager telMng = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		int mflags = PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | 
					 PhoneStateListener.LISTEN_SERVICE_STATE;
		if(!is_system) {
			mflags |= PhoneStateListener.LISTEN_CALL_STATE;
		}
		else {
			// TODO: start precise call monitor received
		}
		mOutgoingCallReceiver = new OutgoingCallReceiver(this);
		telMng.listen(mCallMonitorReceiver, mflags);
		
		// To get number for outgoing calls
		IntentFilter mocFilter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
		registerReceiver(mOutgoingCallReceiver, mocFilter);
	    
		Log.i(TAG + METHOD, "Service started.");
		int res = super.onStartCommand(intent, flags, startId);
		return res;
	}
		
	@Override
	public void onDestroy() {
		final String METHOD = "::onDestroy()  ";
		TelephonyManager telMng = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		telMng.listen(mCallMonitorReceiver, PhoneStateListener.LISTEN_NONE);
		unregisterReceiver(mOutgoingCallReceiver);
		Log.i(TAG + METHOD, " Call info receivers stopped.");
		super.onDestroy();
	}

	@Override
    public IBinder onBind(Intent intent) {
		return mService.getBinder();
	}
	
	// Methods for CallMonitorReceiver and OutgoingCallReceiver to report events
	// This methods 
	
	/**
	 * Used by CallMonitorReceiver to report a change in call state.
	 * The new call state is communicated to the binded apps.
	 * 
	 * @param direction		direction of the call
	 * @param mt_msisdn		incoming number (for MT calls)
	 */
	public void startCall(String direction, String mt_msisdn) {
		final String METHOD = "::startCall()  ";
		Log.i(TAG + METHOD, " Starting call. Direction = " + direction);
		if(mCallDescription == null) {
			mCallDescription = new CallDescription(this, direction, CallMonitorReceiver.signalstrength);
			if(mt_msisdn != null && !mt_msisdn.isEmpty()) {
				int plength = mt_msisdn.length();
				plength = (plength > 6) ? 6 : plength;	// prefix is, at most, 6 char long	
				mCallDescription.setPrefix(mt_msisdn.substring(0, plength));
			}
		}
	}
	
	/**
	 * Ends a call
	 * The call info is written to the call log.
	 * If the disconnection is triggered by the client hanging up, then the 
	 * client must communicate this to this service before actually hanging up. 
	 */
	public void endCall(int strength) {
		final String METHOD = "::endCall()  ";
		Log.i(TAG + METHOD, " Ending call.");
		String side = CallDescription.CALL_DISCONNECTED_BY_NW;
		if(is_client_diconnect) {
			side = CallDescription.CALL_DISCONNECTED_BY_UE;
		}
		mCallDescription.endCall(side, strength);
		mCallDescription.writeCallInfoToLog();
		mCallDescription = null;	// no needed any more
		Log.i(TAG + METHOD, " Call terminated.");
	}
	
	/**
	 * Notifies the number for an outgoing call.
	 * 
	 * @param msisdn
	 */
	public void moCallNotif(String bpty) {
		final String METHOD = "::moCallNotif()  ";
		Log.i(TAG + METHOD, " MO calls to " + bpty);
		if(mCallDescription == null) {
			mCallDescription = new CallDescription(this, CallDescription.MO_CALL, CallMonitorReceiver.signalstrength);
		}
		if(bpty != null && !bpty.isEmpty()) {
			int plength = bpty.length();
			plength = (plength > 6) ? 6 : plength;	// prefix is, at most, 6 char long	
			mCallDescription.setPrefix(bpty.substring(0, plength));
		}
		mCallDescription.setDirection(CallDescription.MO_CALL);
	}
	
	/**
	 * Sends a message to the client via the Messenger object provided 
	 * by the client, if any.
	 * @param what
	 */
	public void sendMsg(int what) {
		final String METHOD = "::sendMsg()  ";
		
		if(mClient != null) {
			Log.i(TAG + METHOD, "Sending message to client. What = " + Integer.toString(what));
			Message msg = Message.obtain(null, what, 0, 0);
			try {
				mClient.send(msg);
				Log.i(TAG + METHOD, "Message sent to client.");
			} catch (RemoteException e) {
				Log.d(TAG + METHOD, e.getClass().getName() + e.toString());
			}
		}
	}
	
	
	   /**
     * Determines whether the app is running in system or user space
     * 
     * @return	true	App is running in system space
     * 			false	App is running in user space
     */
    private boolean isRunningAsSystem() {
    	int uid_radio = android.os.Process.getUidForName("radio");
    	int uid_system = android.os.Process.getUidForName("system");
    	int uid_root = android.os.Process.getUidForName("root");
    	int myuid = android.os.Process.myUid();
    	return (myuid == uid_radio || myuid == uid_system || myuid == uid_root) ? true : false;
    }
}