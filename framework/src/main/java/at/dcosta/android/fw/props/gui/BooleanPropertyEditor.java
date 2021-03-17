package at.dcosta.android.fw.props.gui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import at.dcosta.android.fw.R;

public class BooleanPropertyEditor extends PropertyEditor implements OnCheckedChangeListener {

    @Override
    protected boolean isValid() {
        return true;
    }

    @Override
    protected void onAddPropertyCall(Intent intent) {
        // nothing to do
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        value.setText(Boolean.toString(isChecked));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CheckBox checkbox = (CheckBox) findViewById(R.id.onOff);
        checkbox.setVisibility(View.VISIBLE);
        checkbox.setChecked(property.getBooleanValue(false));
        checkbox.setOnCheckedChangeListener(this);
    }

}
