package com.ssm.luaming.util;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class LuamingImageButton extends ImageView {

	public LuamingImageButton( Context context, AttributeSet attrs ) {
		super( context, attrs );
	}

	@Override
	public boolean onTouchEvent( MotionEvent event ) {
		super.onTouchEvent( event );
		switch( event.getAction() )
		{
		case MotionEvent.ACTION_DOWN:
			setColorFilter( Color.argb( 128, 0, 0, 0 ) );

			return true;
		case MotionEvent.ACTION_MOVE:
			float x = event.getX();
			float y = event.getY();
			if ( x < 0 || x > this.getWidth() || y < 0 || y > this.getHeight() )
			{
				setColorFilter( null );
				return false;
			}
			return true;
		case MotionEvent.ACTION_UP:
			setColorFilter( null );
			return true;
		default:
			break;
		}
		return false;
	}
}
