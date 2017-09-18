package xchihuo.cn.pickerpicture.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import net.bither.util.ImageFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import static net.bither.util.BitmapUtil.getImageViewWH;


/**
 * Created by crf on 2017/9/14.
 * company:逸辰
 * qq:952786280
 * feature:
 */

public class ImageLoader {

  private ImageFactory imageFactory=new ImageFactory();

   private List<String> cachePath=new LinkedList<>();
	private static final int LOADER_IMAGE = 0;
	private static final int RUN_TASK = 1;
	private static ImageLoader mImageLoader;

	/**
	 * @param count 线程池
	 * @param type  队列形式出,还是以栈的形式出
	 */
	private ImageLoader(int count, Type type) {

		init(count, type);
		imageLoader.start();
	}

	private void init(int count, Type type) {
		DEFAULT_POOL_COUNT = count;
		currentType = type;

		mPool = Executors.newFixedThreadPool(DEFAULT_POOL_COUNT);
		long totalMemory = Runtime.getRuntime().totalMemory();
		cache = new LruCache<String, Bitmap>((int) (totalMemory / 8)) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getByteCount();
			}
		};
		imageLoader = new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				mPoolHandler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						try {
							semaphoreRunTask.acquire();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Runnable r= getTask();
						if(r!=null)
						mPool.execute(r);

					}
				};
				//activityThread主线程也差不多这样写的 ,做死循环
				mPoolHandlerSemaphore.release();//放行
				Looper.loop();
			}
		});
	}

	/**
	 *
	 * @return
	 */
	private Runnable getTask() {
		if(currentType==Type.FIFO){
		return	tasks.removeFirst();

		}else{
			return tasks.removeLast();
		}
	}

	/*
	  默认后进先出
	 */
	private Type currentType = Type.LIFO;

	public static ImageLoader getmImageLoader(int count, Type type) {
		synchronized (ImageLoader.class) {
			if (mImageLoader == null) {
				mImageLoader = new ImageLoader(count, type);
			}
		}
		return mImageLoader;
	}

	/**
	 * 后台加载图片线程
	 */
	private Thread imageLoader;
	/**
	 * 线程池
	 */
	private ExecutorService mPool;
	/**
	 * 线程中的handler
	 */
	private Handler mPoolHandler;
	/**
	 * 线程数量
	 */
	private Semaphore semaphoreRunTask=new Semaphore(DEFAULT_POOL_COUNT);;
	private static int DEFAULT_POOL_COUNT = 3;

	private Handler mUiHandler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what==LOADER_IMAGE){
			 ImageBean imageBean= (ImageBean) msg.obj;
				ImageView imageView = imageBean.imageView;
				Bitmap bm = imageBean.bitmap;
				String path = imageBean.path;
				if (imageView.getTag().toString().equals(path))
				{
					imageView.setImageBitmap(bm);
				}
			}
		}
	};

	public enum Type {
		FIFO, LIFO;
	}

	private LruCache<String, Bitmap> cache;
	private LinkedList<Runnable> tasks=new LinkedList<>();

	/**
	 * 加载图片
	 *
	 * @param path
	 * @param v
	 */

	public void loadImage(final String path, final ImageView v) {
		v.setTag(path);
		/**
		 *  先从内存中去 ,没有就去读文件
		 *
		 */
		 Bitmap cacheBitmap = getCacheBitmap(path);
		if(cacheBitmap !=null){
			//发送 message uihandler加载图片
			sendToImageLoad(path, v, cacheBitmap);
		}else{
			 addTask(new Runnable(){
				 @Override
				 public void run() {

					 /*File file=new File(path).getParentFile();
					 String parent = file.getAbsolutePath();
					 int  root= parent.lastIndexOf("/");
					 int i = path.lastIndexOf("/");
					 String rootSu = parent.substring(root);
					 String name = path.substring(i);

					 String exist = tempPath + rootSu ;
					 File fileExist=new File(exist);
					 if(!fileExist.exists()){
						 fileExist.mkdirs();
					 }
					 exist+=name;
					 Bitmap cacheBitmap=null;
					 if(!cachePath.contains(rootSu+name)) {
						 cachePath.add(rootSu+name);

						 cacheBitmap = BitmapUtil.compressBitmap(path, exist);

					 }else{
						 cacheBitmap= BitmapFactory.decodeFile(exist);
					 } 内存消耗过大 等以后有能力 直接 从内存压缩不存储*/

					 //Bitmap bitmap = decodeFile(path,v);
					 /*Bitmap bm = BitmapUtil.getInstance().compressBySimpleOfOrdinary
							 (v, path, null, true);*/
					 //采样适配 imageView 都会有马赛克
					/*int wh[]= BitmapUtil.getImageViewWH(v.getContext().getResources().getDisplayMetrics().widthPixels/3, (int) (v.getContext().getResources().getDisplayMetrics().heightPixels*0.8), v);

					 Bitmap bm = imageFactory.ratio(path, wh[0], wh[1]);*/
				        Bitmap bm	=decodeFile(path,v);
				   /*	 if(bm!=null){
						 if(cache.get(path)==null){
							 cache.put(path,bm);
						 }

					 }*/
					 sendToImageLoad(path, v, bm);

					 semaphoreRunTask.release();
				 }
			 });

		}
	}
	private Semaphore mPoolHandlerSemaphore=new Semaphore(0);

	/**
	 *
	 *  添加任务,并发送消息
	 * @param task
	 */
  public void addTask(Runnable task){
	  tasks.add(task);
	  if(mPoolHandler==null){
		  try {
			  //获得许可,没有就堵塞需要release 放行
			  mPoolHandlerSemaphore.acquire();
		  } catch (InterruptedException e) {
			  e.printStackTrace();
		  }
	  }
	  mPoolHandler.sendEmptyMessage(RUN_TASK);
  }
	private void sendToImageLoad(String path, ImageView v, Bitmap cacheBitmap) {
		Message message=Message.obtain();
		message.what=LOADER_IMAGE;
		ImageBean imageBean=new ImageBean();
		imageBean.bitmap=cacheBitmap;
		imageBean.imageView=v;
		imageBean.path=path;
		message.obj=imageBean;
		mUiHandler.sendMessage(message);
	}

	/**
	 *  加载并压缩,存储到内存
	 * @param path
	 * @return
	 */
	public  Bitmap decodeFile(String path,ImageView v) {

		BitmapFactory.Options option=new BitmapFactory.Options();
		//只读边
		option.inJustDecodeBounds=true;

		BitmapFactory.decodeFile(path, option);
		int bmWidth=option.outWidth;
		int bmHeight=option.outHeight;

		 int size= getSamplieSize(bmWidth,bmHeight,v);
		option.inSampleSize=size;
		//读出片
		option.inJustDecodeBounds=false;
		Bitmap bm= BitmapFactory.decodeFile(path,option);
         if(bm!=null){
	         if(cache.get(path)==null){
		         cache.put(path,bm);
	         }
         }
		return bm;
	}

	/**
	 *  得到其缩放比例
	 * @param bmWidth
	 * @param bmHeight
	 * @param v
	 * @return
	 */
	private int getSamplieSize(int bmWidth, int bmHeight, ImageView v) {
		int ratio=0;
		int wh[] = getImageViewWH(v.getContext().getResources().getDisplayMetrics().widthPixels/3, (int) (v.getContext().getResources().getDisplayMetrics().heightPixels*0.8), v);
		if(bmWidth>wh[0]||bmHeight>wh[1]){

			ratio=Math.max(bmHeight/wh[0],bmWidth/wh[1]);

		}
		if(bmWidth>bmHeight&&bmWidth>wh[0]){
			ratio=bmWidth/wh[0];
		}else if(bmWidth<bmHeight&&bmHeight>wh[1]){
			ratio=bmHeight/wh[1];
		}
		if(ratio<1){
			ratio=1;
		}
		return ratio;
	}





	private Bitmap getCacheBitmap(String path) {

		return cache.get(path);
	}
	private class ImageBean{
		String path;
		Bitmap bitmap;
		ImageView imageView;

	}
}
