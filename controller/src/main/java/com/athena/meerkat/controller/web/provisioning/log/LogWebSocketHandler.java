/* 
 * Copyright (C) 2012-2015 Open Source Consulting, Inc. All rights reserved by Open Source Consulting, Inc.
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
 * BongJin Kwon		2016. 4. 12.		First Draft.
 */
package com.athena.meerkat.controller.web.provisioning.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.athena.meerkat.common.util.JSONUtil;
import com.athena.meerkat.controller.web.provisioning.TomcatProvisioningService;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * <pre>
 * 
 * </pre>
 * @author Bongjin Kwon
 * @version 1.0
 */
@Component
public class LogWebSocketHandler extends TextWebSocketHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LogWebSocketHandler.class);
	
	@Autowired
	private TomcatProvisioningService service;
	
	/**
	 * <pre>
	 * 
	 * </pre>
	 */
	public LogWebSocketHandler() {
		
	}
	
	

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		LOGGER.debug("connected!! id={}", session.getId());
		/*
		LogTailerListener listener = new LogTailerListener(session);
		long delay = 2000;
		File file = new File("G:\\project\\AthenaMeerkat\\meerkat.log");
		Tailer tailer = new Tailer(file, listener, delay);
		new Thread(tailer).start();
		*/
	}



	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		LOGGER.debug("disconnected!!");
	}



	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		LOGGER.debug(message.toString());
		
		String jsonMsg = message.getPayload();

		LOGGER.debug("playload : {}", jsonMsg);
		
		try{
			
			JsonNode json = JSONUtil.readTree(jsonMsg);
			
			String event = json.get("event").asText();
			JsonNode data = json.get("data");
			int domainId = data.get("domainId").asInt();
			
			LOGGER.debug("event: {}, domainId : {}", event, domainId);
		
			if ("viewLog".equals(event)) {
				
			
				int taskHistoryDetailId = data.get("taskDetailId").asInt();
				service.sendLog(session, taskHistoryDetailId);
				
			/*
			 * TaskWorkingWindow.js 로 변경. 아래 나중에 지울것임.
			 * 
			} else if (MeerkatConstants.WS_EVENT_DEPLOY.equals(event)) {
				
				String contextPath = data.get("contextPath").asText();
				String warFilePath = data.get("warFilePath").asText();
				
				service.deployWar(domainId, warFilePath, contextPath, session);
			
			} else if (MeerkatConstants.WS_EVENT_UXMLFILE.equals(event)) {
				
				int configFileId = data.get("configFileId").asInt();
				
				service.updateXml(domainId, configFileId, session);
			
			} else if (MeerkatConstants.WS_EVENT_INSTALL_MYSQL_DRIVER.equals(event)) {
				
				
				service.installJar(domainId, "mysql-connector-java-5.1.38.jar", session);
			*/	
			}
		
		}catch(Exception e) {
			LOGGER.error(e.toString(), e);
		}
		
	}

}
//end of LogWebSocketHandler.java
