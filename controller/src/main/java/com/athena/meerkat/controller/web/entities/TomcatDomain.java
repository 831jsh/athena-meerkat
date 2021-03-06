package com.athena.meerkat.controller.web.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.context.annotation.Lazy;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A domain is used for grouping one or more tomcat instance. It is associated to domain table in database
 * 
 * @author Tran Ho
 * 
 */
@Entity
@Table(name = "tomcat_domain")
@Lazy
public class TomcatDomain implements Serializable {

	private static final long serialVersionUID = -158159581038603435L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "Id")
	private int id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "create_user_id")
	private int createUser;
	
	@Column(name = "scouter_agent_install_path")
	private String scouterAgentInstallPath;

	@Transient
	private int latestConfVersionId;
	@Transient
	private int latestServerXmlVersion;
	@Transient
	private int latestContextXmlVersion;

	@ManyToOne
	@JsonIgnore
	@JoinColumn(name = "datagrid_server_group_id")
	private DatagridServerGroup serverGroup;

	@OneToMany(mappedBy = "tomcatDomain", fetch = FetchType.LAZY)
	@JsonIgnore
	private List<TomcatInstance> tomcatInstances;

	@OneToOne(mappedBy = "tomcatDomain", cascade = CascadeType.REMOVE)
	private DomainTomcatConfiguration domainTomcatConfig;

	@OneToMany(mappedBy = "tomcatDomain", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	@JsonIgnore
	private List<TomcatConfigFile> tomcatConfigFiles;

	@OneToMany(mappedBy = "tomcatDomain", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	@JsonIgnore
	private List<TomcatApplication> tomcatApplication;

	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	@JsonIgnore
	@JoinTable(name = "tomcat_domain_datasource", joinColumns = @JoinColumn(name = "tomcat_domain_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "datasource_id", referencedColumnName = "id"))
	private List<DataSource> datasources;

	@OneToMany(mappedBy = "tomcatDomain", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	@JsonIgnore
	private List<MonAlertConfig> monAlertConfigs;


	/**
	 * Constructor
	 */
	public TomcatDomain() {
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<TomcatInstance> getTomcatInstances() {
		return tomcatInstances;
	}

	public void setTomcatInstances(List<TomcatInstance> tomcatInstances) {
		this.tomcatInstances = tomcatInstances;
	}

	public int getTomcatInstancesCount() {
		if (tomcatInstances != null) {
			return tomcatInstances.size();
		}
		return 0;
	}

	public int getApplicationCount() {
		if (tomcatApplication != null) {
			return tomcatApplication.size();
		}
		return 0;
	}

	public List<DataSource> getDatasources() {
		return datasources;
	}

	public void setDatasources(List<DataSource> datasources) {
		this.datasources = datasources;
	}

	public int getCreateUser() {
		return createUser;
	}

	public void setCreateUser(int createUser) {
		this.createUser = createUser;
	}

	public DatagridServerGroup getServerGroup() {
		return serverGroup;
	}

	public void setServerGroup(DatagridServerGroup serverGroup) {
		this.serverGroup = serverGroup;
	}

	public String getDatagridServerGroupName() {
		if (getServerGroup() != null) {
			return this.getServerGroup().getName();
		}
		return "";
	}

	public int getDataGridServerGroupId() {
		if (getServerGroup() != null) {
			return this.getServerGroup().getId();
		}
		return 0;

	}

	public boolean isClustering() {
		if (serverGroup == null) {
			return false;
		}
		return true;
	}

	public DomainTomcatConfiguration getDomainTomcatConfig() {
		return domainTomcatConfig;
	}

	public void setDomainTomcatConfig(DomainTomcatConfiguration domainTomcatConfig) {
		this.domainTomcatConfig = domainTomcatConfig;
	}

	public List<TomcatApplication> getTomcatApplication() {
		return tomcatApplication;
	}

	public void setTomcatApplication(List<TomcatApplication> tomcatApplication) {
		this.tomcatApplication = tomcatApplication;
	}

	public int getLatestConfVersionId() {
		return latestConfVersionId;
	}

	public void setLatestConfVersionId(int latestConfVersionId) {
		this.latestConfVersionId = latestConfVersionId;
	}

	public int getLatestServerXmlVersion() {
		return latestServerXmlVersion;
	}

	public void setLatestServerXmlVersion(int latestServerXmlVersion) {
		this.latestServerXmlVersion = latestServerXmlVersion;
	}

	public int getLatestContextXmlVersion() {
		return latestContextXmlVersion;
	}

	public void setLatestContextXmlVersion(int latestContextXmlVersion) {
		this.latestContextXmlVersion = latestContextXmlVersion;
	}

	public List<TomcatConfigFile> getTomcatConfigFiles() {
		return tomcatConfigFiles;
	}

	public void setTomcatConfigFiles(List<TomcatConfigFile> tomcatConfigFiles) {
		this.tomcatConfigFiles = tomcatConfigFiles;
	}

	public List<MonAlertConfig> getMonAlertConfigs() {
		return monAlertConfigs;
	}

	public void setMonAlertConfigs(List<MonAlertConfig> monAlertConfigs) {
		this.monAlertConfigs = monAlertConfigs;
	}

	public String getScouterAgentInstallPath() {
		return scouterAgentInstallPath;
	}

	public void setScouterAgentInstallPath(String scouterAgentInstallPath) {
		this.scouterAgentInstallPath = scouterAgentInstallPath;
	}

}
