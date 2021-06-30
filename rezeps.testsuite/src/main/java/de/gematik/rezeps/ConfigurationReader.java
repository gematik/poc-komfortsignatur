/*
 * Copyright (c) 2021 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.rezeps;

import de.gematik.rezeps.util.CommonUtils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Ermöglicht den Zugriff auf die Konfiguration. */
public class ConfigurationReader {

  private String configurationFileName = "configuration.properties";
  private static final String KONNEKTOR_IP = "konnektor_ip";
  private static final String KONNEKTOR_PORT = "konnektor_port";
  private static final String KONNEKTOR_PROTOKOLL = "konnektor_protokoll";
  private static final String ACCESS_TOKEN_PRESCRIBING_ENTITY = "access_token_prescribing_entity";
  private static final String ACCESS_TOKEN_DISPENSING_ENTITY = "access_token_dispensing_entity";
  private static final String KONNEKTOR_AUTH_SIGNATURE_SERVICE_ENDPOINT =
      "konnektor_auth_signature_service_endpoint";
  private static final String KONNEKTOR_CERTIFICATE_SERVICE_ENDPOINT =
      "konnektor_certificate_service_endpoint";
  private static final String KONNEKTOR_EVENT_SERVICE_ENDPOINT = "konnektor_event_service_endpoint";
  private static final String KONNEKTOR_SIGNATURE_SERVICE_ENDPOINT =
      "konnektor_signature_service_endpoint";
  private static final String KONNEKTOR_CARD_SERVICE_ENDPOINT = "konnektor_card_service_endpoint";
  private static final String KONNEKTOR_CARD_TERMINAL_SERVICE_ENDPOINT =
      "konnektor_card_terminal_service_endpoint";
  private static final String PATH_TO_CLIENT_KEYSTORE = "path_to_client_keystore";
  private static final String CLIENT_KEYSTORE_PASSWORD = "client_keystore_password";
  private static final String PATH_TO_CLIENT_TRUSTSTORE = "path_to_client_truststore";
  private static final String CLIENT_TRUSTSTORE_PASSWORD = "client_truststore_password";

  private static final String IDP_DISCOVERY_URL = "idp_discovery_url";
  private static final String IDP_REDIRECT_URL = "idp_redirect_url";
  private static final String IDP_CLIENT_ID = "idp_client_id";
  private static final String CATS_BASEURI = "cats_base";

  private static ConfigurationReader instance;

  private Properties properties = new Properties();
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationReader.class);

  private ConfigurationReader() throws IOException {
    String testEnvName = System.getProperty("CFG_PROPS", "");
    if (!CommonUtils.isNullOrEmpty(testEnvName)) {
      configurationFileName = "configuration." + testEnvName + ".properties";
      if (LOGGER.isDebugEnabled()) {
        LOGGER.info(
            MessageFormat.format("using {0} as file {1}", testEnvName, configurationFileName));
      }
    }

    InputStream inputStream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(configurationFileName);
    if (inputStream != null) {
      properties.load(inputStream);
    } else {
      throw new FileNotFoundException(
          String.format(
              "The properties file %s was not found on the classpath.", configurationFileName));
    }
  }

  /**
   * Liefert die Instanz der Klasse nach dem Singleton-Pattern.
   *
   * @return Die Instanz der Klasse.
   * @throws IOException Wenn die Konfigurationsdatei nicht gelesen werden kann.
   */
  public static ConfigurationReader getInstance() throws IOException {
    if (instance == null) {
      instance = new ConfigurationReader();
    }
    return instance;
  }

  public boolean konnektorUseSSL() {
    return "https://".equalsIgnoreCase(getProtokoll());
  }

  public String getProtokoll() {
    String protokoll = getConfigurationProperty(KONNEKTOR_PROTOKOLL);
    if (CommonUtils.isNullOrEmpty(protokoll)) {
      return "https://";
    } else {
      if (protokoll.endsWith("/")) {
        return protokoll;
      } else {
        return protokoll + "://";
      }
    }
  }

  public String getCatsBaseuri() {
    String catsbase = getConfigurationProperty(CATS_BASEURI);
    if (CommonUtils.isNullOrEmpty(catsbase)) {
      return "http://localhost:9998";
    } else {
      return catsbase;
    }
  }

  public String getKonnektorIp() {
    return getConfigurationProperty(KONNEKTOR_IP);
  }

  public String getKonnektorPort() {
    return getConfigurationProperty(KONNEKTOR_PORT);
  }

  public String getAccessTokenPrescribingEntity() {
    return getConfigurationProperty(ACCESS_TOKEN_PRESCRIBING_ENTITY);
  }

  public void setAccessTokenPrescribingEntity(String accessTokenPrescribingEntity) {
    properties.setProperty(ACCESS_TOKEN_PRESCRIBING_ENTITY, accessTokenPrescribingEntity);
  }

  public String getAccessTokenDispensingEntity() {
    return properties.getProperty(ACCESS_TOKEN_DISPENSING_ENTITY);
  }

  public void setAccessTokenDispensingEntity(String accessTokenDispensingEntity) {
    properties.setProperty(ACCESS_TOKEN_DISPENSING_ENTITY, accessTokenDispensingEntity);
  }

  public String getKonnektorAuthSignatureServiceEndpoint() {
    return properties.getProperty(KONNEKTOR_AUTH_SIGNATURE_SERVICE_ENDPOINT);
  }

  public String getKonnektorCertificateServiceEndpoint() {
    return getConfigurationProperty(KONNEKTOR_CERTIFICATE_SERVICE_ENDPOINT);
  }

  public String getKonnektorEventServiceEndpoint() {
    return getConfigurationProperty(KONNEKTOR_EVENT_SERVICE_ENDPOINT);
  }

  public String getKonnektorSignatureServiceEndpoint() {
    return getConfigurationProperty(KONNEKTOR_SIGNATURE_SERVICE_ENDPOINT);
  }

  public String getKonnektorCardServiceEndpoint() {
    return getConfigurationProperty(KONNEKTOR_CARD_SERVICE_ENDPOINT);
  }

  public String getKonnektorCardTerminalServiceEndpoint() {
    return getConfigurationProperty(KONNEKTOR_CARD_TERMINAL_SERVICE_ENDPOINT);
  }

  public String getPathToClientKeystore() {
    return getConfigurationProperty(PATH_TO_CLIENT_KEYSTORE);
  }

  public String getClientKeystorePassword() {
    return getConfigurationProperty(CLIENT_KEYSTORE_PASSWORD);
  }

  public String getPathToClientTruststore() {
    return getConfigurationProperty(PATH_TO_CLIENT_TRUSTSTORE);
  }

  public String getClientTruststorePassword() {
    return getConfigurationProperty(CLIENT_TRUSTSTORE_PASSWORD);
  }

  public String getIdpDiscoveryUrl() {
    return getConfigurationProperty(IDP_DISCOVERY_URL);
  }

  public String getIdpRedirectUrl() {
    return getConfigurationProperty(IDP_REDIRECT_URL);
  }

  public String getIdpClientId() {
    return getConfigurationProperty(IDP_CLIENT_ID);
  }

  private String getConfigurationProperty(String propertyKey) {
    String propertyValue = "";
    if (properties != null) {
      propertyValue = properties.getProperty(propertyKey);
    }
    return propertyValue;
  }
}
