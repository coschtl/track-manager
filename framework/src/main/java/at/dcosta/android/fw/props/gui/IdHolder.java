package at.dcosta.android.fw.props.gui;

import java.io.File;

import at.dcosta.android.fw.R;
import at.dcosta.android.fw.gui.IconListIdHolder;
import at.dcosta.android.fw.props.Folder;

public abstract class IdHolder extends IconListIdHolder {

	private static final long serialVersionUID = 1L;
	private static final String KEY_CONFIG_XML = "configXml";

	public static final IdHolder DEFAULT = new IdHolder() {

		private static final long serialVersionUID = 1L;

		@Override
		public int getIcon(Class<?> type) {
			if (type == Folder.class) {
				return R.drawable.folder;
			} else if (type == File.class) {
				return R.drawable.file;
			}
			return R.drawable.settings;
		}
	};

	static {
		DEFAULT.setConfigurationXmlResource("at/dcosta/android/fw/props/availableProps.xml").setHeaderId(R.id.list_header).setLineBodyId(R.id.line_body)
				.setLineHeadId(R.id.line_header).setLineIconId(R.id.line_image).setListLayoutId(R.layout.property_list)
				.setListRowLayoutId(R.layout.property_list_line);
	}

	public String getConfigurationXmlResource() {
		return getStringExtra(KEY_CONFIG_XML);
	}

	public abstract int getIcon(Class<?> type);

	public IdHolder setConfigurationXmlResource(String value) {
		setExtra(KEY_CONFIG_XML, value);
		return this;
	}

}
