package com.climate.mirage.app;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.climate.mirage.Mirage;
import com.climate.mirage.targets.TextViewTarget;

public class TextViewActivity extends AppCompatActivity {

	private TextView textView;
    private int drawableSize;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        drawableSize = toPx(60);

        textView = new TextView(this);
        textView.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur et magna tincidunt, hendrerit massa vel, suscipit dui. Vestibulum feugiat nulla sed purus vehicula blandit.");
        textView.setCompoundDrawablePadding(toPx(10));
        Drawable drawable = getResources().getDrawable(R.drawable.mirage_ic_launcher);
        drawable.setBounds(0, 0, drawableSize, drawableSize);
        textView.setCompoundDrawables(drawable, null, null, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = lp.topMargin = lp.rightMargin = lp.bottomMargin = toPx(10);
        ll.addView(textView, lp);

        Button button = new Button(this);
        button.setText("Load Image");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Mirage.get(TextViewActivity.this)
                        .load(Images.PUPPY)
                        .into(textView)
                        .location(TextViewTarget.LEFT)
                        .bounds(0, 0, drawableSize, drawableSize)
                        .placeHolder(R.drawable.mirage_ic_launcher)
                        .error(R.drawable.ic_error)
                        .fade()
                        .go();
            }
        });
        ll.addView(button);
		setContentView(ll);
    }

    private int toPx(int dp) {
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, getResources().getDisplayMetrics());
    }

}