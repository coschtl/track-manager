package at.dcosta.android.fw.oldprops;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class PropertyEditor extends Activity implements OnClickListener {

    public static final String NAME_IS_READONLY = "nameIsReadonly";

    private String type;
    private int status;
    private long id;
    private EditText nameView, valueView;

    @Override
    public void onClick(View v) {
        Bundle bundle = new Bundle();
        bundle.putString(PropertyDbAdapter.DB.COL_TYPE, type);
        bundle.putString(PropertyDbAdapter.DB.COL_NAME, nameView.getText().toString());
        bundle.putString(PropertyDbAdapter.DB.COL_VALUE, valueView.getText().toString());
        bundle.putLong(PropertyDbAdapter.DB.COL_ID, id);
        bundle.putInt(PropertyDbAdapter.DB.COL_STATUS, status);

        Intent mIntent = new Intent();
        mIntent.putExtras(bundle);
        setResult(RESULT_OK, mIntent);
        finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        String err = "PropertyEditor needs the following extra: " + PropertyIds.class.getName() + "!";
        if (extras == null) {
            throw new IllegalArgumentException(err);
        }

        Object idObject = extras.get(PropertyIds.class.getName());
        if (!(idObject instanceof PropertyIds)) {
            throw new IllegalArgumentException(err);
        }
        PropertyIds ids = (PropertyIds) idObject;

        setContentView(ids.getEditView());
        nameView = (EditText) findViewById(ids.getEditTextName());
        valueView = (EditText) findViewById(ids.getEditTextValue());

        id = extras.getLong(PropertyDbAdapter.DB.COL_ID);
        type = extras.getString(PropertyDbAdapter.DB.COL_TYPE);
        String name = extras.getString(PropertyDbAdapter.DB.COL_NAME);
        if (name != null) {
            nameView.setText(name);
            if (extras.getBoolean(NAME_IS_READONLY)) {
                nameView.setEnabled(false);
            }
        }
        String value = extras.getString(PropertyDbAdapter.DB.COL_VALUE);
        if (value != null) {
            valueView.setText(value);
        }
        status = extras.getInt(PropertyDbAdapter.DB.COL_STATUS);

        Button confirmButton = (Button) findViewById(ids.getConfirmButtonId());
        confirmButton.setOnClickListener(this);

        Button cancelButton = (Button) findViewById(ids.getCancelButtonId());
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent();
                setResult(RESULT_CANCELED, mIntent);
                finish();
            }
        });

    }

}