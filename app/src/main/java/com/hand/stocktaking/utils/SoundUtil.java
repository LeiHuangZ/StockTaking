package com.hand.stocktaking.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.widget.Toast;

import com.hand.stocktaking.R;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SoundUtil {

	
	public static SoundPool sp ;
	public static Map<Integer, Integer> suondMap;
	public static Context context;
	
	//
	public static void initSoundPool(Context context){
		SoundUtil.context = context;
		sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
		suondMap = new HashMap<Integer, Integer>();
		suondMap.put(1, sp.load(context, R.raw.msg, 1));
		suondMap.put(2, sp.load(context, R.raw.warning, 1));
	}
	
	//
	public static  void play(int sound, int number){
		AudioManager am = (AudioManager)SoundUtil.context.getSystemService(Context.AUDIO_SERVICE);
	   //
	    float audioMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	        
	        //
	        float audioCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
	        float volumnRatio = audioCurrentVolume/audioMaxVolume;
	        sp.play(
	        		suondMap.get(sound), //
	        		audioCurrentVolume, //
	        		audioCurrentVolume, //
	                1, //
	                number, //
	                1);//
	    }
	public static void pasue() {
		sp.stop(suondMap.get(1));
	}

	public static Toast mToast;
	public static void showToast(Context context, String content){
		if (mToast == null){
			mToast = Toast.makeText(context, content, Toast.LENGTH_SHORT);
		}else {
			mToast.setText(content);
		}
		mToast.show();
	}

	/*
    * 定义文件保存的方法，写入到文件中，所以是输出流
    * */
	public static void save(String name, String content) throws Exception {
		//Context.MODE_PRIVATE权限，只有自身程序才能访问，而且写入的内容会覆盖文本内原有内容
		FileOutputStream output = context.openFileOutput(name, Context.MODE_PRIVATE);
		output.write(content.getBytes());  //将String字符串以字节流的形式写入到输出流中
		output.close();         //关闭输出流
	}


	/*
    * 定义文件读取的方法
    * */
	public static String read(String filename) throws IOException {
		//打开文件输入流
		FileInputStream input = context.openFileInput(filename);
		//定义1M的缓冲区
		byte[] temp = new byte[1024];
		//定义字符串变量
		StringBuilder sb = new StringBuilder("");
		int len = 0;
		//读取文件内容，当文件内容长度大于0时，
		while ((len = input.read(temp)) > 0) {
			//把字条串连接到尾部
			sb.append(new String(temp, 0, len));
		}
		//关闭输入流
		input.close();
		//返回字符串
		return sb.toString();
	}

}
