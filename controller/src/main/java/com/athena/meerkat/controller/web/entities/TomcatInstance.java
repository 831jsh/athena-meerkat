/* 
 * Athena Peacock Dolly - DataGrid based Clustering 
 * 
 * Copyright (C) 2014 Open Source Consulting, Inc. All rights reserved by Open Source Consulting, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * Revision History
 * Author			Date				Description
 * ---------------	----------------	------------
 * Bong-Jin Kwon	2015. 1. 9.		First Draft.
 */
package com.athena.meerkat.controller.web.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.athena.meerkat.controller.MeerkatConstants;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

/**
 * <pre>
 * 
 * </pre>
 * 
 * @author Tran Ho
 * @version 2.0
 */
@Entity
@Table(name = "tomcat_instance")
public class TomcatInstance implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "Id")
	private int Id;
	@Column(name = "name", nullable = false)
	private String name;
	@Column(name = "version")
	private String version;
	@Column(name = "state")
	private int state;
	@Column(name = "created_time")
	private Date createdTime;
	@Column(name = "create_user_id")
	private int createUserId;

	@OneToMany(mappedBy = "tomcatInstance", fetch = FetchType.LAZY)
	@JsonManagedReference
	private List<TomcatConfigFile> tomcatConfigFiles;

	@ManyToOne(fetch = FetchType.LAZY)
	// using this annotation to prevent Infinite recursion json mapping
	@JsonBackReference
	private Server server;

	@ManyToOne(fetch = FetchType.LAZY)
	// using this annotation to prevent Infinite recursion json mapping
	@JsonBackReference
	@JoinColumn(name = "domain_id")
	private TomcatDomain tomcatDomain;

	@ManyToOne(fetch = FetchType.LAZY)
	@JsonBackReference
	@JoinColumn(name = "network_interface_id")
	private NetworkInterface networkInterface;

	@OneToMany(mappedBy = "tomcatInstance", fetch = FetchType.LAZY)
	@JsonManagedReference
	private List<TomcatApplication> tomcatApplications;

	public TomcatInstance() {
	}

	public int getId() {
		return Id;
	}

	public void setId(int id) {
		this.Id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String instanceName) {
		this.name = instanceName;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server machine) {
		this.server = machine;
	}

	public TomcatDomain getTomcatDomain() {
		return tomcatDomain;
	}

	public void setTomcatDomain(TomcatDomain domain) {
		this.tomcatDomain = domain;
	}

	// public Collection<TomcatApplication> getTomcatApplications() {
	// return tomcatApplications;
	// }
	//
	// public void setTomcatApplications(List<TomcatApplication> applications) {
	// this.tomcatApplications = applications;
	// }

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getHostName() {
		return server.getHostName();
	}

	public String getStatusString() {
		switch (this.state) {
		case MeerkatConstants.TOMCAT_STATUS_RUNNING:
			return "Running";
		case MeerkatConstants.TOMCAT_STATUS_SHUTDOWN:
			return "Stopped";
		default:
			return "Unknown";
		}
	}

	public String getOSName() {
		if (server != null) {
			return server.getOsName();
		}
		return "";
	}

	public String getJvm() {
		if (server != null) {
			return server.getJvmVersion();
		}
		return "";
	}

	public String getDomainName() {
		if (tomcatDomain != null) {
			return tomcatDomain.getName();
		}
		return "";
	}

	public int getMachineId() {
		if (server != null) {
			return server.getId();
		}
		return 0;
	}

	// public String getDomainStatus() {
	// if (tomcatDomain != null) {
	// return tomcatDomain.getIsClustering() == true ? "Clustering"
	// : "Non-clustering";
	// }
	// return "";
	// }

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	// public List<TomcatConfigFile> getTomcatConfigFiles() {
	// return tomcatConfigFiles;
	// }
	//
	// public void setTomcatConfigFiles(List<TomcatConfigFile>
	// tomcatConfigFiles) {
	// this.tomcatConfigFiles = tomcatConfigFiles;
	// }

	public int getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(int createUserId) {
		this.createUserId = createUserId;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public NetworkInterface getNetworkInterface() {
		return networkInterface;
	}

	public void setNetworkInterface(NetworkInterface networkInterface) {
		this.networkInterface = networkInterface;
	}

	public String getIpaddress() {
		if (networkInterface != null) {
			return networkInterface.getIpv4();
		}
		return "";
	}

	public List<TomcatApplication> getTomcatApplications() {
		return tomcatApplications;
	}

	public void setTomcatApplications(List<TomcatApplication> tomcatApplications) {
		this.tomcatApplications = tomcatApplications;
	}
}
