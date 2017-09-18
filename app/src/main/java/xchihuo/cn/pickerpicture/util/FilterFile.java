package xchihuo.cn.pickerpicture.util;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;

/**
 * Created by crf on 2017/9/15.
 * company:逸辰
 * qq:952786280
 * feature:
 */

public class FilterFile extends File {
	public FilterFile(@NonNull String pathname) {
		super(pathname);
	}

	public FilterFile(String parent, @NonNull String child) {
		super(parent, child);
	}

	public FilterFile(File parent, @NonNull String child) {
		super(parent, child);
	}

	public FilterFile(@NonNull URI uri) {
		super(uri);
	}

	@Override
	public String[] list(FilenameFilter filter) {
		FilenameFilter filenameFilter=filter;
		if(filenameFilter==null){
			filenameFilter=new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					int i = name.lastIndexOf(".");
					Log.e("tt", "accept: i"+i);
					if(i>0)
					{String suffix = name.substring(i);
						if (suffix.equals(".jpg") || suffix.equals(".jpeg") ||
								suffix.equals(".png"))
							return true;}
					return false;
				}
			};
		}
		return super.list(filenameFilter);
	}
}
