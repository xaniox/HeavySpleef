package de.matzefratze123.heavyspleef.core.uuid;

import java.util.UUID;

public class GameProfile {
	
	private UUID uniqueIdentifier;
	private String name;
	
	public GameProfile(UUID uniqueIdentifier, String name) {
		this.uniqueIdentifier = uniqueIdentifier;
		this.name = name;
	}
	
	public UUID getUniqueIdentifier() {
		return uniqueIdentifier;
	}
	
	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((uniqueIdentifier == null) ? 0 : uniqueIdentifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GameProfile other = (GameProfile) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (uniqueIdentifier == null) {
			if (other.uniqueIdentifier != null)
				return false;
		} else if (!uniqueIdentifier.equals(other.uniqueIdentifier))
			return false;
		return true;
	}
	
}
