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

package de.gematik.rezeps.fdclient.idp;

import de.gematik.rezeps.fdclient.IdpClientWrapper;
import de.gematik.test.erezept.idp.I_IDPClient;
import org.junit.Assert;
import org.junit.Test;

public class IdpClientFactoryTest {

  @Test
  public void shouldDetermineIdpClientWrapper() {
    I_IDPClient idpClient = IdpClientFactory.determineIdpClient(null);
    Assert.assertTrue(idpClient instanceof IdpClientWrapper);
  }

  @Test
  public void shouldDetermineStaticTokenIdpClient() {
    I_IDPClient idpClient = IdpClientFactory.determineIdpClient("some token");
    Assert.assertTrue(idpClient instanceof StaticTokenIdpClient);
  }

}
