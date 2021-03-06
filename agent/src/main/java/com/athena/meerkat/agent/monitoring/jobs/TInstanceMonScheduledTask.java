package com.athena.meerkat.agent.monitoring.jobs;

import java.io.IOException;
import java.lang.management.MemoryUsage;
import java.rmi.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.hyperic.sigar.NetConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.athena.meerkat.agent.MeerkatAgentConstants;
import com.athena.meerkat.agent.monitoring.utils.JSONUtil;
import com.athena.meerkat.agent.monitoring.utils.SigarUtil;
import com.athena.meerkat.agent.monitoring.websocket.StompWebSocketClient;
import com.athena.meerkat.common.tomcat.TomcatVersionsProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * <pre>
 * tomcat instance monitoring.
 * - instance status.
 * - jmx monitoring.
 * </pre>
 * 
 * @author Bongjin Kwon
 * @version 1.0
 */
@Component
public class TInstanceMonScheduledTask extends MonitoringTask {

	private static final Logger LOGGER = LoggerFactory.getLogger(TInstanceMonScheduledTask.class);

	private static final String MON_FACTOR_ID_THREADS = "jmx.tomcatThreads";

	private static final String JMX_ATTR_PROC_CPU = "ProcessCpuLoad";
	private static final String JMX_ATTR_SYS_CPU = "SystemCpuLoad";

	private static final String JMX_ATTR_HEAPMEM = "HeapMemoryUsage";
	private static final String JMX_ATTR_THREAD_POOL = "Catalina:type=ThreadPool,name=\"";
	private static final String JMX_ATTR_THREAD_USED = "currentThreadsBusy";
	private static final String JMX_ATTR_THREAD_MAX = "maxThreads";

	private static final double asMB = 1024.d * 1024.d;

	private List<String> monDatas = new ArrayList<String>();

	private Map<String, JMXConnector> jmxConnMap = new HashMap<String, JMXConnector>();

	private ObjectName memoryObj;
	private ObjectName cpuObj;
	private Map<String, ObjectName> dsObjects;
	
	@Autowired
	private StompWebSocketClient webSocketClient;
	
	@Autowired
	private TomcatVersionsProperties tomcatVerProps;

	public TInstanceMonScheduledTask() {
	}

	@Scheduled(fixedRate = 10000)
	@Override
	public void monitor() {

		monDatas.clear();
		
		if (ServerInitialMonTask.ENABLE_MNITORING == false) {
			LOGGER.debug("======== skip.");
			return;
		}
		
		if (webSocketClient.isOpen() == false) {
			LOGGER.info("websocket is not connected.");
			return;
		}
		
		try {
			ArrayNode tomcatConfigs = webSocketClient.getInstanceConfigs();//DomainTomcatConfiguration array

			if (tomcatConfigs == null || tomcatConfigs.size() == 0) {
				LOGGER.debug("tomcat instance configs is empty. jmx monitoring skip!");
				return;
			}

			NetConnection[] listenPorts = SigarUtil.getListenPorts();

			for (JsonNode tomcatConfig : tomcatConfigs) {

				String tomcatInstanceId = tomcatConfig.get(MeerkatAgentConstants.JSON_KEY_TOMCAT_INSTANCE_ID).asText();
				long port 				= tomcatConfig.get("httpPort").asLong();
				int tomcatVersionCd 	= tomcatConfig.get("tomcatVersionCd").asInt();
				int isRun 				= 7; // TS_STATE common code id.
				
				for (NetConnection netConnection : listenPorts) {
					if (netConnection.getLocalPort() == port) {
						isRun = 8;
					}
				}

				monDatas.add(createJmxJsonString("ti.run", tomcatInstanceId, isRun, 0));//tomcat instance running status.
				monitorJMX(monDatas, tomcatInstanceId, tomcatConfig, tomcatVersionCd);
			}


			sendInstanceMonData(monDatas);

		} catch (Exception e) {
			LOGGER.error(e.toString(), e);
		} finally {
			monDatas.clear();
		}
	}

