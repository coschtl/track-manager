package at.dcosta.android.fw.gui;

/*
 * Copyright (C) 2009 codemobiles.com.
 * http://www.codemobiles.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * author: Chaiyasit Tayabovorn 
 * email: chaiyasit.t@gmail.com
 */

import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public abstract class IconListActivity extends ListActivity implements OnItemClickListener, OnTouchListener {

	public static class EfficientAdapter extends BaseAdapter implements Filterable {

		static class ViewHolder {
			TextView lineHead, lineBody;
			ImageView lineIcon, additionalIcon;
		}

		protected final IconListIdHolder idHolder;
		private final List<? extends IconListBean> beans;
		private final LayoutInflater mInflater;

		public EfficientAdapter(Context context, IconListIdHolder idHolder, List<? extends IconListBean> beans) {
			// Cache the LayoutInflate to avoid asking for a new one each time.
			mInflater = LayoutInflater.from(context);
			this.idHolder = idHolder;
			this.beans = beans;
		}

		@Override
		public int getCount() {
			return beans.size();
		}

		@Override
		public Filter getFilter() {
			return null;
		}

		@Override
		public Object getItem(int position) {
			return beans.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		/**
		 * Make a view to hold each row.
		 * 
		 * @see android.widget.ListAdapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// A ViewHolder keeps references to children views to avoid
			// unneccessary calls
			// to findViewById() on each row.
			ViewHolder holder;
			IconListBean bean = beans.get(position);

			// When convertView is not null, we can reuse it directly, there is
			// no need
			// to reinflate it. We only inflate a new View when the convertView
			// supplied
			// by ListView is null.
			if (convertView == null) {
				convertView = mInflater.inflate(idHolder.getListRowLayoutId(), null);

				// Creates a ViewHolder and store references to the two children
				// views
				// we want to bind data to.
				holder = new ViewHolder();
				holder.lineHead = (TextView) convertView.findViewById(idHolder.getLineHeadId());
				holder.lineBody = (TextView) convertView.findViewById(idHolder.getLineBodyId());
				if (idHolder.getLineIconId() > 0) {
					holder.lineIcon = (ImageView) convertView.findViewById(idHolder.getLineIconId());
				}
				int icon2Id = idHolder.getIntExtra(IconListIdHolder.KEY_ADDITIONAL_ICON, -1);
				if (icon2Id > 0) {
					holder.additionalIcon = (ImageView) convertView.findViewById(icon2Id);
				}
				convertView.setTag(holder);
			} else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data efficiently with the holder.

			holder.lineHead.setText(bean.getHead());
			holder.lineBody.setText(bean.getBody());
			int iconId = bean.getIconId();
			if (idHolder.getLineIconId() > 0 && iconId > 0) {
				holder.lineIcon.setImageResource(iconId);
			} else {
				holder.lineIcon.setImageResource(0);
			}
			int icon2 = bean.getIntExtra(IconListBean.KEY_ADDITIONAL_ICON, -1);
			if (holder.additionalIcon != null) {
				if (icon2 > 0) {
					holder.additionalIcon.setImageResource(icon2);
					holder.additionalIcon.setId((int) bean.getId());
				} else {
					holder.additionalIcon.setImageResource(0);
					holder.additionalIcon.setId(-1);
				}
			}
			return convertView;
		}
	}

	private EfficientAdapter adap;
	private ListView list;
	protected IconListIdHolder idHolder;

	private TextView header;

	public IconListActivity(IconListIdHolder idHolder) {
		this.idHolder = idHolder;
	}

	public abstract List<? extends IconListBean> getBeans();

	public abstract String getHeadline();

	public IconListBean getItem(int position) {
		return (IconListBean) adap.getItem(position);
	}

	public ListView getList() {
		return list;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(idHolder.getListLayoutId());
		header = (TextView) findViewById(idHolder.getHeaderId());
		header.setOnTouchListener(this);
		setHeadline();
		list = (ListView) findViewById(android.R.id.list);
		list.setOnItemClickListener(this);
		list.setOnTouchListener(this);
		list.setOnCreateContextMenuListener(this);
		updateView();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		updateView();
	}

	private void setHeadline() {
		String headline = getHeadline();
		if (header != null && headline != null) {
			header.setText(getHeadline());
		}
	}

	public void setIdHolder(IconListIdHolder idHolder) {
		this.idHolder = idHolder;
	}

	public void updateView() {
		setHeadline();
		adap = new EfficientAdapter(this, idHolder, getBeans());
		setListAdapter(adap);
	}

}
