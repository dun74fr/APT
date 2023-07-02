package fr.areastudio.jwterritorio.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

import fr.areastudio.jwterritorio.R;


public class FullScreenViewActivity extends Activity {

	private FullScreenImageAdapter adapter;
	private ViewPager viewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fullscreen_img_view);

		viewPager = findViewById(R.id.pager);


		Intent i = getIntent();
		String image = i.getStringExtra("image");
		List<String> urls = new ArrayList<>();
		urls.add(image);
		adapter = new FullScreenImageAdapter(FullScreenViewActivity.this,
				urls);

		viewPager.setAdapter(adapter);

		// displaying selected image first
		viewPager.setCurrentItem(0);
	}
}
