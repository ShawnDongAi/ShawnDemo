package com.shawn.sample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.shawn.lib.pinned_section.PinnedSectionListAdapter;
import com.shawn.lib.pinned_section.PinnedSectionSwipeMenuListView;
import com.shawn.lib.pinned_section.SideBar;
import com.shawn.lib.pinned_section.SideBar.OnTouchingLetterChangedListener;
import com.shawn.lib.pull_to_refresh.PullToRefreshBase;
import com.shawn.lib.pull_to_refresh.PullToRefreshBase.Mode;
import com.shawn.lib.pull_to_refresh.PullToRefreshBase.OnRefreshListener;
import com.shawn.lib.pull_to_refresh.PullToRefreshSpinnedSwipeMenuListView;
import com.shawn.lib.swipe_back.SwipeBackActivity;
import com.shawn.lib.swipe_menu.SwipeMenu;
import com.shawn.lib.swipe_menu.SwipeMenuCreator;
import com.shawn.lib.swipe_menu.SwipeMenuItem;
import com.shawn.lib.swipe_menu.SwipeMenuListView.OnMenuItemClickListener;


public class MainActivity extends SwipeBackActivity {

	private List<Item> allData = new ArrayList<Item>();
	private List<Item> itemList = new ArrayList<Item>();
	private FastScrollerAdapter mAdapter;
	private PullToRefreshSpinnedSwipeMenuListView mRootListView;
	private PinnedSectionSwipeMenuListView listView;
	private SideBar sideBar;
	private TextView mTextDialog;
	private ImageButton mImageDialog;
	private List<String> sectionList = new ArrayList<String>();
	
	private ClearEditText filterInput;
	private View headView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		setSwipeBackEnable(false);
		
