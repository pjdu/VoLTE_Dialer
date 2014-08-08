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

package at.a1.volte_dialer.phonestate;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import at.a1.volte_dialer.Globals;

/**
 * This class implements a handler for the PhoneStateService
 * Two receiver classes are used. One [PhoneStateReceiver] to listen 
 * for standard call, signal and service states, and the other
 * [PreciseCallStateReceiver] to listen for precise states. This second
 * one works only on rooted devices.
 * 
 * @author Juan Noguera
 *
 */
public class PhoneStateHandler {
	private static final String TAG = "PhoneStateHandler";
	
	private PhoneStateReceiver mPhoneStateReceiver;
	private PreciseCallStateReceiver mPreciseCallStateReceiver;
	
	public PhoneStateHandler(Context context) {
		mPhoneStateReceiver 		= new PhoneStateReceiver(context);
		mPreciseCallStateReceiver 	= new PreciseCallStateReceiver(context);
	}
	
	public void start(Context context) {
		final String METHOD = "::start()  ";
		Log.d(TAG + METHOD, " Starting Phone state receivers.");
	    // Start listening for changes in service and call states
		TelephonyManager telMng = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		int flags = PhoneStateListener.LISTEN_CALL_STATE;
		if(!Globals.is_receiver) {
			flags = flags | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_SERVICE_STATE;
		}
	    telMng.listen(mPhoneStateReceiver, flags);
	    mPreciseCallStateReceiver.listen();
	    Log.d(TAG + METHOD, " Phone state receivers started.");
	}
	
	public void stop(Context context) {
		final String METHOD = "::stop()  ";
		mPreciseCallStateReceiver.stop();
		TelephonyManager telMng = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		telMng.listen(mPhoneStateReceiver, PhoneStateListener.LISTEN_NONE);
		Log.d(TAG + METHOD, " Phone state receivers stopped.");
	}

	
	/*
	private void registerForDetailedCallEvents() {
		final String METHOD = "registerForDetailedCallEvents";
		try {
			final Class<?> classCallManager = Class.forName("com.android.internal.telephony.CallManager");
			Method methodGetInstance = classCallManager.getDeclaredMethod("getInstance");
			Method methodRegisterForPreciseCallStateChanged = classCallManager.getDeclaredMethod("registerForPreciseCallStateChanged",
																new Class[]{Handler.class, Integer.TYPE, Object.class});
//			methodGetInstance.setAccessible(true);
			Object mCallManager = methodGetInstance.invoke(null);
			
//			mHandler = new PreciseCallEventsHandler();
//			Field pcsc = classCallManager.getDeclaredField("EVENT_PRECISE_CALL_STATE_CHANGED");
//			pcsc.setAccessible(true);
//			methodRegisterForPreciseCallStateChanged.invoke(mCallManager, mHandler, EVENT_PRECISE_CALL_STATE_CHANGED, null);
		} 
		catch (ClassNotFoundException e) {
	        Log.d(TAG + METHOD, e.getClass().getName() + e.toString());
	    }
	    catch (NoSuchMethodException e) {
	        Log.d(TAG + METHOD, e.getClass().getName() + e.toString());
	    }
	    catch (InvocationTargetException e) {
	        Log.d(TAG + METHOD, e.getClass().getName() + e.toString());
	    }
	    catch (IllegalAccessException e) {
	        Log.d(TAG + METHOD, e.getClass().getName() + e.toString());
	    } 
		catch (SecurityException e) {
	        Log.d(TAG + METHOD, e.getClass().getName() + e.toString());
	    } 
		catch (IllegalArgumentException e) {
	        Log.d(TAG + METHOD, e.getClass().getName() + e.toString());
		}
		catch (Exception e) {
	        Log.d(TAG + METHOD, e.getClass().getName() + e.toString());
		}
	}
	
	private void unregisterForDetailedCallEvents() {
		final String METHOD = "unregisterForDetailedCallEvents";
		try {
			final Class<?> classCallManager = Class.forName("com.android.internal.telephony.CallManager");
			Method methodGetInstance = classCallManager.getDeclaredMethod("getInstance");
			Method methodUnregisterForPreciseCallStateChanged = classCallManager.getDeclaredMethod("unregisterForPreciseCallStateChanged",
																new Class[]{Handler.class});
			Object mCallManager = methodGetInstance.invoke(null);
//			methodUnregisterForPreciseCallStateChanged.invoke(mCallManager, mHandler);
		} 
		catch (ClassNotFoundException e) {
	        Log.d(TAG + METHOD, e.getClass().getName() + e.toString());
	    }
	    catch (NoSuchMethodException e) {
	        Log.d(TAG + METHOD, e.getClass().getName() + e.toString());
	    }
	    catch (InvocationTargetException e) {
	        Log.d(TAG + METHOD, e.getClass().getName() + e.toString());
	    }
	    catch (IllegalAccessException e) {
	        Log.d(TAG + METHOD, e.getClass().getName() + e.toString());
	    } 
		catch (SecurityException e) {
	        Log.d(TAG + METHOD, e.getClass().getName() + e.toString());
	    } 
		catch (IllegalArgumentException e) {
	        Log.d(TAG + METHOD, e.getClass().getName() + e.toString());
		}
		catch (Exception e) {
	        Log.d(TAG + METHOD, e.getClass().getName() + e.toString());
		}
	
	}
*/
	
}
