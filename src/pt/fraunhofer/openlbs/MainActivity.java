package pt.fraunhofer.openlbs;

import pt.fraunhofer.openlbs.zxing.CaptureActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
    
	private static final int CAPTURED = 1;
	private static final String TAG = "MainActivity";
	private String result;
	private Button capture, packages, preferences;
	
	public static final String BARCODE_RESULT = "BarcodeResult";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        capture = (Button) findViewById(R.id.Button01);
        packages = (Button) findViewById(R.id.Button02);
        preferences = (Button) findViewById(R.id.Button03);
        
        capture.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), CaptureActivity.class);
				startActivityForResult(i, CAPTURED);
			}
		});
        
        packages.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), ListPackagesActivity.class);
				startActivity(i);
			}
		});
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == CAPTURED){
			Bundle bundle = data.getExtras();
			result = bundle.getString("Result");
			Log.v(TAG, "onActivityResult: value of result: " + result);
			
			Bundle extras = new Bundle();
			extras.putString(MainActivity.BARCODE_RESULT, result);
			
			Intent intent = new Intent(getApplicationContext(), ShowLocationActivity.class);
			intent.putExtras(extras);
			
			startActivity(intent);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	    
}
