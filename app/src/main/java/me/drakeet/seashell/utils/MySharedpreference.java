package me.drakeet.seashell.utils;


import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import android.content.Context;
import android.content.SharedPreferences;

import me.drakeet.seashell.model.Word;

public class MySharedpreference {

	private Context context;
	private SharedPreferences sharedPreferences;

	public MySharedpreference(Context context) {
		this.context = context;
		sharedPreferences = context.getSharedPreferences(
				"userinfo", Context.MODE_PRIVATE);
	}


	/**
	 * Set the honor, it is the number of notify count.
	 * @param honor count
	 * @return true is successful
	 */
	public boolean saveHonor(int honor) {

		boolean flag = false;
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt("honor", honor);
		flag = editor.commit();
		return flag;
	}
	
	public boolean saveAdStatus(boolean b) {

		boolean flag = false;
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean("ad_status", b);
		flag = editor.commit();
		return flag;
	}
	
	public boolean saveYesterdayJson(String string) {

		boolean flag = false;
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("yesterday_json", string);
		flag = editor.commit();
		return flag;
	}
	
	public boolean saveTodayJson(String string) {

		boolean flag = false;
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("today_json", string);
		flag = editor.commit();
		return flag;
	}
	
	public Map<String, Object> getInfo() {

		Map<String, Object> map = new HashMap<String, Object>();
		int honor = sharedPreferences.getInt("honor", 0);
		boolean adStatus = sharedPreferences.getBoolean("ad_status", true);
		map.put("honor", honor);
		map.put("ad_status", adStatus);
		return map;
	}
	
	public Map<String, String> getWordJson() {

		Map<String, String> map = new HashMap<String, String>();
		//init
		Word word  = new Word();
		word.setWord("seashell");
		word.setPhonetic("[ˈsi:ʃel]");
		word.setSpeech("n.");
		word.setExplanation("海中软体动物的壳，贝壳。");
		word.setExample("eg. With your ear to a seashell.");
		
		System.out.println(new Gson().toJson(word));
		String yesterdayJson = sharedPreferences.getString("yesterday_json", new Gson().toJson(word));
		String todayJson = sharedPreferences.getString("today_json", new Gson().toJson(word));
		map.put("yesterday_json", yesterdayJson);
		map.put("today_json", todayJson);
		return map;
	}
}
