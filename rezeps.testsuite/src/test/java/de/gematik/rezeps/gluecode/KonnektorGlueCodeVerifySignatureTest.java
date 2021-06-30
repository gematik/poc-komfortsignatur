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

package de.gematik.rezeps.gluecode;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.rezeps.InvocationContext;
import de.gematik.rezeps.dataexchange.TaskAcceptData;
import de.gematik.rezeps.signature.SignatureVerification;
import de.gematik.rezeps.signature.SignatureVerificationResult;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;

public class KonnektorGlueCodeVerifySignatureTest {

  private static final String MANDANT = "mandant001";
  private static final String CLIENT_SYSTEM = "client_system001";
  private static final String WORKPLACE = "workplace001";
  private static final byte[] SIGNED_PRESCRIPTION =
      "Signierte Beschreibung eines wirksamen Mendikamentes".getBytes();
  private static final String STATUS_OK = "OK";
  private static final String HIGH_LEVEL_RESULT_VALID = "VALID";

  private InvocationContext invocationContext = null;

  @Before
  public void beforeMethod() {
    invocationContext = new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE);
    TestcaseData.getInstance().setInvocationContext(invocationContext);
  }

  @Test
  public void shouldVerifySignature() throws IOException, MissingPreconditionException {
    ConfigurableApplicationContext connfigurableApplicationContext =
        mock(ConfigurableApplicationContext.class);
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(connfigurableApplicationContext);

    SignatureVerification signatureVerification = mock(SignatureVerification.class);
    ConfigurableListableBeanFactory beanFactory = mock(ConfigurableListableBeanFactory.class);
    when(beanFactory.getBean(SignatureVerification.class)).thenReturn(signatureVerification);
    when(connfigurableApplicationContext.getBeanFactory()).thenReturn(beanFactory);

    SignatureVerificationResult signatureVerificationResult =
        new SignatureVerificationResult("OK", "VALID");
    when(signatureVerification.verifySignature(invocationContext, SIGNED_PRESCRIPTION))
        .thenReturn(signatureVerificationResult);

    TaskAcceptData taskAcceptData = mock(TaskAcceptData.class);
    when(taskAcceptData.getSignedPrescription()).thenReturn(SIGNED_PRESCRIPTION);

    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setTaskAcceptData(taskAcceptData);
    konnektorGlueCode.verifySignature();
    Assert.assertEquals(signatureVerificationResult, testcaseData.getSignatureVerificationResult());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldThrowExceptionOnMissingSignedPrescription()
      throws MissingPreconditionException {
    ConfigurableApplicationContext connfigurableApplicationContext =
        mock(ConfigurableApplicationContext.class);
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(connfigurableApplicationContext);

    SignatureVerification signatureVerification = mock(SignatureVerification.class);
    ConfigurableListableBeanFactory beanFactory = mock(ConfigurableListableBeanFactory.class);
    when(beanFactory.getBean(SignatureVerification.class)).thenReturn(signatureVerification);
    when(connfigurableApplicationContext.getBeanFactory()).thenReturn(beanFactory);

    TestcaseData.getInstance().setTaskAcceptData(null);
    konnektorGlueCode.verifySignature();
  }

  @Test
  public void shouldTestValidSignature() throws MissingPreconditionException {
    ConfigurableApplicationContext connfigurableApplicationContext =
        mock(ConfigurableApplicationContext.class);
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(connfigurableApplicationContext);

    SignatureVerificationResult signatureVerificationResult =
        new SignatureVerificationResult(STATUS_OK, HIGH_LEVEL_RESULT_VALID);
    TestcaseData.getInstance().setSignatureVerificationResult(signatureVerificationResult);
    Assert.assertTrue(konnektorGlueCode.isValidSignature());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldThrowExceptionOnMissingSignatureVerificationResult()
      throws MissingPreconditionException {
    ConfigurableApplicationContext connfigurableApplicationContext =
        mock(ConfigurableApplicationContext.class);
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(connfigurableApplicationContext);
    TestcaseData.getInstance().setSignatureVerificationResult(null);
    konnektorGlueCode.isValidSignature();
  }
}
