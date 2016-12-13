package com.test.app;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

/**
 * A {@link android.preference.Preference} that displays a number picker as a
 * dialog.
 */
public class NumberPickerPreference extends DialogPreference
{

	// allowed Values
	public static final int DEFAULTVALUE = 24;
	public static final int MIN_VALUE = 12;
	public static final int MAX_VALUE = 1440;
	public static final String[] NUMBERS = { "12", "15", "18", "20", "24",
			"30", "40", "50", "60", "70", "80", "90", "100", "144", "288",
			"480", "720", "1440" };
	// enable or disable the 'circular behavior'
	public static final boolean WRAP_SELECTOR_WHEEL = true;

	private NumberPicker picker;
	private int value;

	// Method to get Values from the Number Picker index Position
	public static int getValueOfPicker(int position)
	{
		switch (position)
		{
		case 0:
			return 12;
		case 1:
			return 15;
		case 2:
			return 18;
		case 3:
			return 20;
		case 4:
			return 24;
		case 5:
			return 30;
		case 6:
			return 40;
		case 7:
			return 50;
		case 8:
			return 60;
		case 9:
			return 70;
		case 10:
			return 80;
		case 11:
			return 90;
		case 12:
			return 100;
		case 13:
			return 144;
		case 14:
			return 288;
		case 15:
			return 480;
		case 16:
			return 720;
		case 17:
			return 1440;
		default:
			return 24;
		}
	}

	public NumberPickerPreference(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public NumberPickerPreference(Context context, AttributeSet attrs,
			int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected View onCreateDialogView()
	{
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER;

		picker = new NumberPicker(getContext());
		picker.setLayoutParams(layoutParams);

		FrameLayout dialogView = new FrameLayout(getContext());
		dialogView.addView(picker);

		return dialogView;
	}

	@Override
	protected void onBindDialogView(View view)
	{
		super.onBindDialogView(view);
		picker.setMinValue(0);
		picker.setMaxValue(NUMBERS.length - 1);
		picker.setDisplayedValues(NUMBERS);
		picker.setWrapSelectorWheel(WRAP_SELECTOR_WHEEL);
		picker.setValue(getValue());
	}

	@Override
	protected void onDialogClosed(boolean positiveResult)
	{
		if (positiveResult)
		{
			picker.clearFocus();
			int newValue = picker.getValue();
			if (callChangeListener(newValue))
			{
				setValue(newValue);
			}
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		return a.getInt(index, DEFAULTVALUE);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue,
			Object defaultValue)
	{
		setValue(restorePersistedValue ? getPersistedInt(DEFAULTVALUE)
				: (Integer) defaultValue);
	}

	public void setValue(int value)
	{
		this.value = value;
		persistInt(this.value);
	}

	public int getValue()
	{
		return this.value;
	}
}
