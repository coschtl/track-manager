package at.dcosta.android.fw.oldprops;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;
import at.dcosta.android.fw.R;
import at.dcosta.android.fw.gui.IconListActivity;
import at.dcosta.android.fw.gui.IconListBean;
import at.dcosta.android.fw.gui.IconListIdHolder;

public class FileChooser extends IconListActivity {

	private static final int SELECT_ID = Menu.FIRST;

	private static final Set<String> IGNORE_DIRS;
	static {
		IGNORE_DIRS = new HashSet<String>();
		IGNORE_DIRS.add("/dev");
		IGNORE_DIRS.add("/etc");
		IGNORE_DIRS.add("/cache");
		IGNORE_DIRS.add("/config");
		IGNORE_DIRS.add("/dev");
		IGNORE_DIRS.add("/proc");
		IGNORE_DIRS.add("/root");
		IGNORE_DIRS.add("/sbin");
		IGNORE_DIRS.add("/sys");
		IGNORE_DIRS.add("/system");
	}

	private File currentDir;

	private List<Option> currentSubdirs;

	private String type, name;
	private int status;
	private PropertyIds ids;

	public FileChooser() {
		super(null);
	}

	private void fill() {
		File[] dirs = currentDir.listFiles();
		currentSubdirs = new ArrayList<Option>();
		try {
			int i = 0;
			for (File ff : dirs) {
				if (ff.isDirectory() && !IGNORE_DIRS.contains(ff.getAbsolutePath())) {
					currentSubdirs.add(new Option(i++, ff.getName(), "Folder", ff.getAbsolutePath(), ids.getIcon(type)));
				}
			}
		} catch (Exception e) {
			// ignore
		}
		Collections.sort(currentSubdirs);
		// if (!currentDir.getName().equalsIgnoreCase("sdcard")) {
		if (!currentDir.getName().equalsIgnoreCase("")) {
			currentSubdirs.add(0, new Option(-1, ".", getString(R.string.actual_directory), currentDir.getAbsolutePath(), ids.getIcon(type)));
			currentSubdirs.add(1, new Option(-1, "..", getString(R.string.parent_directory), currentDir.getParent(), ids.getIcon(type)));
		}
	}

	@Override
	public List<? extends IconListBean> getBeans() {
		return currentSubdirs;
	}

	@Override
	public String getHeadline() {
		return currentDir.getAbsolutePath();
	}

	private void handleDirClicked(Option optionItem) {
		if (!getString(R.string.parent_directory).equals(optionItem.getData())) {
			Bundle bundle = new Bundle();
			bundle.putString(PropertyDbAdapter.DB.COL_TYPE, type);
			bundle.putString(PropertyDbAdapter.DB.COL_NAME, name);
			bundle.putString(PropertyDbAdapter.DB.COL_VALUE, optionItem.getPath());
			bundle.putInt(PropertyDbAdapter.DB.COL_STATUS, status);

			Intent mIntent = new Intent();
			mIntent.putExtras(bundle);
			setResult(RESULT_OK, mIntent);
			finish();
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case SELECT_ID:
				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
				Option optionItem = (Option) getItem((int) info.id);
				handleDirClicked(optionItem);
				return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// currentDir = new File("/sdcard/");
		Bundle extras = getIntent().getExtras();
		type = extras.getString(PropertyDbAdapter.DB.COL_TYPE);
		name = extras.getString(PropertyDbAdapter.DB.COL_NAME);
		status = extras.getInt(PropertyDbAdapter.DB.COL_STATUS);
		ids = (PropertyIds) extras.get(PropertyIds.class.getName());
		setIdHolder((IconListIdHolder) extras.get(IconListIdHolder.class.getName()));
		currentDir = new File("/");
		fill();
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, SELECT_ID, 0, R.string.menu_select);
	}

	private void onFileClick(Option o) {
		Toast.makeText(this, "File Clicked: " + o.getName(), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Option o = (Option) getItem(position);
		if (o.getData().equalsIgnoreCase("folder") || o.getData().equalsIgnoreCase("parent directory")) {
			currentDir = new File(o.getPath());
			fill();
		} else {
			if (".".equals(o.getName())) {
				handleDirClicked(o);
			} else {
				onFileClick(o);
			}
		}
		super.onItemClick(parent, view, position, id);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return false;
	}

}