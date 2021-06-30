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

import java.io.IOException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

public class IPUtilIntegrationTest {

  @Rule public Timeout globalTimeout = Timeout.seconds(10); // 10 seconds max per method tested

  @Test(expected = IOException.class)
  public void getIpAddressShouldFail() throws IOException {

    String ipAddress = IPUtil.getIpAddress("konni-simulator.edu");
    Assert.assertNull(ipAddress);
  }

  @Test
  @Ignore("Verfügbar, wenn direkte Verbindung zum Konnektor verfügbar ist.")
  public void getIpAddress() throws IOException {

    String ipAddress = IPUtil.getIpAddress("google.com");
    Assert.assertNotNull(ipAddress);
  }

  @Test
  public void shouldCleanUpWhenSystemPropertyIsSet() {
    String expected = "4.3.2.1";
    System.setProperty(IPUtil.SYSTEM_PROPERTY_DNS_SERVER, expected);
    try {
      IPUtil.getIpAddress("google.com");
    } catch (IOException ioException) {
      // we can ignore the exception but we have to test the reverted settings
      Assert.assertEquals(expected, System.getProperty(IPUtil.SYSTEM_PROPERTY_DNS_SERVER));
    } finally {
      // finally we must clearing the test settings
      System.clearProperty(IPUtil.SYSTEM_PROPERTY_DNS_SERVER);
      Assert.assertNull(System.getProperty(IPUtil.SYSTEM_PROPERTY_DNS_SERVER));
    }
  }

  @Test
  public void shouldCleanUpWhenSystemPropertyIsNotSet() {
    try {
      IPUtil.getIpAddress("google.com");
    } catch (IOException ioException) {
      // we can ignore the exception but we have to test the reverted settings - must be null
      Assert.assertNull(System.getProperty(IPUtil.SYSTEM_PROPERTY_DNS_SERVER));
    } finally {
      // finally we must clear the Setting
      System.clearProperty(IPUtil.SYSTEM_PROPERTY_DNS_SERVER);
    }
  }
}
