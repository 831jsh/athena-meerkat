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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.athena.meerkat.controller.MeerkatConstants;
import com.athena.meerkat.controller.web.common.util.WebUtil;
import com.athena.meerkat.controller.web.monitoring.jmx.MonJmx;
import com.athena.meerkat.controller.web.monitoring.stat.MonUtilStat;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

	private static final long serialVersionUID = 1852609995878767410L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "Id")
	private int id;
	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "state")
	private int state = MeerkatConstants.TOMCAT_STATUS_NOTINSTALLED; //default value.

	@Transient
	private String stateNm;

	@Column(name = "created_time")
	private Date createdTime;
	@Column(name = "create_user_id")
	private int createUserId;

	@Column(name = "domain_id", insertable = false, updatable = false)
	private int domainId;

	@Column(name = "server_id", insertable = false, updatable = false)
	private int serverId;
	
	@Column(name = "task_history_id")
	private int lastTaskHistoryId;// last task id (maybe install or remove task).

	@OneToMany(mappedBy = "tomcatInstance", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	@JsonManagedReference(value = "inst-configFile")
	private List<TomcatConfigFile> tomcatConfigFiles;

	@ManyToOne(fetch = FetchType.LAZY)
	@JsonBackReference(value = "inst-server")
	private Server server;

	@ManyToOne(fetch = FetchType.LAZY)
	// using this annotation to prevent Infinite recursion json mapping
	@JsonBackReference(value = "domain-inst")
	@JoinColumn(name = "domain_id")
	private TomcatDomain tomcatDomain;

	@OneToMany(mappedBy = "tomcatInstance", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	@JsonManagedReference(value = "inst-app")
	private List<TomcatApplication> tomcatApplications;

	@OneToMany(mappedBy = "tomcatInstance", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	//@JsonManagedReference(value = "config-tomcat")
	private List<TomcatInstConfig> tomcatConfigs;
	
	@OneToMany(mappedBy = "tomcatInstance", fetch = FetchType.LAZY)
	@JsonIgnore
	private List<TaskHistoryDetail> taskHistoryDetails;
	
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	@JsonIgnore
	@JoinColumn(name = "tomcat_instance_id")
	private List<MonUtilStat> monUtilStats;
	
	
	public TomcatInstance() {
	}

	public int getDomainId() {
		return domainId;
	}

	public void setDomainId(int domainId) {
		this.domainId = domainId;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public String getStateNm() {
		return stateNm;
	}

	public void setStateNm(String stateNm) {
		this.stateNm = stateNm;
	}

	public String getHostName() {
		if (server != null) {
			return server.getHostName();
		}
		return "";
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

	public String getIpaddress() {
		if (server != null) {
			return server.getSshIPAddr();
		}
		return "";
	}

	public List<TomcatApplication> getTomcatApplications() {
		return tomcatApplications;
	}

	public void setTomcatApplications(List<TomcatApplication> tomcatApplications) {
		this.tomcatApplications = tomcatApplications;
	}

	public List<TomcatInstConfig> getTomcatConfigs() {
		return tomcatConfigs;
	}

	public void setTomcatConfigs(List<TomcatInstConfig> tomcatConfigs) {
		this.tomcatConfigs = tomcatConfigs;
	}

	public int getLastTaskHistoryId() {
		return lastTaskHistoryId;
	}

	public void setLastTaskHistoryId(int lastTaskHistoryId) {
		this.lastTaskHistoryId = lastTaskHistoryId;
	}

	@PrePersist
	public void onPreSave() {
		this.createdTime = new Date();
		this.createUserId = WebUtil.getLoginUserId();
	}

}
