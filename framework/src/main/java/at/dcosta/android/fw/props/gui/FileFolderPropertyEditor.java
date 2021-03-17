package at.dcosta.android.fw.props.gui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.io.File;

import at.dcosta.android.fw.props.Folder;

public class FileFolderPropertyEditor extends PropertyEditor {

    public static final String VALUE = "value";
    public static final int REQUEST_CODE = FileFolderPropertyEditor.class.hashCode() > 0 ? FileFolderPropertyEditor.class.hashCode() : -1
            * FileFolderPropertyEditor.class.hashCode();

    private IdHolder idHolder;

    @Override
    protected boolean isValid() {
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_CODE == requestCode && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                String stringValue = extras.getString(VALUE);
                value.setText(stringValue == null ? "" : stringValue);
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onAddPropertyCall(Intent intent) {
        intent.putExtra(IdHolder.class.getName(), idHolder);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(value)) {
            Intent i = new Intent(this, FileChooser.class);
            System.out.println("type: " + getType());
            System.out.println("isfolder: " + getType().isInstance(Folder.class));
            System.out.println("isFile: " + getType().isInstance(File.class));
            i.putExtra(FileChooser.TYPE, getType() == Folder.class ? FileChooser.TYPE_FOLDER : FileChooser.TYPE_FILE);
            i.putExtra(IdHolder.class.getName(), idHolder);
            startActivityForResult(i, REQUEST_CODE);
        } else {
            super.onClick(v);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("FILEFOLDER");
        super.onCreate(savedInstanceState);
        idHolder = (IdHolder) getIntent().getExtras().get(IdHolder.class.getName());
        value.setVisibility(View.VISIBLE);
        value.setOnClickListener(this);
    }

}
