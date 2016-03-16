package it.smartcommunitylab.carpooling.model;

public class UserInfo {

	private String displayName;
	private String telephone;
	private Auto auto;

	public UserInfo() {
	}

	public UserInfo(String displayName, String telephone, Auto auto) {
		super();
		this.displayName = displayName;
		this.telephone = telephone;
		this.auto = auto;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public Auto getAuto() {
		return auto;
	}

	public void setAuto(Auto auto) {
		this.auto = auto;
	}

}
