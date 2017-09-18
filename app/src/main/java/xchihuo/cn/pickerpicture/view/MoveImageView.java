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
import android.graphics.RectF;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
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
public class MoveImageView extends ImageView {
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


	}

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

		float y = event.getY();
		float x = event.getX();
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (!scroller.isFinished()) {
					scroller.abortAnimation();
					return false;
				}
				lastX = x;
				lastY = y;
				return true;

			case MotionEvent.ACTION_MOVE:
				Log.e("tt", "onTouchEvent: ");
				int dx = (int) (x - lastX);
				int dy = (int) (y - lastY);
				Log.e("tt", "onTouchEvent: dx" + dx);
				Log.e("tt", "onTouchEvent: dy" + dy);


				if (onMoveListener != null) {
					onMoveListener.moveListener(moveX, moveY);
				}
				scroller.startScroll(moveX, moveY, dx, dy, 500);
				invalidate();

				break;
			case MotionEvent.ACTION_UP:

				break;
		}
		return super.onTouchEvent(event);
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
}
