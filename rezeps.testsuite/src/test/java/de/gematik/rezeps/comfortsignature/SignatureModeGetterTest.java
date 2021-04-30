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

package de.gematik.rezeps.comfortsignature;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.rezeps.InvocationContext;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.signatureservice.v7.ComfortSignatureStatusEnum;
import de.gematik.ws.conn.signatureservice.v7.GetSignatureModeResponse;
import de.gematik.ws.conn.signatureservice.v7.SessionInfo;
import de.gematik.ws.conn.signatureservice.v7.SignatureModeEnum;
import java.io.IOException;
import javax.xml.datatype.Duration;
import org.junit.Assert;
import org.junit.Test;

public class SignatureModeGetterTest {

  private static final String HBA_HANDLE = "hba_handle_1";
  private static final String MANDANT = "mandant";
  private static final String CLIENT_SYSTEM = "clientSystem";
  private static final String WORKPLACE = "";
  private static final String USER = "128 bit integer";
  private static final String STATUS_OK = "OK";

  @Test
  public void shouldDetermineSignatureMode() throws IOException {
    PerformGetSignatureMode performGetSignatureMode = mock(PerformGetSignatureMode.class);

    GetSignatureModeResponse getSignatureModeResponse = mock(GetSignatureModeResponse.class);
    Status status = new Status();
    status.setResult(STATUS_OK);
    when(getSignatureModeResponse.getStatus()).thenReturn(status);
    when(getSignatureModeResponse.getComfortSignatureStatus())
        .thenReturn(ComfortSignatureStatusEnum.ENABLED);
    SessionInfo sessionInfo = new SessionInfo();
    sessionInfo.setSignatureMode(SignatureModeEnum.COMFORT);
    sessionInfo.setCountRemaining(2);
    Duration durationMock = mock(Duration.class);
    sessionInfo.setTimeRemaining(durationMock);
    when(getSignatureModeResponse.getSessionInfo()).thenReturn(sessionInfo);

    InvocationContext invocationContext =
        new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE, USER);
    when(performGetSignatureMode.performGetSignatureMode(HBA_HANDLE, invocationContext))
        .thenReturn(getSignatureModeResponse);

    SignatureModeGetter signatureModeGetter = new SignatureModeGetter();
    signatureModeGetter.performGetSignatureMode = performGetSignatureMode;
    ComfortSignatureResult comfortSignatureResult =
        signatureModeGetter.determineSignatureMode(HBA_HANDLE, invocationContext);
    Assert.assertTrue(comfortSignatureResult.isComfortSignatureActivated());
  }

  @Test
  public void shouldDetermineSignatureModeIsComfort() throws IOException {
    PerformGetSignatureMode performGetSignatureMode = mock(PerformGetSignatureMode.class);

    GetSignatureModeResponse getSignatureModeResponse = mock(GetSignatureModeResponse.class);
    Status status = new Status();
    status.setResult(STATUS_OK);
    when(getSignatureModeResponse.getStatus()).thenReturn(status);
    when(getSignatureModeResponse.getComfortSignatureStatus())
        .thenReturn(ComfortSignatureStatusEnum.ENABLED);
    SessionInfo sessionInfo = new SessionInfo();
    sessionInfo.setSignatureMode(SignatureModeEnum.COMFORT);
    sessionInfo.setCountRemaining(2);
    Duration durationMock = mock(Duration.class);
    sessionInfo.setTimeRemaining(durationMock);
    when(getSignatureModeResponse.getSessionInfo()).thenReturn(sessionInfo);

    InvocationContext invocationContext =
        new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE, USER);
    when(performGetSignatureMode.performGetSignatureMode(HBA_HANDLE, invocationContext))
        .thenReturn(getSignatureModeResponse);

    SignatureModeGetter signatureModeGetter = new SignatureModeGetter();
    signatureModeGetter.performGetSignatureMode = performGetSignatureMode;
    ComfortSignatureResult comfortSignatureResult =
        signatureModeGetter.determineSignatureMode(HBA_HANDLE, invocationContext);
    Assert.assertTrue(comfortSignatureResult.isComfortSignatureActivated());
    Assert.assertEquals(
        SignatureModeEnum.COMFORT.value(), comfortSignatureResult.getSignatureMode());
  }

  @Test
  public void shouldDetermineSignatureModeIsPin() throws IOException {
    PerformGetSignatureMode performGetSignatureMode = mock(PerformGetSignatureMode.class);

    GetSignatureModeResponse getSignatureModeResponse = mock(GetSignatureModeResponse.class);
    Status status = new Status();
    status.setResult(STATUS_OK);
    when(getSignatureModeResponse.getStatus()).thenReturn(status);
    when(getSignatureModeResponse.getComfortSignatureStatus())
        .thenReturn(ComfortSignatureStatusEnum.ENABLED);
    // Wenn der Signatur-Modus PIN ist, ist das Objekt SessionInfo nicht gesetzt
    when(getSignatureModeResponse.getSessionInfo()).thenReturn(null);

    InvocationContext invocationContext =
        new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE, USER);
    when(performGetSignatureMode.performGetSignatureMode(HBA_HANDLE, invocationContext))
        .thenReturn(getSignatureModeResponse);

    SignatureModeGetter signatureModeGetter = new SignatureModeGetter();
    signatureModeGetter.performGetSignatureMode = performGetSignatureMode;
    ComfortSignatureResult comfortSignatureResult =
        signatureModeGetter.determineSignatureMode(HBA_HANDLE, invocationContext);
    Assert.assertEquals(SignatureModeEnum.PIN.value(), comfortSignatureResult.getSignatureMode());
    Assert.assertTrue(comfortSignatureResult.isDeactivateComfortSignature());
  }
}
