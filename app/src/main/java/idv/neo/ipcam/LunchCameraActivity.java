package idv.neo.ipcam;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LunchCameraActivity extends Activity {
	private Button button;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		addListenerOnButton();
	}

	public void addListenerOnButton() {
		final Context context = this;
		button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(context, MainActivity.class);

				Bundle sendBundle = new Bundle();
				sendBundle.putString("mediaUrl", "http://192.168.99.103/video/ACVS-H264.cgi");
//				sendBundle.putString("mediaUrl", "");
				sendBundle.putString("userName", "admin");
				sendBundle.putString("password", "");

				intent.putExtras(sendBundle);
				startActivity(intent);
			}
		});
	}
}
