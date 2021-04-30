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

import de.gematik.rezeps.bundle.BundleHelper;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.junit.Test;
import org.xml.sax.SAXException;

public class KonnektorGlueCodePreconditionsTest {

  private static final String CARD_HANDLE = "hba_handle001";
  private static final String JOB_NUMBER = "4711";

  @Test(expected = MissingPreconditionException.class)
  public void shouldThrowMissingPreconditionExceptionForCardHandle()
      throws MissingPreconditionException, ParserConfigurationException, SAXException, IOException,
          TransformerException {

    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setHbaHandle(null);
    testcaseData.setBundle(new BundleHelper());
    testcaseData.setJobNumber(JOB_NUMBER);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.checkPreconditions();
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldThrowMissingPreconditionExceptionForPrescription()
      throws MissingPreconditionException, TransformerException {

    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setHbaHandle(CARD_HANDLE);
    testcaseData.setBundle(null);
    testcaseData.setJobNumber(JOB_NUMBER);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.checkPreconditions();
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldThrowMissingPreconditionExceptionForJobNumber()
      throws MissingPreconditionException, ParserConfigurationException, SAXException, IOException,
          TransformerException {

    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setHbaHandle(CARD_HANDLE);
    testcaseData.setBundle(new BundleHelper());
    testcaseData.setJobNumber(null);

    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.checkPreconditions();
  }
}
