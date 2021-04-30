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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.rezeps.InvocationContext;
import de.gematik.rezeps.bundle.BundleHelper;
import de.gematik.rezeps.signature.PrescriptionSigner;
import de.gematik.rezeps.signature.SignDocumentResult;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.xml.sax.SAXException;

public class KonnektorGlueCodeSignPrescriptionTest {

  private static final String MANDANT = "mandant001";
  private static final String CLIENT_SYSTEM = "client_system001";
  private static final String WORKPLACE = "workplace001";
  private static final String USER = "user001";
  private static final String CARD_HANDLE = "hba_handle001";
  private static final byte[] SIGNED_PRESCRIPTION =
      "Signierte Beschreibung eines wirksamen Mendikamentes".getBytes();
  private static final String JOB_NUMBER = "4711";

  @Test
  public void shouldSignPrescription()
      throws MissingPreconditionException, IOException, ParserConfigurationException, SAXException {
    ConfigurableApplicationContext connfigurableApplicationContext =
        mock(ConfigurableApplicationContext.class);

    PrescriptionSigner prescriptionSigner = mock(PrescriptionSigner.class);

    ConfigurableListableBeanFactory beanFactory = mock(ConfigurableListableBeanFactory.class);
    when(beanFactory.getBean(PrescriptionSigner.class)).thenReturn(prescriptionSigner);
    when(connfigurableApplicationContext.getBeanFactory()).thenReturn(beanFactory);

    InvocationContext invocationContext =
        new InvocationContext(MANDANT, CLIENT_SYSTEM, WORKPLACE, USER);

    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setHbaHandle(CARD_HANDLE);
    testcaseData.setBundle(new BundleHelper());
    testcaseData.setJobNumber(JOB_NUMBER);
    testcaseData.setInvocationContext(invocationContext);
    SignDocumentResult signDocumentResult =
        new SignDocumentResult(
            SignDocumentResult.STATUS_OK,
            SignDocumentResult.MIME_TYPE_BASE_64_DATA,
            SignDocumentResult.MIME_TYPE_BASE_64_DATA,
            SIGNED_PRESCRIPTION);
    when(prescriptionSigner.performSignPrescription(
            eq(invocationContext), eq(CARD_HANDLE), anyString(), eq(JOB_NUMBER)))
        .thenReturn(signDocumentResult);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(connfigurableApplicationContext);
    konnektorGlueCode.signPrescription();

    Assert.assertEquals(
        SIGNED_PRESCRIPTION, testcaseData.getSignDocumentResult().getSignedBundle());
  }

  @Test
  public void shouldVerifySignPrescription() throws MissingPreconditionException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);

    SignDocumentResult signDocumentResult =
        new SignDocumentResult(
            SignDocumentResult.STATUS_OK,
            SignDocumentResult.MIME_TYPE_BASE_64_DATA,
            SignDocumentResult.TYPE_BASE_64_SIGNATURE,
            SIGNED_PRESCRIPTION);
    TestcaseData.getInstance().setSignDocumentResult(signDocumentResult);
    Assert.assertTrue(konnektorGlueCode.verifySignPrescription());
  }

  @Test
  public void shouldNotVerifySignPrescription() throws MissingPreconditionException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);

    SignDocumentResult signDocumentResult =
        new SignDocumentResult(
            "Warning",
            SignDocumentResult.MIME_TYPE_BASE_64_DATA,
            SignDocumentResult.TYPE_BASE_64_SIGNATURE,
            SIGNED_PRESCRIPTION);
    TestcaseData.getInstance().setSignDocumentResult(signDocumentResult);
    Assert.assertFalse(konnektorGlueCode.verifySignPrescription());
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldThrowExceptionOnMissingSignDocumentResult()
      throws MissingPreconditionException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);

    TestcaseData.getInstance().setSignDocumentResult(null);
    Assert.assertFalse(konnektorGlueCode.verifySignPrescription());
  }
}
