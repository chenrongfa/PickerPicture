package xchihuo.cn.pickerpicture;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import xchihuo.cn.pickerpicture.bean.ImageFolder;
import xchihuo.cn.pickerpicture.imageloader.ListImageDirPopupWindow;
import xchihuo.cn.pickerpicture.imageloader.MyAdapter;
import xchihuo.cn.pickerpicture.util.CacheUtils;
import xchihuo.cn.pickerpicture.util.FilterFile;
public class MainActivity extends AppCompatActivity implements ListImageDirPopupWindow.OnImageDirSelected,

		MyAdapter.SingleChoiceSelectListener{
	private GridView idGridView;
	private RelativeLayout idBottomLy;
	private TextView idChooseDir;
	private TextView idTotalCount;
	private Toolbar mToolbar;

	private ListImageDirPopupWindow mListImageDirPopupWindow;
	/**
	 *  图片的数量
	 */
	private int totalCount;
	/**
	 *  所有图片的路径
	 */
	private List<String> allImagePath=new LinkedList<>();
	private int currentCount;
	private MyAdapter mAdapter;
	private List<String> mImageContainer=new LinkedList<>();
	//当先图片所在父的路径
	private FilterFile currentFile;
	private HashSet<String> dirPaths=new HashSet<>();
	private List<String> rootPath=new LinkedList<>() ;
	private List<ImageFolder> imageFolders=new ArrayList<>();
    private CacheUtils cache;
	private Dialog mProgressDialog;
	private Handler handler=new Handler(){
	  @Override
	  public void handleMessage(Message msg) {
		  mProgressDialog.dismiss();
		  // 为View绑定数据
		  bind2View(currentFile,null);
		  // 初始化展示文件夹的popupWindw
		  initListDirPopupWindw();
	  }
  };
	private double mScreenHeight;

	@Override
	protected void onRestart() {
		mAdapter.notifyDataSetChanged();
		super.onRestart();
	}

	private void bind2View(File currentFile, ImageFolder folder)
	{
		if (currentFile == null)
		{
			Toast.makeText(getApplicationContext(), "一张图片没扫描到",
					Toast.LENGTH_SHORT).show();
			return;
		}


		/**
		 * 可以看到文件夹的路径和图片的路径分开保存，极大的减少了内存的消耗；
		 */
		if (folder==null){
			mAdapter = new MyAdapter(getApplicationContext(), allImagePath,
					R.layout.grid_item);

			idGridView.setAdapter(mAdapter);
		idTotalCount.setText(totalCount + "张");}
		else{
			String file[] =currentFile.list(null);

			mImageContainer.clear();
			for(String path:file){
				mImageContainer.add(folder.getDir()+"/"+path);
			}

			mAdapter = new MyAdapter(getApplicationContext(), mImageContainer,
					R.layout.grid_item);
			idGridView.setAdapter(mAdapter);
			idTotalCount.setText(folder.getCount() + "张");
		}
		mAdapter.setSingleChoiceSelectListener(this);
	}
	private void findViews() {
		idGridView = (GridView)findViewById( R.id.id_gridView );
		idBottomLy = (RelativeLayout)findViewById( R.id.id_bottom_ly );
		idChooseDir = (TextView)findViewById( R.id.id_choose_dir );
		idTotalCount = (TextView)findViewById( R.id.id_total_count );
		mToolbar= (Toolbar) findViewById(R.id.tb_bar);
		setSupportActionBar(mToolbar);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		DisplayMetrics outMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		cache=CacheUtils.getInstance();
		mScreenHeight = outMetrics.heightPixels;
		findViews();
		getImages();
		initListDirPopupWindw();
		initEvent();
	}

	private void initEvent() {
		idBottomLy.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mListImageDirPopupWindow.showAsDropDown(idBottomLy);
			}
		});
	}

	/**
	 *  初始化 popwindow
	 */
	private void initListDirPopupWindw()
	{
		mListImageDirPopupWindow = new ListImageDirPopupWindow(
				ViewGroup.LayoutParams.MATCH_PARENT, (int) (mScreenHeight * 0.7),
				imageFolders, LayoutInflater.from(getApplicationContext())
				.inflate(R.layout.list_dir, null));

		mListImageDirPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {


			@Override
			public void onDismiss() {
				// 设置背景颜色变暗
				WindowManager.LayoutParams lp = getWindow().getAttributes();
				lp.alpha = 1.0f;
				getWindow().setAttributes(lp);
			}
		});
		// 设置选择文件夹的回调
		mListImageDirPopupWindow.setOnImageDirSelected(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main,menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.id.single_choice:
				Toast.makeText(this, "单选", Toast.LENGTH_SHORT).show();
				cache.save("choice",0);
				break;
			case R.id.more_choice:
				Toast.makeText(this, "多选", Toast.LENGTH_SHORT).show();
				cache.save("choice",1);
				break;
			case R.id.refresh:
				getImages();
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	/*
			 遍历
			*/
	private void getImages() {
		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			Toast.makeText(this, "没有外部存储", Toast.LENGTH_SHORT).show();
			return;
		}
		mProgressDialog= ProgressDialog.show(this, null, "正在加载...");
		new Thread(new Runnable() {
			@Override
			public void run() {

				Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				//扫描
				ContentResolver contentResolver=getContentResolver();
				Cursor query = contentResolver.query(uri, null, MediaStore.Images.Media
						.MIME_TYPE + "=?or " +
						MediaStore.Images.Media.MIME_TYPE + "=?", new
						String[]{"image/jpeg", "image/png"}, null);
				while (query.moveToNext()){
					String firstImage=null;
					String path = query.getString(query.getColumnIndex(MediaStore
							.Images.Media.DATA));
					Log.e("tt", "run: "+path );
					File file=new File(path);
					if(firstImage==null){
						firstImage=path;
					}
					File parentFile =  file.getParentFile();
					if(parentFile==null)
						continue;
					String absolutePath = parentFile.getAbsolutePath();
					ImageFolder imageFolder=null;
					FilterFile filterFile=new FilterFile(absolutePath);
					if(dirPaths.contains(absolutePath))
						continue;
					else{
						dirPaths.add(absolutePath);
						imageFolder=new ImageFolder();
						imageFolder.setDir(absolutePath);
						imageFolder.setFirstImagePath(firstImage);
						String[] list = filterFile.list(null);
						for(String paths:list){
							allImagePath.add(absolutePath+"/"+paths);
						}
						int pictureSzie = list.length;
						imageFolder.setCount(pictureSzie);
						if(pictureSzie>currentCount){
							currentCount=pictureSzie;
							currentFile=filterFile;
						}
						totalCount+=pictureSzie;
						imageFolders.add(imageFolder);
					}
				}
				query.close();
				handler.sendEmptyMessage(0);
			}
		}).start();
	}

	@Override
	public void selected(ImageFolder folder) {
		  FilterFile file=new FilterFile(folder.getDir());
		  bind2View(file,folder);
		  mListImageDirPopupWindow.dismiss();
	}


	@Override
	public void onSingleChoiceSelectListener(String path) {
		Intent intent=new Intent(this,PictureCutActivity.class);
		intent.putExtra("IMAGEPATH",path);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		startActivity(intent);
	}
}
