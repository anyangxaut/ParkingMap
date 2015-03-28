package name.bagi.levente.pedometer.preferences;

import android.content.Context;
import android.util.AttributeSet;

import com.example.departmentmap.R;

public class StepLengthPreference extends EditMeasurementPreference {
	
	public StepLengthPreference(Context context) {
		super(context);
	}
	public StepLengthPreference(Context context, AttributeSet attr) {
		super(context, attr);
	}
	public StepLengthPreference(Context context, AttributeSet attr, int defStyle) {
		super(context, attr, defStyle);
	}

	protected void initPreferenceDetails() {
		mTitleResource = R.string.step_length_setting_title;
		mMetricUnitsResource = R.string.centimeters;
	}
}

