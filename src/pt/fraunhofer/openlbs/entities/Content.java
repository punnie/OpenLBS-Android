package pt.fraunhofer.openlbs.entities;

import java.io.Serializable;


public class Content implements Serializable {
	private static final long serialVersionUID = 3132864411035084381L;
	
	private String name;
	private String tags;
	private String path;
	
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
}
