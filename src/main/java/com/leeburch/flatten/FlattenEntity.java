package com.leeburch.flatten;

import java.io.File;
import java.nio.file.Path;
import java.util.Date;

class FlattenEntity implements Comparable<FlattenEntity> { 
	String name;
	String alias;
	Date creationDate;
	
	public FlattenEntity(Path path, Date creationDate) {
		super();
		this.name = path.toAbsolutePath().toString();
		this.creationDate = creationDate;
	}

	public FlattenEntity(String name, Date creationDate) {
		super();
		this.name = name;
		this.creationDate = creationDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlias() {
		return alias;
	}
	
	public String getLastPathElement() {
		int lastSep = name.lastIndexOf(File.separator);
		return name.substring(lastSep + 1);
	}
	
	public String getPath() {
		int lastSep = name.lastIndexOf(File.separator);
		return name.substring(0, lastSep);
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alias == null) ? 0 : alias.hashCode());
		result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		FlattenEntity other = (FlattenEntity) obj;
		if (alias == null) {
			if (other.alias != null)
				return false;
		} else if (!alias.equals(other.alias))
			return false;
		if (creationDate == null) {
			if (other.creationDate != null)
				return false;
		} else if (!creationDate.equals(other.creationDate))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public int compareTo(FlattenEntity o) {
		return this.name.compareTo(o.name);
	}
	
}