		headView = View.inflate(this, R.layout.list_header, null);
		filterInput = (ClearEditText) headView.findViewById(R.id.filter_edit);
		filterInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				filterData(s.toString().toLowerCase());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		sideBar = (SideBar) findViewById(R.id.side_bar);
		mTextDialog = (TextView) findViewById(R.id.text_dialog);
		mImageDialog = (ImageButton) findViewById(R.id.image_dialog);
		sideBar.setPopView(mTextDialog, mImageDialog);
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
			@Override
			public void onTouchingLetterChanged(String s) {
				if (s.equals(SideBar.TAG_SEARCH)) {
					listView.setSelection(0);
					return;
				}
				int position = mAdapter.getPositionForSection(sectionList
						.indexOf(s));
				if (position >= 0) {
					listView.setSelection(position + 1);
				}
			}
		});

		mRootListView = (PullToRefreshSpinnedSwipeMenuListView) findViewById(R.id.listView);
		listView = mRootListView.getRefreshableView();
		listView.addHeaderView(headView);
		mAdapter = new FastScrollerAdapter(this);
		allData = filledData(getResources().getStringArray(R.array.date));
		Collections.sort(allData, new PinyinComparator());
		setDatas();
		listView.setAdapter(mAdapter);

		// step 1. create a MenuCreator
		SwipeMenuCreator creator = new SwipeMenuCreator() {

			@Override
			public void create(SwipeMenu menu) {
				// Create different menus depending on the view type
				switch (menu.getViewType()) {
				case 1:
					createMenu1(menu);
					break;
				case 2:
					createMenu2(menu);
					break;
				case 3:
					createMenu3(menu);
					break;
				}
			}

			private void createMenu1(SwipeMenu menu) {
				SwipeMenuItem item1 = new SwipeMenuItem(getApplicationContext());
				item1.setBackground(new ColorDrawable(Color.rgb(0xE5, 0x18,
						0x5E)));
				item1.setWidth(dp2px(90));
				item1.setIcon(R.drawable.ic_action_favorite);
				menu.addMenuItem(item1);
				SwipeMenuItem item2 = new SwipeMenuItem(getApplicationContext());
				item2.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
						0xCE)));
				item2.setWidth(dp2px(90));
				item2.setIcon(R.drawable.ic_action_good);
				menu.addMenuItem(item2);
			}

			private void createMenu2(SwipeMenu menu) {
				SwipeMenuItem item1 = new SwipeMenuItem(getApplicationContext());
				item1.setBackground(new ColorDrawable(Color.rgb(0xE5, 0xE0,
						0x3F)));
				item1.setWidth(dp2px(90));
				item1.setIcon(R.drawable.ic_action_important);
				menu.addMenuItem(item1);
				SwipeMenuItem item2 = new SwipeMenuItem(getApplicationContext());
				item2.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F,
						0x25)));
				item2.setWidth(dp2px(90));
				item2.setIcon(R.drawable.ic_action_discard);
				menu.addMenuItem(item2);
			}

			private void createMenu3(SwipeMenu menu) {
				SwipeMenuItem item1 = new SwipeMenuItem(getApplicationContext());
				item1.setBackground(new ColorDrawable(Color.rgb(0x30, 0xB1,
						0xF5)));
				item1.setWidth(dp2px(90));
				item1.setIcon(R.drawable.ic_action_about);
				menu.addMenuItem(item1);
				SwipeMenuItem item2 = new SwipeMenuItem(getApplicationContext());
				item2.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
						0xCE)));
				item2.setWidth(dp2px(90));
				item2.setIcon(R.drawable.ic_action_share);
				menu.addMenuItem(item2);
			}
		};
		// set creator
		listView.setMenuCreator(creator);
		listView.setOpenInterpolator(new DecelerateInterpolator(1.0f));
		listView.setCloseInterpolator(new DecelerateInterpolator(1.0f));

		// step 2. listener item click event
		listView.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(int position, SwipeMenu menu,
					int index) {
				startActivity(new Intent(MainActivity.this, SwipeBackDemoActivity.class));
				switch (menu.getViewType()) {
				case 1:
					switch (index) {
					case 0:
						break;
					case 1:
						break;
					default:
						break;
					}
					break;
				case 2:
					switch (index) {
					case 0:
						break;
					case 1:
						break;
					default:
						break;
					}
					break;
				case 3:
					switch (index) {
					case 0:
						break;
					case 1:
						break;
					default:
						break;
					}
					break;
				default:
					break;
				}
				return false;
			}
		});

		mRootListView.setMode(Mode.PULL_FROM_START);
		mRootListView
				.setOnRefreshListener(new OnRefreshListener<PinnedSectionSwipeMenuListView>() {

					@Override
					public void onRefresh(
							PullToRefreshBase<PinnedSectionSwipeMenuListView> refreshView) {
						String label = DateUtils.formatDateTime(
								getApplicationContext(),
								System.currentTimeMillis(),
								DateUtils.FORMAT_SHOW_TIME
										| DateUtils.FORMAT_SHOW_DATE
										| DateUtils.FORMAT_ABBREV_ALL);

						// Update the LastUpdatedLabel
						refreshView.getLoadingLayoutProxy()
								.setLastUpdatedLabel(label);
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								mRootListView.onRefreshComplete();
							}
						}, 3000);
					}
				});
	}

	class FastScrollerAdapter extends AppAdapter implements SectionIndexer {
		private Item[] sections;

		public FastScrollerAdapter(Context context) {
			super(context);
		}

		@Override
		protected void prepareSections(int sectionsNumber) {
			sections = new Item[sectionsNumber];
		}

		@Override
		protected void onSectionAdded(Item section, int sectionPosition) {
			sections[sectionPosition] = section;
		}

		@Override
		public Item[] getSections() {
			return sections;
		}

		@Override
		public int getPositionForSection(int section) {
			if (section >= sections.length) {
				section = sections.length - 1;
			}
			return sections[section].listPosition;
		}

		@Override
		public int getSectionForPosition(int position) {
			if (position >= getCount()) {
				position = getCount() - 1;
			}
			return getItem(position).sectionPosition;
		}
	}

	class AppAdapter extends BaseAdapter implements PinnedSectionListAdapter {
		private Context mContext;
		private List<Item> datas = new ArrayList<Item>();

		public AppAdapter(Context context) {
			this.mContext = context;
		}

		public void setDatas(List<Item> items, int setionCount) {
			prepareSections(setionCount);
			int sectionPos = 0;
			for (int i = 0; i < items.size(); i++) {
				Item section = items.get(i);
				if (section.type == Item.SECTION) {
					section.sectionPosition = i;
					section.listPosition = i + 1;
					onSectionAdded(section, sectionPos);
					sectionPos++;
				}
			}
			this.datas = items;
		}
		
		public void clear() {
			datas.clear();
		}

		@Override
		public int getCount() {
			return datas.size();
		}

		@Override
		public Item getItem(int position) {
			return datas.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public int getViewTypeCount() {
			// menu type count
			return 4;
		}

		@Override
		public int getItemViewType(int position) {
			// current menu type
			if (getItem(position).type == Item.SECTION) {
				return Item.SECTION;
			}
			return position % 3 + 1;
		}

		protected void prepareSections(int sectionsNumber) {
		}

		protected void onSectionAdded(Item section, int sectionPosition) {
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = View.inflate(getApplicationContext(),
						R.layout.item_list_app, null);
				new ViewHolder(convertView);
			}
			ViewHolder holder = (ViewHolder) convertView.getTag();
			Item item = getItem(position);
			// holder.iv_icon.setImageDrawable(item.loadIcon(getPackageManager()));
			holder.tv_name.setText(item.text);
			if (isItemViewTypePinned(getItemViewType(position))) {
				// view.setOnClickListener(PinnedSectionListActivity.this);
				holder.tv_name.setPadding(10, 10, 10, 10);
				holder.tv_name.setTextSize(12f);
				convertView.setBackgroundColor(Color.GRAY);
			}
			return convertView;
		}

		class ViewHolder {
			// ImageView iv_icon;
			TextView tv_name;

			public ViewHolder(View view) {
				// iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
				tv_name = (TextView) view.findViewById(R.id.tv_name);
				view.setTag(this);
			}
		}

		@Override
		public boolean isItemViewTypePinned(int viewType) {
			if (viewType == Item.SECTION) {
				return true;
			}
			return false;
		}
	}

	static class Item {

		public static final int ITEM = 1;
		public static final int SECTION = 0;

		public final int type;
		public final String text;

		public int sectionPosition;
		public int listPosition;

		public String sortLetter;

		public Item(int type, String text) {
			this.type = type;
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}

	}

	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getResources().getDisplayMetrics());
	}

	private List<Item> filledData(String[] date) {
		List<Item> mSortList = new ArrayList<Item>();
		for (int i = 0; i < date.length; i++) {
			Item sortModel = new Item(Item.ITEM, date[i]);
			String pinyin = CharacterParser.getInstance().getSelling(date[i]);
			String sortString = pinyin.substring(0, 1).toUpperCase();
			if (!sortString.matches("[A-Z]")) {
				sortString = "#";
			}
			sortModel.sortLetter = sortString.toUpperCase();
			mSortList.add(sortModel);
		}
		return mSortList;
	}
	
	
	private void setDatas() {
		itemList.clear();
		for (int i = 0; i < allData.size(); i++) {
			if (!sectionList.contains(allData.get(i).sortLetter)) {
				sectionList.add(allData.get(i).sortLetter);
				Item item = new Item(Item.SECTION, allData.get(i).sortLetter);
				itemList.add(item);
			}
			itemList.add(allData.get(i));
		}
		sideBar.setSections(true, sectionList.toArray(new String[] {}));
		mAdapter.setDatas(itemList, sectionList.size());
		mAdapter.notifyDataSetChanged();
	}
	
	private synchronized void filterData(String key) {
		if(TextUtils.isEmpty(key)){
			sectionList.clear();
			setDatas();
		} else {
			List<Item> filterList = new ArrayList<Item>();
			sectionList.clear();
			for (int i = 0; i < allData.size(); i++) {
				String text = allData.get(i).text;
				if (text.toLowerCase().contains(key)
						|| CharacterParser.getInstance().getSelling(text)
								.toLowerCase().contains(key)) {
					if (!sectionList.contains(allData.get(i).sortLetter)) {
						sectionList.add(allData.get(i).sortLetter);
						Item item = new Item(Item.SECTION,
								allData.get(i).sortLetter);
						filterList.add(item);
					}
					filterList.add(allData.get(i));
				}
			}
			sideBar.setSections(true, sectionList.toArray(new String[] {}));
			mAdapter.setDatas(filterList, sectionList.size());
			mAdapter.notifyDataSetChanged();
		}
	}
}
