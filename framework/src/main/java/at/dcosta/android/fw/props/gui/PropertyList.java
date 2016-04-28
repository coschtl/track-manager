package at.dcosta.android.fw.props.gui;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import at.dcosta.android.fw.IOUtil;
import at.dcosta.android.fw.R;
import at.dcosta.android.fw.gui.IconListActivity;
import at.dcosta.android.fw.gui.IconListBean;
import at.dcosta.android.fw.props.Property;
import at.dcosta.android.fw.props.PropertyConfiguration;
import at.dcosta.android.fw.props.PropertyDbAdapter;

public class PropertyList extends IconListActivity {

	private static final class IconListAdapter extends IconListBean {
		private static final long serialVersionUID = 1L;
		private final Property property;

		public IconListAdapter(Property property, IdHolder idHolder) {
			super(property.getId(), property.getDisplayName(), property.getValue(), idHolder.getIcon(property.getType()));
			this.property = property;
		}

		public Property getProperty() {
			return property;
		}

	}

	public static final int REQUEST_CODE = PropertyList.class.hashCode() > 0 ? PropertyList.class.hashCode() : -1 * PropertyList.class.hashCode();
	private static final int CONTEXT_DELETE = 101;

	private PropertyDbAdapter propertyDbAdapter;
	private List<IconListAdapter> listEntries;
	private PropertyConfiguration propertyConfiguration;

	private Toast helpToast;

	public PropertyList() {
		this(IdHolder.DEFAULT);
	}

	public PropertyList(IdHolder idHolder) {
		super(idHolder);
	}

	@Override
	public List<? extends IconListBean> getBeans() {
		return listEntries;
	}

	@Override
	public String getHeadline() {
		return getString(R.string.header_Preferencies);
	}

	private int getPropertyCount() {
		Iterator<Property> all = propertyDbAdapter.fetchAllProperties();
		int cnt = 0;
		while (all.hasNext()) {
			all.next();
			cnt++;
		}
		return cnt;
	}

	private IdHolder idHolder() {
		return (IdHolder) idHolder;
	}

	private void loadAllPropertiesFromDb() {
		listEntries.clear();
		Iterator<Property> it = propertyDbAdapter.fetchAllProperties();
		while (it.hasNext()) {
			Property property = it.next();
			listEntries.add(new IconListAdapter(property, idHolder()));
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			loadAllPropertiesFromDb();
			updateView();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		IdHolder idHolderExtra = (IdHolder) getIntent().getSerializableExtra(IdHolder.class.getName());
		if (idHolderExtra != null) {
			setIdHolder(idHolderExtra);
		}
		InputStream is = getClass().getClassLoader().getResourceAsStream(idHolder().getConfigurationXmlResource());
		propertyConfiguration = new PropertyConfiguration(is);
		IOUtil.close(is);

		propertyDbAdapter = new PropertyDbAdapter(this, propertyConfiguration);
		listEntries = new ArrayList<IconListAdapter>();
		loadAllPropertiesFromDb();
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Property property = listEntries.get(position).getProperty();
		if (property.getAccess().isEditable()) {
			updateHelpToast(null);
			Intent i = new Intent(this, PropertyEditor.getImplementation(property));
			i.putExtra(PropertyEditor.ARG_PROPERTY_CONFIGURATION, propertyConfiguration);
			i.putExtra(PropertyEditor.ARG_PROPERTY_ID, property.getId());
			i.putExtra(IdHolder.class.getName(), idHolder);
			startActivityForResult(i, REQUEST_CODE);
		} else {
			updateHelpToast(property.getHelpText());
		}
	}

	@Override
	protected void onPause() {
		propertyDbAdapter.close();
		super.onPause();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(IdHolder.class.getName(), idHolder);
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return false;
	}

	private void updateHelpToast(String message) {
		if (helpToast == null) {
			if (message != null) {
				helpToast = Toast.makeText(this, message, Toast.LENGTH_LONG);
				helpToast.show();
			}
			return;
		}
		if (message == null) {
			helpToast.cancel();
			return;
		} else {
			helpToast.setText(message);
		}
		helpToast.show();
	}

}
