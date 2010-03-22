package pt.fraunhofer.openlbs;

import pt.fraunhofer.openlbs.zxing.CaptureActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
	private Button capture;
	private int dialogs;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        capture = (Button) findViewById(R.id.Button01);
        
        capture.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), CaptureActivity.class);
				startActivityForResult(i, CAPTURED);
				}
		});
        
        dialogs = 0;
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == CAPTURED){
			Bundle bundle = data.getExtras();
			result = bundle.getString("Result");
			Log.v(TAG, "onActivityResult: value of result: " + result);
			
			showDialog(dialogs++);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Log.v(TAG, "onCreateDialog: value of result: " + result);
		return new AlertDialog.Builder(this)
		.setIcon(R.drawable.icon)
		.setTitle(R.string.app_name)
		.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    /* User clicked OK so do some stuff */
                }
            })
		.setMessage(result)
		.create();
	}
    
}
