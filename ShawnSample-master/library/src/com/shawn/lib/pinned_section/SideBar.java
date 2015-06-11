package com.shawn.lib.pinned_section;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.shawn.lib.R;

public class SideBar extends View {
	private OnTouchingLetterChangedListener onTouchingLetterChangedListener;
	public static final String TAG_SEARCH = "Search";
	// 默认索引为24个字母加#
	private String[] sections = { "A", "B", "C", "D", "E", "F", "G", "H", "I",
			"J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
			"W", "X", "Y", "Z", "#" };
	private int choose = -1;
	private Paint paint = new Paint();
	// 中间的提示文字
	private TextView mTextDialog;
	private ImageButton mImageDialog;
	// 是否显示搜索索引
	private boolean showSearch;
	private Drawable iconSearch;
	
	public void setPopView(TextView mTextDialog, ImageButton mImageDialog) {
		this.mTextDialog = mTextDialog;
		this.mImageDialog = mImageDialog;
	}

	public void setSections(boolean showSearch, String[] sections) {
		this.showSearch = showSearch;
		if (showSearch) {
			this.sections = new String[sections.length + 1];
			this.sections[0] = TAG_SEARCH;
			for (int i = 0; i < sections.length; i++) {
				this.sections[i + 1] = sections[i];
			}
		}
		iconSearch = getResources().getDrawable(R.drawable.search_bar_icon_normal);
		iconSearch.setBounds(0, 0, iconSearch.getIntrinsicWidth(),
				iconSearch.getIntrinsicHeight());
		invalidate();
	}

	public SideBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public SideBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SideBar(Context context) {
		super(context);
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int height = getHeight();
		int width = getWidth();
		// 索引填充高度
		// int singleHeight = height / sections.length;
		// 设置单个索引字母高度为35像素
		int singleHeight = 35;
		int emptyHeight = (height - singleHeight * sections.length) / 2;

		for (int i = 0; i < sections.length; i++) {
			float yPos = singleHeight * i + emptyHeight;
			if (showSearch && i == 0) {
				paint.setColor(Color.rgb(33, 65, 98));
				paint.setAntiAlias(true);
				float xPos = width / 2 - 15;
				canvas.drawBitmap(((BitmapDrawable)iconSearch).getBitmap(), xPos, yPos, paint);
			} else {
				paint.setColor(Color.rgb(33, 65, 98));
				paint.setTypeface(Typeface.DEFAULT_BOLD);
				paint.setAntiAlias(true);
				paint.setTextSize(30);
				// 触摸到的文字变色
				if (i == choose) {
					paint.setColor(Color.parseColor("#3399ff"));
					paint.setFakeBoldText(true);
				}
				if (showSearch) {
					yPos += singleHeight;
				}
				float xPos = width / 2 - paint.measureText(sections[i]) / 2;
				canvas.drawText(sections[i], xPos, yPos, paint);
			}
			paint.reset();
		}

	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		int action = event.getAction();
		final float y = event.getY();
		final int oldChoose = choose;
		final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
		int singleHeight = 35;
		int emptyHeight = (getHeight() - singleHeight * sections.length) / 2;
		// 触摸超出索引时默认不处理
		if (y > emptyHeight + singleHeight * sections.length || y < emptyHeight) {
			action = MotionEvent.ACTION_CANCEL;
		}
		// 当前触摸的索引
		final int c = (int) ((y - emptyHeight) / singleHeight);

		switch (action) {
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_OUTSIDE:
			choose = -1;//
			invalidate();
			if (mTextDialog != null) {
				mTextDialog.setVisibility(View.INVISIBLE);
			}
			if (mImageDialog != null) {
				mImageDialog.setVisibility(View.INVISIBLE);
			}
			break;

		default:
			if (oldChoose != c) {
				if (c >= 0 && c < sections.length) {
					if (listener != null) {
						listener.onTouchingLetterChanged(sections[c]);
					}
					if (mTextDialog != null) {
						if (sections[c].equals(TAG_SEARCH) && mImageDialog != null) {
							mImageDialog.setImageResource(R.drawable.search_bar_icon_normal);
							mImageDialog.setVisibility(View.VISIBLE);
							mTextDialog.setVisibility(View.INVISIBLE);
						} else {
							mTextDialog.setText(sections[c]);
							mTextDialog.setVisibility(View.VISIBLE);
							if (mImageDialog != null) {
								mImageDialog.setVisibility(View.INVISIBLE);
							}
						}
					}
					choose = c;
					invalidate();
				}
			}

			break;
		}
		return true;
	}

	public void setOnTouchingLetterChangedListener(
			OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
		this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
	}

	public interface OnTouchingLetterChangedListener {
		public void onTouchingLetterChanged(String s);
	}

}