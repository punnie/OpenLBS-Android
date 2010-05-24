package pt.fraunhofer.openlbs.entities;

import java.util.ArrayList;

public class Package {
	private Integer id;
	private String name;
	private Integer version;
	private String updated_at;
	private String content_file_name;
	private String content_file_size;
	private ArrayList<Location> locations;
	
	public Package() {
		locations = new ArrayList<Location>();
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

	public ArrayList<Location> getLocations() {
		return locations;
	}

	public void setLocations(ArrayList<Location> locations) {
		this.locations = locations;
	}
	
	public void addLocation(Location location){
		this.locations.add(location);
	}
}
