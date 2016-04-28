package at.dcosta.android.fw.props.gui;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;

public class TextPropertyEditor extends PropertyEditor {

	@Override
	protected boolean isValid() {
		if (getType() == String.class) {
			return true;
		}
		try {
			if (getType() == BigDecimal.class) {
				new BigDecimal(getValue());
				return true;
			} else if (getType() == BigInteger.class) {
				new BigInteger(getValue());
				return true;
			} else {
				// Class newInstance = getType().getClass().newInstance();
				Method method = getType().getMethod("valueOf", String.class);
				method.invoke(null, getValue());
				return true;
			}
		} catch (Exception e) {
			System.out.println("wrong format: " + e.getMessage());
		}
		return false;
	}

	@Override
	protected void onAddPropertyCall(Intent intent) {
		// nothing to do
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		value.setVisibility(View.VISIBLE);
		if (getType().getSuperclass() == Number.class) {
			value.setRawInputType(EditorInfo.TYPE_CLASS_NUMBER);
		}
	}

}
