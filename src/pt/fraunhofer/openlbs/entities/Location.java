package pt.fraunhofer.openlbs.entities;

import java.io.Serializable;
import java.util.Vector;

public class Location implements Serializable {
	private static final long serialVersionUID = -8405292098457422779L;
	
	private String name;
	private String coordinates;
	private String tags;
	private Vector<Content> contents;
	
	public Location() {
		contents = new Vector<Content>();
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
