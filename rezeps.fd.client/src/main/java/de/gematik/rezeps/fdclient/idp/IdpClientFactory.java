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
import org.apache.commons.lang.StringUtils;

/**
 * Stellt für unterschiedliche Teststufen Implementierungen des Interface I_IDPClient bereit.
 */
public class IdpClientFactory {

  private IdpClientFactory() {}

  /**
   * Falls ein bearerToken vorliegt wird ein IDP-Client erzeugt, der dieses Bearer-Token verwendet,
   * andernfalls wird der Wrapper der TestNG-Testsuite verwendet.
   * @param bearerToken Vorliegendes Bearer-Token des IDP, kann leer sein.
   * @return IDP-Client in Abhängigkeit von der Existenz eines Bearer-Tokens.
   */
  public static I_IDPClient determineIdpClient(String bearerToken) {
    I_IDPClient idpClient = null;
    if (StringUtils.isEmpty(bearerToken)) {
      idpClient = new IdpClientWrapper();
    } else {
      idpClient = new StaticTokenIdpClient(bearerToken);
    }
    return idpClient;
  }

}
