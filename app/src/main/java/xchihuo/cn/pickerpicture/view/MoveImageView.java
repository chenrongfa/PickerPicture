package xchihuo.cn.pickerpicture.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Scroller;

import net.bither.util.BitmapUtil;

import java.io.File;

/**
 * Created by crf on 2017/9/15.
 * company:逸辰
 * qq:952786280
 * feature:
 */

@SuppressLint("AppCompatCustomView")
public class MoveImageView extends ImageView implements GestureDetector.OnGestureListener{
	public MoveImageView(Context context) {
		this(context, null);
	}

	public MoveImageView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}
	Scroller scroller;
	Paint paint;
	public MoveImageView(Context context, @Nullable AttributeSet attrs, int
			defStyleAttr) {
		super(context, attrs, defStyleAttr);
		File file = new File(tempPath);
		if (!file.exists()) {
			file.mkdirs();
		}
		scroller=new Scroller(context);
     gestureDetector=new GestureDetector(context,  this);
		int identifier = getResources().getIdentifier("status_bar_height",
				"dimen", "android");


		status_bar_height = getResources().getDimensionPixelSize
				(identifier);
	}
	int status_bar_height;
  private GestureDetector gestureDetector;
	@Override
	public void computeScroll() {
		if (scroller.computeScrollOffset()){
			moveX=scroller.getCurrX();
			moveY=scroller.getCurrY();
			invalidate();
		}
	}


	private String tempPath = Environment.getExternalStorageDirectory().getAbsolutePath
			() + "/tempCache";
	private float lastX;
	private float lastY;
	//高亮区域
	RectF rectF=new RectF();

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	private String path;
	Bitmap bitmap;
	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	@Override
	protected void onDraw(Canvas canvas) {
		Log.e("hei", "onDraw: "+width+"||"+height);
		if (path != null) {
			if (bitmap==null) {
				int i = path.lastIndexOf("/");
				String name = path.substring(i);
				bitmap = BitmapUtil.compressBitmap(path, tempPath + name);
			}
			Path path1 = new Path();
			path1.addRect(0, 0, width, height, Path.Direction.CCW);
			Path path2 = new Path();
			path2.addRect(rectF, Path.Direction.CCW);
			path1.op(path2, Path.Op.DIFFERENCE);
			canvas.save();

			if (!isFirst1) {
				int dx = (int) ((bitmap.getWidth() - rectF.width()) / 2);
				int dy = (int) ((bitmap.getHeight() - rectF.height()) / 2);
				isFirst1 = true;
				moveX = (int) (rectF.left-dx);
				moveY = (int) (rectF.top-dy);



			}

			canvas.clipPath(path1);
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(0);

			ColorMatrix matrix = new ColorMatrix();
			matrix.setSaturation(0); //变灰
			matrix.setScale(1,1,1,0.3f);//改变透明度

			ColorFilter colorFilter = new ColorMatrixColorFilter(matrix);

			paint.setColorFilter(colorFilter);
			canvas.drawBitmap(bitmap,    moveX ,
					  moveY, paint);
			canvas.restore();



			//矩形
			canvas.save();
			canvas.clipRect(rectF);
			canvas.drawBitmap(bitmap, moveX ,
					 moveY , null);
			canvas.restore();
			paint.setColorFilter(null);
			paint.setColor(Color.WHITE);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(4);
			canvas.drawRect(rectF,paint);


		}

		super.onDraw(canvas);
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
	}

	private int moveX;
	private int moveY;
	private boolean isFirst1;

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		return gestureDetector.onTouchEvent(event);
	}

	Region region = new Region();
	@Override
	public boolean onDown(MotionEvent event) {
		//是否点中图片getRawX getRawY 是屏幕左上角的位置1所以
		if (bitmap!=null) {
			Log.e("density", "onDown: "+density);
			region.set(moveX, moveY, moveX + bitmap.getWidth(), (int) (moveY + bitmap.getHeight()+40*density+status_bar_height));
			Rect bounds = region.getBounds();
			Log.e("density", "onDown: "+bounds.toString() );
			Log.e("density", "event.getRawX(): "+event.getRawX() );
			Log.e("density", "event.getRawY(): "+event.getRawY());
			Log.e("density", "event.getRawY(): "+event.getRawY());
			Log.e("density", "moveY + bitmap.getHeight() "+moveY + bitmap.getHeight());
			Log.e("density", "moveY + 40*density "+40*density);
			Log.e("density", "moveY + status_bar_height "+status_bar_height);
			if (region.contains((int)event.getRawX(),(int)event.getRawY())) {


				return true;

			}
		}

		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float
			distanceY) {
		Log.e("fling", "onTouchEvent:distanceX "+distanceX);
		Log.e("fling", "onTouchEvent:distanceX "+distanceY);

		//scroller.startScroll(moveX, moveY, (int)-distanceX, (int)-distanceY, 200);
		moveX+=-distanceX; moveY+=(int)-distanceY;
		invalidate();
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {

	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float
			velocityY) {


		return false;
	}

	public interface OnMoveListener {
		void moveListener(int left, int top);
	}
	public void setOnMoveListener(OnMoveListener onMoveListener) {
		this.onMoveListener = onMoveListener;
	}
	public OnMoveListener onMoveListener;
	private int width;
	private int height;



	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		Log.e("hei", "onDraw:ccc11 " );
		width = getResources().getDisplayMetrics().widthPixels;
		height = getResources().getDisplayMetrics().heightPixels;

		int[] imageViewWH = BitmapUtil.getImageViewWH(width, height, this);
		width=imageViewWH[0];
		height=imageViewWH[1];
		rectF.set(width/8,height/8,width*7/8,height*7/8);
	}
	float density;
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {

		super.onWindowFocusChanged(hasWindowFocus);
		if (hasWindowFocus){

			density = getResources().getDisplayMetrics().density;
		}
	}
}