	private void monitorJMX(List<String> monDatas, String tomcatInstanceId, JsonNode tomcatConfig, int tomcatVersionCd) {

		String rmiRegistryPort = JSONUtil.getString(tomcatConfig, "rmiRegistryPort");
		String httpPort = JSONUtil.getString(tomcatConfig, "httpPort");

		if (StringUtils.isEmpty(rmiRegistryPort)) {

			LOGGER.debug("rmi port is empty. monitoring skip.");
			return;
		}

		JMXConnector jmxc = jmxConnMap.get(tomcatInstanceId);

		try {
			if (jmxc == null) {
				JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:" + rmiRegistryPort + "/jmxrmi");
				jmxc = JMXConnectorFactory.connect(url);
				LOGGER.debug("JMX connected!!");

				jmxConnMap.put(tomcatInstanceId, jmxc);

				initJmx();
			}

			MBeanServerConnection mbeanServerConn = jmxc.getMBeanServerConnection();

			monitorTomcatCpu(mbeanServerConn, tomcatInstanceId);
			monitorTomcatHeapMemory(mbeanServerConn, tomcatInstanceId);
			monitorTomcatThreads(mbeanServerConn, tomcatInstanceId, httpPort, tomcatVersionCd);
			monitorJDBC(mbeanServerConn, tomcatInstanceId, tomcatVersionCd);

		} catch (ConnectException e) {
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.error(e.toString(), e);
			}
			
			closeJmx(jmxc, tomcatInstanceId);

		} catch (IOException e) {
			if (e.toString().contains("Connection closed")) {
				closeJmx(jmxc, tomcatInstanceId);
			} else {
				handleException(e, tomcatInstanceId);
			}
		} catch (Exception e) {
			handleException(e, tomcatInstanceId);
		}

	}

	private void closeJmx(JMXConnector jmxc, String tomcatInstanceId) {
		try {
			jmxc.close();
		} catch (IOException ex) {
			//ignore.
		}
		jmxConnMap.remove(tomcatInstanceId);

		if (dsObjects != null) {
			dsObjects.clear();
			dsObjects = null;
		}
	}

	private void handleException(Exception e, String tomcatInstanceId) {
		//if (LOGGER.isDebugEnabled()) {
		//	LOGGER.error(e.toString(), e);
		//}else {
		LOGGER.error("instanceId:{} - {}", tomcatInstanceId, e.toString());
		//}
	}

	private void monitorTomcatCpu(MBeanServerConnection mbeanServerConn, String tomcatInstanceId) throws Exception {

		Object used = mbeanServerConn.getAttribute(cpuObj, JMX_ATTR_PROC_CPU);//returns the CPU usage of the JVM
		Object sysCpuUsed = mbeanServerConn.getAttribute(cpuObj, JMX_ATTR_SYS_CPU);// returns the CPU usage of the whole system

		double usedVal = parseDouble(used) * 100d;
		double sysVal = parseDouble(sysCpuUsed) * 100d;

		monDatas.add(createJmxJsonString(MeerkatAgentConstants.MON_FACTOR_ID_CPU_USED_PER, tomcatInstanceId, usedVal, sysVal));
	}

	private void monitorTomcatHeapMemory(MBeanServerConnection mbeanServerConn, String tomcatInstanceId) throws Exception {

		MemoryUsage memUsage = MemoryUsage.from((CompositeData) mbeanServerConn.getAttribute(memoryObj, JMX_ATTR_HEAPMEM));

		double used = memUsage.getUsed() / asMB;//mbytes
		double max = memUsage.getMax() / asMB;//mbytes
		double used_per = used * 100D / max;

		monDatas.add(createJmxJsonString(MeerkatAgentConstants.MON_FACTOR_ID_MEM_USED, tomcatInstanceId, used, max));
		monDatas.add(createJmxJsonString(MeerkatAgentConstants.MON_FACTOR_ID_MEM_USED_PER, tomcatInstanceId, used_per, 0));
	}

	private void monitorTomcatThreads(MBeanServerConnection mbeanServerConn, String tomcatInstanceId, String httpPort, int tomcatVersionCd) throws Exception {

		ObjectName name = new ObjectName(getThreadsAttr(tomcatVersionCd) + httpPort + "\"");
		Object used = mbeanServerConn.getAttribute(name, JMX_ATTR_THREAD_USED);// currentThreadsBusy
		Object max = mbeanServerConn.getAttribute(name, JMX_ATTR_THREAD_MAX);// maxThreads

		monDatas.add(createJmxJsonString(MON_FACTOR_ID_THREADS, tomcatInstanceId, parseDouble(used), parseDouble(max)));
	}

	private void monitorJDBC(MBeanServerConnection mbeanServerConn, String tomcatInstanceId, int tomcatVersionCd) throws Exception {

		if (dsObjects == null) {
			initDSObjects(mbeanServerConn, tomcatInstanceId);
		}

		if (dsObjects != null && dsObjects.size() > 0) {
			for (Entry<String, ObjectName> entry : dsObjects.entrySet()) {
				Object active = mbeanServerConn.getAttribute(entry.getValue(), "numActive");
				Object idle = mbeanServerConn.getAttribute(entry.getValue(), "numIdle");
				Object max = mbeanServerConn.getAttribute(entry.getValue(), tomcatVerProps.getDBCPMaxActive(tomcatVersionCd));

				double connVal = parseDouble(active) + parseDouble(idle);
				double maxConnVal = parseDouble(max);

				monDatas.add(createJmxJsonString("jmx.ds." + entry.getKey(), tomcatInstanceId, connVal, maxConnVal));
			}
		} else {
			LOGGER.debug("DataSource is empty.");
			dsObjects = null;
		}

	}

	private void initJmx() throws Exception {

		if (memoryObj == null) {
			memoryObj = new ObjectName("java.lang:type=Memory");
		}

		if (cpuObj == null) {
			cpuObj = new ObjectName("java.lang:type=OperatingSystem");
		}

	}

	private void initDSObjects(MBeanServerConnection mbeanServerConn, String tomcatInstanceId) {
		try {

			String objectName = "Catalina:type=DataSource,*";
			Set<ObjectName> contexts = mbeanServerConn.queryNames(new ObjectName(objectName), null);

			String contextName = null;
			for (ObjectName context : contexts) {
				contextName = context.getKeyProperty("context");
				break;
			}

			objectName = "Catalina:type=DataSource,context=" + contextName + ",*";
			Set<ObjectName> hosts = mbeanServerConn.queryNames(new ObjectName(objectName), null);

			String hostName = null;
			for (ObjectName host : hosts) {
				hostName = host.getKeyProperty("host");
				break;
			}

			objectName = "Catalina:type=DataSource,context=" + contextName + ",host=" + hostName + ",class=javax.sql.DataSource,*";
			Set<ObjectName> dsNames = mbeanServerConn.queryNames(new ObjectName(objectName), null);

			LOGGER.debug("dsNames.size : {}, {}", dsNames.size(), objectName);

			dsObjects = new HashMap<String, ObjectName>();
			String jndiName = null;
			for (ObjectName dsName : dsNames) {
				jndiName = dsName.getKeyProperty("name");

				objectName = "Catalina:type=DataSource,context=" + contextName + ",host=" + hostName + ",class=javax.sql.DataSource,name=" + jndiName + "";
				LOGGER.debug("JNDI NAME : {}", objectName);

				dsObjects.put(jndiName.replaceAll("\"", ""), new ObjectName(objectName));
			}

		} catch (Exception e) {
			//if (LOGGER.isDebugEnabled()) {
			//	LOGGER.error(e.toString(), e);
			//}else {
			LOGGER.error("instanceId:{} - {}", tomcatInstanceId, e.toString());
			//}
		}
	}

	private double parseDouble(Object obj) {
		return Double.parseDouble(obj.toString());
	}
	
	private String getThreadsAttr(int tomcatVersionCd) {
		return JMX_ATTR_THREAD_POOL + tomcatVerProps.getTomcatThreadAttr(tomcatVersionCd);
	}

}