package xchihuo.cn.pickerpicture.imageloader;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.List;

import xchihuo.cn.pickerpicture.R;
import xchihuo.cn.pickerpicture.bean.ImageFolder;
import xchihuo.cn.pickerpicture.util.BasePopupWindowForListView;
import xchihuo.cn.pickerpicture.util.CommonAdapter;
import xchihuo.cn.pickerpicture.util.ViewHolder;


public class ListImageDirPopupWindow extends BasePopupWindowForListView<ImageFolder>
{
	private ListView mListDir;

	public ListImageDirPopupWindow(int width, int height,
			List<ImageFolder> datas, View convertView)
	{
		super(convertView, width, height, true, datas);
	}

	@Override
	public void initViews()
	{
		mListDir = (ListView) findViewById(R.id.id_list_dir);
		mListDir.setAdapter(new CommonAdapter<ImageFolder>(context, mDatas,
				R.layout.list_dir_item)
		{
			@Override
			public void convert(ViewHolder helper, ImageFolder item)
			{
				helper.setText(R.id.id_dir_item_name, item.getName());
				helper.setImageByUrl(R.id.id_dir_item_image,
						item.getFirstImagePath());
				helper.setText(R.id.id_dir_item_count, item.getCount() + "张");
			}
		});
	}

	public interface OnImageDirSelected
	{
		void selected(ImageFolder floder);
	}

	private OnImageDirSelected mImageDirSelected;

	public void setOnImageDirSelected(OnImageDirSelected mImageDirSelected)
	{
		this.mImageDirSelected = mImageDirSelected;
	}

	@Override
	public void initEvents()
	{
		mListDir.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{

				if (mImageDirSelected != null)
				{
					mImageDirSelected.selected(mDatas.get(position));
				}
			}
		});
	}

	@Override
	public void init()
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void beforeInitWeNeedSomeParams(Object... params)
	{
		// TODO Auto-generated method stub
	}

}
