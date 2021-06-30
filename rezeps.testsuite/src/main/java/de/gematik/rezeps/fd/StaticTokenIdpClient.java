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

package de.gematik.rezeps.fd;

import de.gematik.test.erezept.idp.I_IDPClient;
import de.gematik.test.erezept.keystore.Identity;

/**
 * Ermöglicht die statische Konfiguration eines Bearer Tokens. Ein Login beim IDP findet nicht
 * statt.
 */
public class StaticTokenIdpClient implements I_IDPClient {

  private String bearerToken;

  public StaticTokenIdpClient(String bearerToken) {
    this.bearerToken = bearerToken;
  }

  @Override
  public String getBearerToken() {
    return bearerToken;
  }

  @Override
  public void login(Identity identity) {
    // Do nothing here //NOSONAR
  }

  @Override
  public Identity getIdentity() {
    // Nothing to return here
    return null;
  }
}
