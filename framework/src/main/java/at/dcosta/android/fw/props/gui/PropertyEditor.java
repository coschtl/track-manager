package at.dcosta.android.fw.props.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Date;
import java.util.Iterator;

import at.dcosta.android.fw.R;
import at.dcosta.android.fw.props.Folder;
import at.dcosta.android.fw.props.Property;
import at.dcosta.android.fw.props.PropertyConfiguration;
import at.dcosta.android.fw.props.PropertyDbAdapter;
import at.dcosta.android.fw.props.ValueCollection;
import at.dcosta.android.fw.util.BundleUtil;

public abstract class PropertyEditor extends Activity implements OnClickListener, TextWatcher {

    public static final int REQUEST_CODE = PropertyEditor.class.hashCode() > 0 ? PropertyEditor.class.hashCode() : -1 * PropertyEditor.class.hashCode();
    public static final String ARG_PROPERTY_CONFIGURATION = "propertyConfiguration";
    public static final String ARG_PROPERTY_ID = "propertyId";

    private static final int ID_ADD = 100;
    private static final int ID_DELETE = 101;
    private static final int ID_CANCEL = -1;
    private static final int ID_ACCEPT = 1;
    protected PropertyDbAdapter propertyDbAdapter;
    protected Property property;
    protected EditText value;
    protected PropertyConfiguration propertyConfiguration;
    private ImageView add, delete;

    public static Class<?> getImplementation(Property property) {
        Class<?> type = property.getType();
        if (type == Boolean.class) {
            return BooleanPropertyEditor.class;
        } else if (type == ValueCollection.class) {
            return ValueCollectionPropertyEditor.class;
        } else if (type == Folder.class || type == File.class) {
            return FileFolderPropertyEditor.class;
        } else if (type == Date.class || type == java.sql.Date.class) {
            return DatePropertyEditor.class;
        }
        return TextPropertyEditor.class;

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (add.getVisibility() != View.GONE) {
            add.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // not needed
    }

    protected Class<?> getType() {
        return property.getType();
    }

    protected String getValue() {
        return value.getText().toString();
    }

    protected abstract boolean isValid();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("onActivityResult" + requestCode + ", " + resultCode + ", " + data);
        if (requestCode == PropertyEditor.REQUEST_CODE) {
            setResult(resultCode);
            finish();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected abstract void onAddPropertyCall(Intent indent);

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case ID_ACCEPT:
                if (isValid()) {
                    property.setValue(getValue());
                    propertyDbAdapter.updateProperty(property);
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String msg = getValue() + " is not valid for " + getType().getSimpleName();
                    Toast notValidToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
                    notValidToast.show();
                }
                break;
            case ID_ADD:
                Property newProp = new Property(property);
                newProp.setValue(propertyConfiguration.getByName(property.getName()).getDefaultValue());
                long newPropId = propertyDbAdapter.createPropertyEntry(newProp);
                Intent i = new Intent(this, getClass());
                i.putExtra(PropertyEditor.ARG_PROPERTY_CONFIGURATION, propertyConfiguration);
                i.putExtra(PropertyEditor.ARG_PROPERTY_ID, newPropId);
                startActivityForResult(i, PropertyEditor.REQUEST_CODE);
                setResult(RESULT_OK);
                finish();
                break;
            case ID_DELETE:
                propertyDbAdapter.deleteProperty(property.getId());
                setResult(RESULT_OK);
                finish();
                break;
            case ID_CANCEL:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        propertyConfiguration = BundleUtil.get(ARG_PROPERTY_CONFIGURATION, extras);
        propertyDbAdapter = new PropertyDbAdapter(this, propertyConfiguration);

        long propertyId = BundleUtil.getLong(ARG_PROPERTY_ID, -1, extras);
        property = propertyDbAdapter.fetchProperty(propertyId);
        System.out.println("PROPERTY has type: " + getType().getName());

        setContentView(R.layout.property_edit);
        ((TextView) findViewById(R.id.property_name)).setText(property.getDisplayName());
        value = (EditText) findViewById(R.id.property_value);
        value.setText(property.getValue());
        value.addTextChangedListener(this);
        if (property.getHelpText() != null) {
            TextView help = (TextView) findViewById(R.id.help_text);
            help.setText(property.getHelpText());
            help.setVisibility(View.VISIBLE);
        }
        add = (ImageView) findViewById(R.id.but_add);
        delete = (ImageView) findViewById(R.id.but_delete);
        // ImageView removeMultivalue = (ImageView) findViewById(R.id.but_remove_multivalue);
        if (property.isMultivalue()) {
            add.setId(ID_ADD);
            LinearLayout row = (LinearLayout) findViewById(R.id.row);
            row.setOnCreateContextMenuListener(this);
            add.setOnClickListener(this);

            Iterator<Property> all = propertyDbAdapter.fetchAllProperties(property.getName());
            int cnt = 0;
            while (all.hasNext()) {
                all.next();
                cnt++;
            }
            if (cnt > 1) {
                delete.setId(ID_DELETE);
                delete.setOnClickListener(this);
            }
        } else {
            add.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
            // removeMultivalue.setVisibility(View.GONE);
        }
        ImageView cancel = (ImageView) findViewById(R.id.but_cancel);
        cancel.setId(ID_CANCEL);
        cancel.setOnClickListener(this);
        ImageView accept = (ImageView) findViewById(R.id.but_accept);
        accept.setId(ID_ACCEPT);
        accept.setOnClickListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.row) {
            System.out.println("hallo welt");
            return;
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    protected void onPause() {
        propertyDbAdapter.close();
        super.onPause();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // not needed
    }

}
