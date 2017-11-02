package de.bjusystems.vdrmanager.data;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import de.bjusystems.vdrmanager.R;

public class FetchEditTextPreference extends DialogPreference{

	private EditText mEditText;

	private String initialValue;

	public String getInitialValue() {
		return initialValue;
	}

	public void setInitialValue(String initialValue) {
		this.initialValue = initialValue;
	}

	public EditText getmEditText() {
		return mEditText;
	}

	public void setEditText(String text) {
		this.mEditText.setText(text);
	}

	private ImageButton mButton;

	private String mText;
	//private CharSequence mCompoundButtonText;
	private View.OnClickListener mCompoundButtonCallback;

	public FetchEditTextPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		setDialogLayoutResource(R.layout.fetch_preference);
	}

	public FetchEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.fetch_preference);
	}

	@Override
	protected View onCreateDialogView() {
		View root = super.onCreateDialogView();
		mEditText = (EditText) root.findViewById(R.id.edit);
		mButton = (ImageButton) root.findViewById(R.id.button);
		return root;
	}

	public void setText(String text) {
		mText = text;
	}

	public String getText() {
		return mText;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE: // User clicked OK!
			mText = mEditText.getText().toString();
			callChangeListener(mText);
			Editor editor = (Editor) getEditor();
	        editor.putString(getKey(), mText);
	        editor.commit();
			break;
		}
		super.onClick(dialog, which);
	}

	@Override
	protected void onBindDialogView(View view) {
		mEditText.setText(mText);
		//mButton.setText(mCompoundButtonText);

		// Set a callback to our button.
		mButton.setOnClickListener(mCompoundButtonCallback);
	}

	public void setCompoundButtonListener(View.OnClickListener callback) {
		mCompoundButtonCallback = callback;
	}
}