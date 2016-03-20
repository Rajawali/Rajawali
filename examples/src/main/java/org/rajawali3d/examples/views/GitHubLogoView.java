package org.rajawali3d.examples.views;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import org.rajawali3d.examples.R;


public class GitHubLogoView extends View {

	protected static final ArgbEvaluator ARGB_EVAL = new ArgbEvaluator();

	protected Canvas mBackgroundCanvas;
	protected Bitmap mBackground;
	protected Bitmap mMask;
	protected Paint mPaint;
	protected Rect mDrawingRect;
	protected boolean mDirection;
	protected long mLastChange;
	protected int mDuration;

	public GitHubLogoView(Context context) {
		this(context, null);
	}

	public GitHubLogoView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GitHubLogoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mDuration = 2000;

		mMask = BitmapFactory.decodeResource(getResources(),
				R.drawable.githublogo);

		mPaint = new Paint();
		mPaint.setColor(0xffff0000);
		mPaint.setAntiAlias(true);

		mBackgroundCanvas = new Canvas();
	}


	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		mDrawingRect = new Rect(0, 0, w, h);

		final Bitmap mask = convertToAlphaMask(Bitmap.createScaledBitmap(mMask,
				w, h, false));
		mPaint.setShader(new BitmapShader(mask, Shader.TileMode.CLAMP,
				Shader.TileMode.CLAMP));

		mBackground = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		mBackgroundCanvas.setBitmap(mBackground);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		final long time = SystemClock.uptimeMillis();
		final int diff = (int) (time - mLastChange);
		if (time - mLastChange > mDuration) {
			mDirection = !mDirection;
			mLastChange = time;
		}

		final float percent = (mDirection ? diff % mDuration : mDuration
				- (diff % mDuration))
				/ (float) mDuration;

		mPaint.setColor((Integer) ARGB_EVAL.evaluate(percent, 0xffffffff,
				0xffff8800));

		// Draw the masked logo in the new color
		mBackgroundCanvas.drawRect(mDrawingRect, mPaint);

		// Draw the new logo
		canvas.drawBitmap(mBackground, 0, 0, mPaint);

		invalidate();
	}

	protected static Bitmap convertToAlphaMask(Bitmap b) {
		final Bitmap a = Bitmap.createBitmap(b.getWidth(), b.getHeight(),
				Bitmap.Config.ALPHA_8);
		final Canvas c = new Canvas(a);
		c.drawBitmap(b, 0.0f, 0.0f, null);
		return a;
	}
}
