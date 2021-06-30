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
import de.gematik.ws.conn.cardterminalservice.v1.EjectCardResponse;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class CardEjectorTest {

  private static final String MANDANT = "mandant";
  private static final String CLIENT_SYSTEM = "client systen";
  private static final String WORKPLACE = "workplace";
  private static final String USER = "user";
  public static final String CARD_HANDLE = "HBA-1";

  private static final String STATUS_OK = "OK";

  @Test
  public void shouldPerformEjectCard() throws IOException {
    PerformEjectCard performEjectCard = mock(PerformEjectCard.class);
    EjectCardResponse ejectCardResponse = new EjectCardResponse();
    Status status = new Status();
    status.setResult(STATUS_OK);
    ejectCardResponse.setStatus(status);
    InvocationContext invocationContext =
        new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE, USER);
    when(performEjectCard.performEjectCard(invocationContext, CARD_HANDLE))
        .thenReturn(ejectCardResponse);

    CardEjector cardEjector = new CardEjector();
    cardEjector.performEjectCard = performEjectCard;
    EjectCardResult ejectCadResult = cardEjector.performEjectCard(invocationContext, CARD_HANDLE);

    EjectCardResult ejectCardResultExpected = new EjectCardResult(STATUS_OK);
    Assert.assertEquals(ejectCardResultExpected, ejectCadResult);
  }
}
