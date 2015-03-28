package name.bagi.levente.pedometer.preferences;

import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

abstract public class EditMeasurementPreference extends EditTextPreference {
	
	protected int mTitleResource;
	protected int mMetricUnitsResource;
	
	public EditMeasurementPreference(Context context) {
		super(context);
		initPreferenceDetails();
	}
	public EditMeasurementPreference(Context context, AttributeSet attr) {
		super(context, attr);
		initPreferenceDetails();
	}
	public EditMeasurementPreference(Context context, AttributeSet attr, int defStyle) {
		super(context, attr, defStyle);
		initPreferenceDetails();
	}
	
	abstract protected void initPreferenceDetails();
	
	// 步长设置：单位 cm；默认20cm
	protected void showDialog(Bundle state) {
		setDialogTitle(
				getContext().getString(mTitleResource) + 
				" (" + getContext().getString(mMetricUnitsResource) + ")"
		);
		
		try {
			Float.valueOf(getText());
		}
		catch (Exception e) {
			setText("20");
		}
		
		super.showDialog(state);
	}
	protected void onAddEditTextToDialogView (View dialogView, EditText editText) {
		editText.setRawInputType(
				InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		super.onAddEditTextToDialogView(dialogView, editText);
	}
	public void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			try {
				Float.valueOf(((CharSequence)(getEditText().getText())).toString());
			}
			catch (NumberFormatException e) {
				this.showDialog(null);
				return;
			}
		}
		super.onDialogClosed(positiveResult);
	}
}
