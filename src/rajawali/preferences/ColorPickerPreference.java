package rajawali.preferences;

import android.content.*;
import android.graphics.Color;
import android.preference.*;
import android.util.*;
import android.view.*;
import android.widget.EditText;

/**
 * A {@link Preference} that allows for string
 * input.
 * <p>
 * It is a subclass of {@link DialogPreference} and shows the {@link EditText}
 * in a dialog. This {@link EditText} can be modified either programmatically
 * via {@link #getEditText()}, or through XML by setting any EditText
 * attributes on the EditTextPreference.
 * <p>
 * This preference will store a string into the SharedPreferences.
 * <p>
 */
public class ColorPickerPreference extends DialogPreference {
    /**
     * The edit text shown in the dialog.
     */
    
	ColorPickerView mPickerView;
	int mColor;
	
    public ColorPickerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public ColorPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

	private void init(Context context, AttributeSet attrs) {
		// Don't create view here
	}

    public ColorPickerPreference(Context context) {
        this(context, null);
    }
   
    
    @Override
    protected View onCreateDialogView() {
		mPickerView = new ColorPickerView(getContext(),null);
		mPickerView.setColor(mColor);
        mPickerView.setEnabled(true);
    	return mPickerView;
    }
    
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue,
    		Object defaultValue) {
    	if(defaultValue instanceof Integer){
    	   Integer intDefaultValue = (Integer)defaultValue;
            setColor(restorePersistedValue ? getPersistedInt(mColor) : intDefaultValue);
    	}
    	else {
            setColor(restorePersistedValue ? getPersistedInt(mColor) : Color.WHITE);
    	}
    }

    private void setColor(int color) {
    	mColor = color;
	}

	@Override
    protected void onDialogClosed(boolean positiveResult) {
    	super.onDialogClosed(positiveResult);
        if (positiveResult) {
            Integer value = new Integer(mPickerView.getColor());
            if (callChangeListener(value)) {
                storeColor(value);
            }
        }
        ViewParent vp = mPickerView.getParent();
        Log.d("",vp.toString());

    }
   
    
    /**
     * Saves the color to the {@link SharedPreferences}.
     * 
     * @param color Color to save
     */
    public void storeColor(int color) {
    	mColor = color;
        final boolean wasBlocking = shouldDisableDependents();
        persistInt(color);
        final boolean isBlocking = shouldDisableDependents(); 
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }

    }

}