package xchihuo.cn.pickerpicture;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import java.io.File;

import xchihuo.cn.pickerpicture.view.MoveImageView;

public class PictureCutActivity extends AppCompatActivity {
   private MoveImageView iv_cut;

	private String tempPath= Environment.getExternalStorageDirectory().getAbsolutePath()+"/tempCache";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture_cut1);
		iv_cut= (MoveImageView) findViewById(R.id.iv_cut);
		final String imagepath = getIntent().getStringExtra("IMAGEPATH");
		File file = new File(tempPath);
		if(!file.exists()){
			file.mkdirs();
		}

		iv_cut.setPath(imagepath);

		iv_cut.invalidate();

	}
}
