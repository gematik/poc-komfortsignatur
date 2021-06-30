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

package de.gematik.rezeps.service;

import de.gematik.rezeps.ConfigurationReader;
import de.gematik.rezeps.util.CommonUtils;
import java.io.IOException;
import java.net.InetAddress;
import java.security.SecureRandom;
import java.util.Random;
import org.xbill.DNS.*;

/**
 * @since 0.0.2
 * @author gematik GmbH IPUtil zum ermitteln der IP Adressen des Fachdienstes via Anfrage an den
 *     Konnektor verwendung findet https://github.com/dnsjava/dnsjava
 */
public class IPUtil {

  public static final String SYSTEM_PROPERTY_DNS_SERVER = "dns.server";

  /** Konstruktor */
  private IPUtil() {
    // nothing here just a private Constructor
  }

  /**
   * Ermittelt die IP des FD via Konnektor
   *
   * @param fqdn String Fully-Qualified Domain Name
   * @return String IP
   */
  public static synchronized String getIpAddress(String fqdn) throws IOException {
    String actualDNSServer = "";
    try {
      actualDNSServer = System.getProperty(SYSTEM_PROPERTY_DNS_SERVER);
      ConfigurationReader configurationReader = ConfigurationReader.getInstance();
      System.setProperty(
          SYSTEM_PROPERTY_DNS_SERVER, configurationReader.getKonnektorIp()); // NOSONAR
      InetAddress[] addr = Address.getAllByName(fqdn);
      return getRandomIpFromResult(addr);
    } catch (IOException ioException) {
      throw new IOException(ioException);
    } finally {
      if (!CommonUtils.isNullOrEmpty(actualDNSServer)) {
        System.setProperty(SYSTEM_PROPERTY_DNS_SERVER, actualDNSServer);
      } else {
        System.clearProperty(SYSTEM_PROPERTY_DNS_SERVER);
      }
    }
  }

  private static synchronized String getRandomIpFromResult(InetAddress[] addr) throws IOException {
    if (addr == null) {
      throw new IOException("No IP Addresses resolved.");
    }
    int max = addr.length;
    Random rn = new SecureRandom();
    int i = rn.nextInt(max);
    return addr[i].getHostAddress();
  }
}
