package at.dcosta.android.fw.props.gui;

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
import at.dcosta.android.fw.R;
import at.dcosta.android.fw.gui.IconListActivity;
import at.dcosta.android.fw.gui.IconListBean;

public class FileChooser extends IconListActivity {

	private static final String PARENT_DIR = "..";
	private static final String CURRENT_DIR = ".";
	public static final String TYPE = "type";
	public static final String TYPE_FILE = "typeFile";
	public static final String TYPE_FOLDER = "typeFolder";

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

	private List<Option> currentList;

	private String type;

	public FileChooser() {
		super(null);
	}

	private void fill() {
		File[] dirs = currentDir.listFiles();
		currentList = new ArrayList<Option>();
		List<Option> files = new ArrayList<Option>();
		List<Option> folders = new ArrayList<Option>();
		try {
			int i = 0;
			for (File ff : dirs) {
				if (ff.isDirectory() && !IGNORE_DIRS.contains(ff.getAbsolutePath())) {
					folders.add(new Option(i++, ff.getName(), "Folder", ff.getAbsolutePath(), R.drawable.folder));
				} else {
					files.add(new Option(i++, ff.getName(), "File", ff.getAbsolutePath(), R.drawable.file));
				}
			}
		} catch (Exception e) {
			// ignore
		}
		Collections.sort(folders);
		Collections.sort(files);
		if (!"".equalsIgnoreCase(currentDir.getName())) {
			if (!isFileMode()) {
				currentList.add(new Option(-1, CURRENT_DIR, getString(R.string.actual_directory), currentDir.getAbsolutePath(), R.drawable.folder));
			}
			currentList.add(new Option(-1, PARENT_DIR, getString(R.string.parent_directory), currentDir.getParent(), R.drawable.folder));
		}
		currentList.addAll(folders);
		if (isFileMode()) {
			currentList.addAll(files);
		}
	}

	@Override
	public List<? extends IconListBean> getBeans() {
		return currentList;
	}

	@Override
	public String getHeadline() {
		return currentDir.getAbsolutePath();
	}

	private void handleDirClicked(Option optionItem) {
		if (!isFileMode()) {
			returnResult(optionItem);
		}
	}

	private void handleFileClicked(Option optionItem) {
		System.out.println("File clicked. isFileMode: " + isFileMode());
		if (isFileMode()) {
			returnResult(optionItem);
		}
	}

	private boolean isFileMode() {
		return TYPE_FILE.equals(type);
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
		Bundle extras = getIntent().getExtras();
		type = extras.getString(TYPE);
		setIdHolder((IdHolder) extras.get(IdHolder.class.getName()));
		currentDir = new File("/");
		fill();
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// super.onCreateContextMenu(menu, v, menuInfo);
		Option optionItem = (Option) getItem(((AdapterContextMenuInfo) menuInfo).position);
		System.out.println("pos: " + ((AdapterContextMenuInfo) menuInfo).position);
		System.out.println("data: " + optionItem.getData());
		System.out.println("name: " + optionItem.getName());
		System.out.println("path: " + optionItem.getPath());
		if ("Folder".equals(optionItem.getData())) {
			if (!isFileMode()) {
				menu.add(0, SELECT_ID, 0, R.string.menu_select);
			}
		} else {
			if (isFileMode()) {
				menu.add(0, SELECT_ID, 0, R.string.menu_select);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Option o = (Option) getItem(position);
		if (o.getData().equalsIgnoreCase("folder") || o.getData().equalsIgnoreCase("parent directory")) {
			currentDir = new File(o.getPath());
			fill();
		} else if (CURRENT_DIR.equals(o.getName())) {
			handleDirClicked(o);
		} else if (TYPE_FILE.equals(type)) {
			handleFileClicked(o);
		}
		super.onItemClick(parent, view, position, id);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return false;
	}

	private void returnResult(Option optionItem) {
		if (!getString(R.string.parent_directory).equals(optionItem.getData())) {
			Intent intent = new Intent();
			intent.putExtra(FileFolderPropertyEditor.VALUE, optionItem.getPath());
			setResult(RESULT_OK, intent);
			finish();
		}
	}

}