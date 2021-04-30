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

package de.gematik.rezeps.signature;

import de.gematik.rezeps.InvocationContext;
import de.gematik.ws.conn.signatureservice.v7.GetJobNumberResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Erfragt beim Konnektor die nächste Job-Nummer zum Signieren eines Dokumentes. */
@Component
public class JobNumberFinder {

  @Autowired PerformGetJobNumber performGetJobNumber;

  /**
   * Erfragt beim Konnektor die nächste Job-Nummer zum Signieren eines Dokumentes.
   *
   * @param invocationContext Der Aufrufkontext.
   * @return Die nächste Job-Nummer zum Signieren eines Dokumentes.
   */
  public String performGetJobNumber(InvocationContext invocationContext) throws IOException {
    GetJobNumberResponse getJobNumberResponse =
        performGetJobNumber.performGetJobNumber(invocationContext);
    return getJobNumberResponse.getJobNumber();
  }
}
