/**
 *  Part of the dialer for testing VoLTE network side KPIs.
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OutgoingCallReceiver extends BroadcastReceiver {
	
	private CallMonitorInterface mCmIf;
	
	public OutgoingCallReceiver(CallMonitorService cmif) {
		mCmIf = cmif;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		mCmIf.csmif_CallState(CallMonitorService.CALLSTATE_DIALING, 
							  intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER));
	}

}
