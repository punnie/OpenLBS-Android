package pt.fraunhofer.openlbs;

import java.util.Vector;

import pt.fraunhofer.openlbs.entities.Location;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class LocationListItemAdapter extends ArrayAdapter<Location> {
	private static final String TAG = "LocationListItemAdapter";
	
	private Vector<Location> locations;
	private Context context;

	public LocationListItemAdapter(Context context, int textViewResourceId, Vector<Location> locations) {
		super(context, textViewResourceId, locations);
		this.locations = locations;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.d(TAG, "Entered getView: " + position);
		View v = convertView;
		
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.locations_row, null);
		}
		
		Location l = locations.get(position);
		
		if (l != null) {
			TextView packageName = (TextView) v.findViewById(R.id.listLocationName);
			TextView packageVersion = (TextView) v.findViewById(R.id.listLocationTags);
			if (packageName != null) {
				packageName.setText(l.getName());
			}
			if (packageVersion != null){
				packageVersion.setText(l.getTags());
			}
		}
		return v;
	}
}
