package pt.fraunhofer.openlbs.entities;

import java.io.Serializable;
import java.util.Vector;

public class Location implements Serializable {

	private static final long serialVersionUID = -8405292098457422779L;
	
	/**
	 * DBAdapter stuff
	 */
	
	public static String TABLE_NAME = "locations";
    public static String ID = "_id";
    public static String NAME = "name";
    public static String COORDINATES = "coordinates";
    public static String TAGS = "tags";
    public static String PACKAGE_ID = "package_id";
    public static String[] COLUMNS = { ID, NAME, COORDINATES, TAGS, PACKAGE_ID };
	
    private Integer id;
	private String name;
	private String coordinates;
	private String tags;
	private Vector<Content> contents;
	
	public Location() {
		contents = new Vector<Content>();
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
	
	public String getCoordinates() {
		return coordinates;
	}
	
	public void setCoordinates(String coordinates) {
		this.coordinates = coordinates;
	}
	
	public String getTags() {
		return tags;
	}
	
	public void setTags(String tags) {
		this.tags = tags;
	}
	
	public Vector<Content> getContents() {
		return contents;
	}
	
	public void setContents(Vector<Content> contents) {
		this.contents = contents;
	}
	
	public void addContent(Content content) {
		this.contents.add(content);
	}

	
}
