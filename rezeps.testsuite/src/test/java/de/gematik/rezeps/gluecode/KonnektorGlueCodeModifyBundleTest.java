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

import static org.hamcrest.MatcherAssert.assertThat;

import de.gematik.rezeps.bundle.Coverage;
import de.gematik.rezeps.bundle.Medication;
import de.gematik.rezeps.bundle.Patient;
import de.gematik.rezeps.dataexchange.create.TaskCreateData;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.xml.transform.TransformerException;
import org.junit.Before;
import org.junit.Test;
import org.xmlunit.matchers.CompareMatcher;

public class KonnektorGlueCodeModifyBundleTest {

  private static final String PATH_TO_EXPECTED_BUNDLE =
      "src/test/resources/bundle_template_adapted_all.xml";
  private static final String PATH_TO_EXPECTED_BUNDLE_WITH_TASK =
      "src/test/resources/bundle_template_with_task.xml";

  @Before
  public void setup() {
    TestcaseData.getInstance().setBundle(null);
  }

  @Test
  public void shouldModifyBundleWithPatientCoverageAndMedication()
      throws TransformerException, IOException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    konnektorGlueCode.modifyBundleWithPatientData(initializePatient());
    Coverage coverage = new Coverage("428154711", "Gesünder Leben", "3");
    konnektorGlueCode.modifyBundleWithCoverageData(coverage);
    Medication medication = new Medication("11223344", "Anakinra 250ml");
    konnektorGlueCode.modifyBundleWithMedicationData(medication);

    String bundle = TestcaseData.getInstance().getBundle().readBundle();
    String expectedBundle = readFileToString(PATH_TO_EXPECTED_BUNDLE);

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
  public void shouldModifyBundleWithCoveragePatientAndMedication()
      throws TransformerException, IOException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    Coverage coverage = new Coverage("428154711", "Gesünder Leben", "3");
    konnektorGlueCode.modifyBundleWithCoverageData(coverage);
    konnektorGlueCode.modifyBundleWithPatientData(initializePatient());
    Medication medication = new Medication("11223344", "Anakinra 250ml");
    konnektorGlueCode.modifyBundleWithMedicationData(medication);

    String bundle = TestcaseData.getInstance().getBundle().readBundle();
    String expectedBundle = readFileToString(PATH_TO_EXPECTED_BUNDLE);

    assertThat(bundle, CompareMatcher.isIdenticalTo(expectedBundle));
  }

  @Test
  public void shouldModifyBundleWithMedicationPatientAndCoverage()
      throws TransformerException, IOException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);
    Medication medication = new Medication("11223344", "Anakinra 250ml");
    konnektorGlueCode.modifyBundleWithMedicationData(medication);
    konnektorGlueCode.modifyBundleWithPatientData(initializePatient());
    Coverage coverage = new Coverage("428154711", "Gesünder Leben", "3");
    konnektorGlueCode.modifyBundleWithCoverageData(coverage);

    String bundle = TestcaseData.getInstance().getBundle().readBundle();
    String expectedBundle = readFileToString(PATH_TO_EXPECTED_BUNDLE);

    assertThat(bundle, CompareMatcher.isIdenticalTo(expectedBundle));
  }

  @Test
  public void shouldModifyBundleWithTaskData()
      throws MissingPreconditionException, TransformerException, IOException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);

    TestcaseData.getInstance()
        .setTaskCreateData(new TaskCreateData("not important", "9876543210", "not important"));
    konnektorGlueCode.modifyBundleWithTaskData();

    String bundle = TestcaseData.getInstance().getBundle().readBundle();
    String expectedBundle = readFileToString(PATH_TO_EXPECTED_BUNDLE_WITH_TASK);

    assertThat(bundle, CompareMatcher.isIdenticalTo(expectedBundle));
  }

  @Test(expected = MissingPreconditionException.class)
  public void shouldThrowExceptionOnMissingTaks() throws MissingPreconditionException {
    KonnektorGlueCode konnektorGlueCode = new KonnektorGlueCode(null);

    TestcaseData.getInstance().setTaskCreateData(null);
    konnektorGlueCode.modifyBundleWithTaskData();
  }
}
