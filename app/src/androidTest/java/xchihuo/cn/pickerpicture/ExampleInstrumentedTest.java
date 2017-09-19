package xchihuo.cn.pickerpicture;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
	@Test
	public void useAppContext() throws Exception {
		// Context of the app under test.
		Context appContext = InstrumentationRegistry.getTargetContext();

		int actionBarSize = getSystemComponentDimen(appContext, "actionbar_default_height");
	/*	int identifier = appContext.getResources().getIdentifier("actionbar_default_height",
				"dimen", "android");
		System.out.println(actionBarSize);
		int dimensionPixelSize = appContext.getResources().getDimensionPixelSize
				(identifier);*/
		Log.e("nihao", "useAppContext: "+actionBarSize);
		//Log.e("nihao", "useAppContext: "+dimensionPixelSize);
		assertEquals("xchihuo.cn.pickerpicture", appContext.getPackageName());
	}

	private static int getSystemComponentDimen(Context context, String dimenName){
		// 反射手机运行的类：android.R.dimen.status_bar_height.
		int statusHeight = -1;
		try {
			Class<?> clazz = Class.forName("com.android.internal.R$dimen");
			Object object = clazz.newInstance();
			String heightStr = clazz.getField(dimenName).get(object).toString();
			//这是id 不是dp
			int height = Integer.parseInt(heightStr);
			//dp--->px
			statusHeight = context.getResources().getDimensionPixelSize(height);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return statusHeight;
	}
}
