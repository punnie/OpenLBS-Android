package pt.fraunhofer.openlbs;

import java.util.ArrayList;

import pt.fraunhofer.openlbs.entities.Package;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PackageListItemAdapter extends ArrayAdapter<Package> {
	private static final String TAG = "PackageListItemAdapter";
	
	private ArrayList<Package> packages;
	private Context context;

	public PackageListItemAdapter(Context context, int textViewResourceId, ArrayList<Package> packages) {
		super(context, textViewResourceId, packages);
		this.packages = packages;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.d(TAG, "Entered getView: " + position);
		View v = convertView;
		
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.packages_row, null);
		}
		
		Package p = packages.get(position);
		
		if (p != null) {
			TextView packageName = (TextView) v.findViewById(R.id.listPackageName);
			TextView packageVersion = (TextView) v.findViewById(R.id.listPackageVersion);
			if (packageName != null) {
				packageName.setText(p.getName());
			}
			if (packageVersion != null){
				packageVersion.setText("Going on version " + p.getVersion().toString());
			}
		}
		return v;
	}


}
