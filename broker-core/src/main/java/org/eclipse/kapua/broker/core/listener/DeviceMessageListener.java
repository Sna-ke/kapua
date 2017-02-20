/*******************************************************************************
 * Copyright (c) 2011, 2016 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.kapua.broker.core.listener;

import org.apache.camel.spi.UriEndpoint;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.broker.core.message.CamelKapuaMessage;
import org.eclipse.kapua.commons.security.KapuaSecurityUtils;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.message.device.lifecycle.KapuaAppsMessage;
import org.eclipse.kapua.message.device.lifecycle.KapuaBirthMessage;
import org.eclipse.kapua.message.device.lifecycle.KapuaDisconnectMessage;
import org.eclipse.kapua.message.device.lifecycle.KapuaMissingMessage;
import org.eclipse.kapua.message.device.lifecycle.KapuaNotifyMessage;
import org.eclipse.kapua.message.device.lifecycle.KapuaUnmatchedMessage;
import org.eclipse.kapua.service.device.registry.lifecycle.DeviceLifeCycleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;

/**
 * Device messages listener (device life cycle).<br>
 * Manage:<br>
 * - BIRTH/DC/LWT/APPS/NOTIFY device messages<br>
 * Republish of the lifecycle messages (once processed by the broker) isn't supported yet (see #136).
 *
 * @since 1.0
 */
@UriEndpoint(title = "device message processor", syntax = "bean:deviceMessageListener", scheme = "bean")
public class DeviceMessageListener extends AbstractListener {

    private static final Logger logger = LoggerFactory.getLogger(DeviceMessageListener.class);

    private static DeviceLifeCycleService deviceLifeCycleService = KapuaLocator.getInstance().getService(DeviceLifeCycleService.class);

    // metrics
    private Counter metricDeviceBirthMessage;
    private Counter metricDeviceDisconnectMessage;
    private Counter metricDeviceMissingMessage;
    private Counter metricDeviceAppsMessage;
    private Counter metricDeviceNotifyMessage;
    private Counter metricDeviceUnmatchedMessage;
    private Counter metricDeviceErrorMessage;

    public DeviceMessageListener() {
        super("deviceLifeCycle");
        metricDeviceBirthMessage = registerCounter("messages", "birth", "count");
        metricDeviceDisconnectMessage = registerCounter("messages", "dc", "count");
        metricDeviceMissingMessage = registerCounter("messages", "missing", "count");
        metricDeviceAppsMessage = registerCounter("messages", "apps", "count");
        metricDeviceNotifyMessage = registerCounter("messages", "notify", "count");
        metricDeviceUnmatchedMessage = registerCounter("messages", "unmatched", "count");
        metricDeviceErrorMessage = registerCounter("messages", "error", "count");
    }

    /**
     * Process a birth message.
     * 
     * @param birthMessage
     */
    public void processBirthMessage(CamelKapuaMessage<KapuaBirthMessage> birthMessage) {
        try {
            KapuaSecurityUtils.doPriviledge(() -> {
                deviceLifeCycleService.birth(birthMessage.getConnectionId(), birthMessage.getMessage());
                return null;
            });

            metricDeviceBirthMessage.inc();
            // //republish BA
            // Date now = new Date();
            // KapuaPayload kapuaPayload = birthMessage.getMessage().getPayload();
            // kapuaPayload.getProperties().put("server_event_time", Long.toString(now.getTime()));
            //
            // birthMessage.getMessage().setSemanticChannel(BIRTH_SEMANTIC_TOPIC);
            // try {
            // logger.debug("MESSAGE: {}", kapuaPayload);
            // JmsAssistantProducerPool pool = JmsAssistantProducerPool.getIOnstance(DESTINATIONS.NO_DESTINATION);
            // JmsAssistantProducerWrapper producer = null;
            // try {
            // producer = pool.borrowObject();
            // producer.sendRawMessage(birthMessage);
            // }
            // finally {
            // pool.returnObject(producer);
            // }
            // }
            // catch (JMSException e) {
            // logger.error("An error occurred while publishing device history event: {}", e.getMessage());
            // return;
            // }
            // catch (Throwable t) {
            // logger.warn("Cannot send birth life cycle message {}! {}", new Object[]{birthMessage.getMessage().getChannel().toString(), t.getMessage()}, t);
            // return;
            // }
        } catch (KapuaException e) {
            metricDeviceErrorMessage.inc();
            logger.error("Error while processing device birth life-cycle event", e);
            return;
        }
    }

    /**
     * Process a disconnect message.
     * 
     * @param disconnectMessage
     */
    public void processDisconnectMessage(CamelKapuaMessage<KapuaDisconnectMessage> disconnectMessage) {
        try {
            KapuaSecurityUtils.doPriviledge(() -> {
                deviceLifeCycleService.death(disconnectMessage.getConnectionId(), disconnectMessage.getMessage());
                return null;
            });
            metricDeviceDisconnectMessage.inc();
        } catch (KapuaException e) {
            metricDeviceErrorMessage.inc();
            logger.error("Error while processing device disconnect life-cycle event", e);
            return;
        }
    }

    /**
     * Process an application message.
     * 
     * @param appsMessage
     */
    public void processAppsMessage(CamelKapuaMessage<KapuaAppsMessage> appsMessage) {
        try {
            KapuaSecurityUtils.doPriviledge(() -> {
                deviceLifeCycleService.applications(appsMessage.getConnectionId(), appsMessage.getMessage());
                return null;
            });

            metricDeviceAppsMessage.inc();
        } catch (KapuaException e) {
            metricDeviceErrorMessage.inc();
            logger.error("Error while processing device apps life-cycle event", e);
            return;
        }
    }

    /**
     * Process a missing message.
     * 
     * @param missingMessage
     */
    public void processMissingMessage(CamelKapuaMessage<KapuaMissingMessage> missingMessage) {
        try {
            KapuaSecurityUtils.doPriviledge(() -> {
                deviceLifeCycleService.missing(missingMessage.getConnectionId(), missingMessage.getMessage());
                return null;
            });

            metricDeviceMissingMessage.inc();
        } catch (KapuaException e) {
            metricDeviceErrorMessage.inc();
            logger.error("Error while processing device missing life-cycle event", e);
            return;
        }
    }

    /**
     * Process a notify message.
     * 
     * @param notifyMessage
     */
    public void processNotifyMessage(CamelKapuaMessage<KapuaNotifyMessage> notifyMessage) {
        logger.info("Received notify message from device channel: {}",
                new Object[] { notifyMessage.getMessage().getChannel().toString() });
        metricDeviceNotifyMessage.inc();
    }

    /**
     * Process a unmatched message.
     * 
     * @param unmatchedMessage
     */
    public void processUnmatchedMessage(CamelKapuaMessage<KapuaUnmatchedMessage> unmatchedMessage) {
        logger.info("Received unmatched message from device channel: {}",
                new Object[] { unmatchedMessage.getMessage().getChannel().toString() });
        metricDeviceUnmatchedMessage.inc();
    }

}
