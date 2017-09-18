package xchihuo.cn.pickerpicture.imageloader;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import java.util.LinkedList;
import java.util.List;

import xchihuo.cn.pickerpicture.R;
import xchihuo.cn.pickerpicture.util.CacheUtils;
import xchihuo.cn.pickerpicture.util.CommonAdapter;
import xchihuo.cn.pickerpicture.util.ViewHolder;

public class MyAdapter extends CommonAdapter<String>
{

	public SingleChoiceSelectListener getSingleChoiceSelectListener() {
		return singleChoiceSelectListener;
	}

	/**
	 *  单选回调
	 */
	public interface  SingleChoiceSelectListener{
		void onSingleChoiceSelectListener(String path);

	}
	public List<String> getmSelectedImage() {
		return mSelectedImage;
	}

	public void setSingleChoiceSelectListener(SingleChoiceSelectListener
			                                          singleChoiceSelectListener) {
		this.singleChoiceSelectListener = singleChoiceSelectListener;
	}

	private SingleChoiceSelectListener singleChoiceSelectListener;
	public void setmSelectedImage(List<String> mSelectedImage) {
		this.mSelectedImage = mSelectedImage;
	}

	/**
	 * 用户选择的图片，存储为图片的完整路径
	 */

	public  List<String> mSelectedImage = new LinkedList<String>();



	public MyAdapter(Context context, List<String> mDatas, int itemLayoutId
			)
	{
		super(context, mDatas, itemLayoutId);


		Log.e("tt", "MyAdapter: "+mDatas.toString() );
	}


	@Override
	public void convert(final ViewHolder helper, final String item)
	{
		//设置no_pic
		helper.setImageResource(R.id.id_item_image, R.drawable.pictures_no);
		//设置no_selected
				helper.setImageResource(R.id.id_item_select,
						R.drawable.picture_unselected);
		//设置图片
		helper.setImageByUrl(R.id.id_item_image, item);
		
		final ImageView mImageView = helper.getView(R.id.id_item_image);
		final ImageView mSelect = helper.getView(R.id.id_item_select);
		
		mImageView.setColorFilter(null);
		//设置ImageView的点击事件
		mImageView.setOnClickListener(new OnClickListener()
		{
			//选择，则将图片变暗，反之则反之
			@Override
			public void onClick(View v)
			{

				// 已经选择过该图片
				//如果是单选
					if(CacheUtils.getInstance().get("choice",0)==0){
						if(mSelectedImage.contains(item)){
							mSelectedImage.clear();
							mSelect.setImageResource(R.drawable.picture_unselected);
							mImageView.setColorFilter(null);
						}else {
							mSelectedImage.clear();
							mSelectedImage.add(item);

							mSelect.setImageResource(R.drawable.pictures_selected);
							mImageView.setColorFilter(Color.parseColor("#77000000"));
							if (singleChoiceSelectListener != null) {
								singleChoiceSelectListener.onSingleChoiceSelectListener(item);

							}
						}
					} else {

						if (mSelectedImage.contains(item)) {
							mSelectedImage.remove(item);
							mSelect.setImageResource(R.drawable.picture_unselected);
							mImageView.setColorFilter(null);
						} else
						// 未选择该图片
						{
							mSelectedImage.add(item);
							mSelect.setImageResource(R.drawable.pictures_selected);
							mImageView.setColorFilter(Color.parseColor("#77000000"));
						}
					}
			}
		});
		
		/**
		 * 已经选择过的图片，显示出选择过的效果
		 */
		if (mSelectedImage.contains(item))
		{
			mSelect.setImageResource(R.drawable.pictures_selected);
			mImageView.setColorFilter(Color.parseColor("#77000000"));
		}

	}
}
