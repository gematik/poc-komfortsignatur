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

package de.gematik.rezeps.cardterminal;

import de.gematik.rezeps.InvocationContext;
import de.gematik.ws.conn.cardterminalinfo.v8.CardTerminalInfoType;
import de.gematik.ws.conn.cardterminalinfo.v8.CardTerminals;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.eventservice.v7.GetCardTerminalsResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CardTerminalsGetter {

  @Autowired PerformGetCardTerminals performGetCardTerminals;

  /**
   * Bestimmt die CtId des ersten Kartenterminals, welches im Aufrufkontext sichtbar ist. Dazu wird
   * die Operation GetCardTerminals des Konnektors aufgerufen.
   *
   * @param invocationContext Der Kontext, der für den Aufruf beim Konnektor verwendet werden soll.
   * @return Das Ergebnis des Aufrufs.
   */
  public GetCardTerminalsResult performGetCardTerminals(InvocationContext invocationContext)
      throws IOException {

    GetCardTerminalsResponse getCardTerminalsResponse =
        performGetCardTerminals.performGetCardTerminals(invocationContext);

    GetCardTerminalsResult getCardTerminalsResult = new GetCardTerminalsResult();
    if (getCardTerminalsResponse != null) {

      Status status = getCardTerminalsResponse.getStatus();
      if (status != null) {
        getCardTerminalsResult.setStatus(status.getResult());
      }

      getCardTerminalsResult.setCtId(
          determineIdOfFirstTerminal(getCardTerminalsResponse.getCardTerminals()));
    }
    return getCardTerminalsResult;
  }

  private String determineIdOfFirstTerminal(CardTerminals cardTerminals) {
    String firstTerminalId = null;
    if (cardTerminals != null) {
      List<CardTerminalInfoType> cardTerminalInfos = cardTerminals.getCardTerminal();
      if (cardTerminalInfos != null && !cardTerminalInfos.isEmpty()) {
        firstTerminalId = cardTerminalInfos.get(0).getCtId();
      }
    }
    return firstTerminalId;
  }
}
