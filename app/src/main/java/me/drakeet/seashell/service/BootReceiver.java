package me.drakeet.seashell.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import me.drakeet.seashell.service.NotificatService;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {		// boot
			Intent intent2 = new Intent(context, NotificatService.class);
//			intent2.setAction("android.intent.action.MAIN");
//			intent2.addCategory("android.intent.category.LAUNCHER");
			intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startService(intent2);
		}
	}
}