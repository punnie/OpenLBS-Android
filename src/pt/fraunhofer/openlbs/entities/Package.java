package pt.fraunhofer.openlbs.entities;

public class Package {
	private Integer id;
	private String name;
	private Integer version;
	private String updated_at;
	
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
	
}
