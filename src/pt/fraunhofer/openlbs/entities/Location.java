package pt.fraunhofer.openlbs.entities;

import java.util.ArrayList;

public class Location {
	private String name;
	private String coordinates;
	private String tags;
	private ArrayList<Content> contents;
	
	public Location() {
		contents = new ArrayList<Content>();
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
	
	public ArrayList<Content> getContents() {
		return contents;
	}
	
	public void setContents(ArrayList<Content> contents) {
		this.contents = contents;
	}
	
	public void addContent(Content content) {
		this.contents.add(content);
	}
	
}
