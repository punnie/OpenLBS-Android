package pt.fraunhofer.openlbs.entities;

import java.io.Serializable;


public class Content implements Serializable {
	private static final long serialVersionUID = 3132864411035084381L;
	
	/**
	 * DBAdapter stuff
	 */
	
	public static String TABLE_NAME = "contents";
    public static String ID = "_id";
    public static String NAME = "name";
    public static String PATH = "path";
    public static String TYPE = "mimetype";
    public static String LOCATION_ID = "location_id";
    public static String[] COLUMNS = { ID, NAME, PATH, TYPE, LOCATION_ID };
	
    private int id;
	private String name;
	private String tags;
	private String path;
	private String mimetype;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getTags() {
		return tags;
	}
	
	public void setTags(String tags) {
		this.tags = tags;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}

	public String getMimetype() {
		return mimetype;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}
}
