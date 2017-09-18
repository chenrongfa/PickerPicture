package xchihuo.cn.pickerpicture.util;

import android.content.Context;
import android.content.SharedPreferences;

import xchihuo.cn.pickerpicture.BaseApp;


/**
 * 待完善
 * Created by chenrongfa on 2017/3/3
 * email:18720979339@163.com
 * qq:952786280
 * company:yy
 */

public class CacheUtils {
	private static CacheUtils mCache;
	private static final String name="weixin";
	private Context context;
	private CacheUtils(Context context){
		this.context=context;
		init();
	}
	public static CacheUtils getInstance(){
		synchronized (CacheUtils.class) {
			if (mCache == null) {
				mCache = new CacheUtils(BaseApp.getContext());
			}
		}
		return mCache;
	}
	public static SharedPreferences sharedPreferences;


	public void init() {
		sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
	}

	/**
	 *
	 *  保存
	 * @param key
	 * @param value
	 */

	public void save(String key, Object value) {
		SharedPreferences.Editor edit = sharedPreferences.edit();
		if (value instanceof String) {
			edit.putString(key, (String) value);

		} else if (value instanceof Boolean) {

			edit.putBoolean(key, (Boolean) value);
		} else if (value instanceof Integer) {
			edit.putInt(key, (Integer) value);
		}
       edit.commit();
	}

	/**
	 *  读取数据
	 * @param key
	 * @param t
	 * @param <T>
	 * @return
	 */
	public <T extends Object> T get(String key, T t) {
		if (t instanceof String) {
			return (T) sharedPreferences.getString(key, (String) t);
		} else if (t instanceof Integer) {

			Integer result = sharedPreferences.getInt(key, (Integer) t);
			return (T) result;
		} else if (t instanceof Boolean) {
			Boolean result = sharedPreferences.getBoolean(key, (Boolean) t);
			return (T) result;
		} else {
			return t;
		}

	}


}
