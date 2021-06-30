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
import de.gematik.ws.conn.cardservice.v8.CardInfoType;
import de.gematik.ws.conn.cardterminalservice.v1.RequestCardResponse;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class CardRequestorTest {

  private static final String MANDANT = "mandant";
  private static final String CLIENT_SYSTEM = "client systen";
  private static final String WORKPLACE = "workplace";
  private static final String CT_ID = "dummy_ct_id";
  private static final int SLOT = 3;
  private static final String STATUS_OK = "OK";
  private static final String CARD_HANDLE = "dummy_card_handle";

  @Test
  public void shouldPerformRequestCard() throws IOException {
    PerformRequestCard performRequestCard = mock(PerformRequestCard.class);

    InvocationContext invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);

    RequestCardResponse requestCardResponse = new RequestCardResponse();

    Status status = new Status();
    status.setResult(STATUS_OK);
    requestCardResponse.setStatus(status);

    CardInfoType cardInfoType = new CardInfoType();
    cardInfoType.setCardHandle(CARD_HANDLE);
    requestCardResponse.setCard(cardInfoType);

    when(performRequestCard.performRequestCard(invocationContext, CT_ID, SLOT))
        .thenReturn(requestCardResponse);

    CardRequestor cardRequestor = new CardRequestor();
    cardRequestor.performRequestCard = performRequestCard;
    RequestCardResult requestCardResult =
        cardRequestor.performRequestCard(invocationContext, CT_ID, SLOT);

    RequestCardResult expectedRequestCardResult = new RequestCardResult(STATUS_OK, CARD_HANDLE);
    Assert.assertEquals(expectedRequestCardResult, requestCardResult);
  }
}
