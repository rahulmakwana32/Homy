package com.spring.hcloud.main.model;

import java.util.Date;

 

 
public class MetaData {
	
 
	String filename,filetype,username,filestatus;
	Integer size;
	Date uploadeddate,deleteddate;
	public Date getDeleteddate() {
		return deleteddate;
	}
	public void setDeleteddate(Date deleteddate) {
		this.deleteddate = deleteddate;
	}
	Boolean isFile,active;
 
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getFiletype() {
		return filetype;
	}
	public void setFiletype(String filetype) {
		this.filetype = filetype;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getFilestatus() {
		return filestatus;
	}
	public void setFilestatus(String filestatus) {
		this.filestatus = filestatus;
	}
	public Integer getSize() {
		return size;
	}
	public void setSize(Integer size) {
		this.size = size;
	}
	public Date getUploadeddate() {
		return uploadeddate;
	}
	public void setUploadeddate(Date uploadeddate) {
		this.uploadeddate = uploadeddate;
	}
	public Boolean getIsFile() {
		return isFile;
	}
	public void setIsFile(Boolean isFile) {
		this.isFile = isFile;
	}
	public MetaData(String filename, String filetype, String username, String filestatus, Integer size,
			Date uploadeddate, Date deleteddate, Boolean isFile, Boolean active) {
		super();
		this.filename = filename;
		this.filetype = filetype;
		this.username = username;
		this.filestatus = filestatus;
		this.size = size;
		this.uploadeddate = uploadeddate;
		this.deleteddate = deleteddate;
		this.isFile = isFile;
		this.active = active;
	}
	@Override
	public String toString() {
		return "MetaData [filename=" + filename + ", filetype=" + filetype + ", username=" + username + ", filestatus="
				+ filestatus + ", size=" + size + ", uploadeddate=" + uploadeddate + ", deleteddate=" + deleteddate
				+ ", isFile=" + isFile + ", active=" + active + "]";
	}

	 
 
	
	
	
	
	

}
