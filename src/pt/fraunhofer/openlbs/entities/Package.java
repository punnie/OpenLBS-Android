package pt.fraunhofer.openlbs.entities;

import java.io.Serializable;
import java.util.Vector;

public class Package implements Serializable {
	private static final long serialVersionUID = -8775446713415610220L;
	
	/**
	 * DBAdapter stuff
	 */
	
	public static String TABLE_NAME = "packages";
    public static String ID = "_id";
    public static String NAME = "name";
    public static String VERSION = "version";
    public static String[] COLUMNS = { ID, NAME, VERSION };
	
	private Integer id;
	private String name;
	private Integer version;
	private String updated_at;
	private String content_file_name;
	private String content_file_size;
	private Vector<Location> locations;
	
	public Package() {
		locations = new Vector<Location>();
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Integer getVersion() {
		return version;
	}
	
	public void setVersion(Integer version) {
		this.version = version;
	}
	
	public String getUpdated_at() {
		return updated_at;
	}
	
	public void setUpdated_at(String updatedAt) {
		updated_at = updatedAt;
	}

	public String getContent_file_name() {
		return content_file_name;
	}

	public void setContent_file_name(String contentFileName) {
		content_file_name = contentFileName;
	}

	public String getContent_file_size() {
		return content_file_size;
	}

	public void setContent_file_size(String contentFileSize) {
		content_file_size = contentFileSize;
	}

	public Vector<Location> getLocations() {
		return locations;
	}

	public void setLocations(Vector<Location> locations) {
		this.locations = locations;
	}
	
	public void addLocation(Location location){
		this.locations.add(location);
	}
}
