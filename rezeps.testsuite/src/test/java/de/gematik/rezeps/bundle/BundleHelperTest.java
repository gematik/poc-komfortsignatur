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

package de.gematik.rezeps.bundle;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xmlunit.matchers.CompareMatcher;

public class BundleHelperTest {

  private static final String PATH_TO_BUNDLE_WITH_PATIENT_DATA =
      "src/test/resources/bundle_template_with_patient.xml";
  private static final String PATH_TO_BUNDLE_WITH_COVERAGE_DATA =
      "src/test/resources/bundle_template_with_coverage.xml";
  private static final String PATH_TO_BUNDLE_WITH_MEDICATION_DATA =
      "src/test/resources/bundle_template_with_medication.xml";

  @Test
  public void shouldInitializePatientWithEmptyBundle()
      throws IOException, XPathExpressionException, SAXException, ParserConfigurationException,
          TransformerException {

    BundleHelper bundleHelper = new BundleHelper();

    Patient patient = initializePatient();
    String bundle = bundleHelper.initializePatientData(patient);

    String expectedBundle = readFileToString(PATH_TO_BUNDLE_WITH_PATIENT_DATA);
    assertThat(bundle, CompareMatcher.isIdenticalTo(expectedBundle));
  }

  private Patient initializePatient() {
    return new Patient(
        "Bart",
        "Simpson",
        "X081504711",
        "Evergreen Terrace",
        "742",
        "12345",
        "Springfield",
        "1990-04-01");
  }

  private String readFileToString(String pathToFile) throws IOException {
    return new String(Files.readAllBytes(Paths.get(pathToFile)), StandardCharsets.UTF_8);
  }

  @Test
  public void shouldInitializeCoverageWithEmptyBundle()
      throws ParserConfigurationException, SAXException, IOException, TransformerException,
          XPathExpressionException {

    BundleHelper bundleHelper = new BundleHelper();

    Coverage coverage = new Coverage("428154711", "Gesünder Leben", "3");
    String bundle = bundleHelper.initializeCoverageData(coverage);

    String expectedBundle = readFileToString(PATH_TO_BUNDLE_WITH_COVERAGE_DATA);
    assertThat(bundle, CompareMatcher.isIdenticalTo(expectedBundle));
  }

  @Test
  public void shouldInitializeMedicationWithEmptyBundle()
      throws ParserConfigurationException, SAXException, IOException, TransformerException,
          XPathExpressionException {

    BundleHelper bundleHelper = new BundleHelper();

    Medication medication = new Medication("11223344", "Anakinra 250ml");
    String bundle = bundleHelper.initializeMedicationData(medication);

    String expectedBundle = readFileToString(PATH_TO_BUNDLE_WITH_MEDICATION_DATA);
    assertThat(bundle, CompareMatcher.isIdenticalTo(expectedBundle));
  }
}
