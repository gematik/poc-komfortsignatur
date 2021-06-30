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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.rezeps.InvocationContext;
import de.gematik.ws.conn.cardterminalinfo.v8.CardTerminalInfoType;
import de.gematik.ws.conn.cardterminalinfo.v8.CardTerminals;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.eventservice.v7.GetCardTerminalsResponse;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class CardTerminalsGetterTest {

  private static final String STATUS_OK = "OK";
  private static final String CT_ID = "dummy_ct_01";

  private static final String MANDANT = "mandant";
  private static final String CLIENT_SYSTEM = "client systen";
  private static final String WORKPLACE = "workplace";

  @Test
  public void shouldPerformGetCardTerminals() throws IOException {
    PerformGetCardTerminals performGetCardTerminals = mock(PerformGetCardTerminals.class);

    GetCardTerminalsResponse getCardTerminalsResponse = new GetCardTerminalsResponse();
    Status status = new Status();
    status.setResult(STATUS_OK);
    getCardTerminalsResponse.setStatus(status);

    CardTerminals cardTerminals = new CardTerminals();
    CardTerminalInfoType cardTerminalInfoType = new CardTerminalInfoType();
    cardTerminalInfoType.setCtId(CT_ID);
    cardTerminals.getCardTerminal().add(cardTerminalInfoType);
    getCardTerminalsResponse.setCardTerminals(cardTerminals);

    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    when(performGetCardTerminals.performGetCardTerminals(invocationContext))
        .thenReturn(getCardTerminalsResponse);

    CardTerminalsGetter cardTerminalsGetter = new CardTerminalsGetter();
    cardTerminalsGetter.performGetCardTerminals = performGetCardTerminals;

    GetCardTerminalsResult getCardTerminalsResult =
        cardTerminalsGetter.performGetCardTerminals(invocationContext);

    GetCardTerminalsResult getCardTerminalsResultExpected =
        new GetCardTerminalsResult(GetCardTerminalsResult.STATUS_OK, CT_ID);
    Assert.assertEquals(getCardTerminalsResultExpected, getCardTerminalsResult);
  }
}
