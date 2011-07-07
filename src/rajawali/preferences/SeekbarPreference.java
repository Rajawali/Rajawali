package rajawali.preferences;


import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;

public class SeekbarPreference extends DialogPreference {
	private SeekBar mSeekBar;
	
	//private TextView mTextView;
	
	private int mValue;
	private int mTempValue;
	
	public SeekbarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setPersistent(true);
		//this.setDialogLayoutResource(R.layout.preference_cover_spacing);
	}
	/*
	private final OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			final int value = SeekbarPreference.this.mSeekBar.getProgress();
			
			SeekbarPreference.this.mTextView.setText(Integer.toString(value));
			
			SeekbarPreference.this.setValue(value);
			SeekbarPreference.this.mValue = value;
		}
	};
	*/
	@Override
	protected void onBindDialogView(final View view) {
		super.onBindDialogView(view);
		//mSeekBar = (SeekBar)view.findViewById(R.id.cover_spacing_seekbar);
		//mTextView = (TextView)view.findViewById(R.id.cover_spacing_value);
		//mSeekBar.setOnSeekBarChangeListener(this.mSeekBarChangeListener);
		mSeekBar.setProgress(this.mValue);
	}
	
	@Override
	protected Object onGetDefaultValue(final TypedArray a, final int index) {
		return a.getInt(index, 0);
	}
	
	@Override
	protected void onSetInitialValue(final boolean restore, final Object defaultValue) {
		final int value = this.getPersistedInt(defaultValue == null ? 0 : (Integer)defaultValue);
		this.mValue = value;
	}
	
	@Override
	protected void onDialogClosed(final boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		
		if(positiveResult) {
			this.mTempValue = this.mValue;
			if(this.callChangeListener(this.mTempValue)) {
				saveValue(mTempValue);
			}
		}
	}
	
	public void setValue(final int value) {
		this.mValue = value;
	}
	
	public void saveValue(final int value) {
		this.setValue(value);
		this.persistInt(value);
	}
}
