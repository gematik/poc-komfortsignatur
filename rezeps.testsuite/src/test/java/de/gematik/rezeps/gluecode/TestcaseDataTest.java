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

import de.gematik.rezeps.InvocationContext;
import de.gematik.rezeps.cucumber.stepdefinitions.KonnektorStepDefinitions;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import org.junit.*;
import org.slf4j.LoggerFactory;

/*
Tests der in TestcaseData verwendetet Methoden
*/
public class TestcaseDataTest {

  private static final String dummyDataFilePath =
      System.getProperty("java.io.tmpdir") + File.separatorChar + "ignoreMe.dat";
  private static final InvocationContext invocationContextExpected =
      new InvocationContext("mandant", "client-system", "workplace", "user");

  @After
  public void afterMethod() {
    try {
      Path path = Paths.get(dummyDataFilePath);
      boolean deleted = Files.deleteIfExists(path);
      LoggerFactory.getLogger(KonnektorStepDefinitions.class)
          .info(
              MessageFormat.format(
                  "After: dummyDataFilePath: \"{0}\" file deleted: {1} ",
                  dummyDataFilePath, deleted));
    } catch (IOException ioException) {
      throw new IllegalStateException(ioException.getMessage(), ioException);
    }
  }

  @Test
  public void serializeAndDeserializeToFileTest() {
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setAccessTokenPrescribingEntity("AccessTokenPrescribingEntity");
    testcaseData.setAccessTokenDispensingEntity("AccessTokenDispensingEntity");
    testcaseData.setInvocationContext(invocationContextExpected);
    testcaseData.serializeToFile(dummyDataFilePath);

    TestcaseData testcaseDataDeserialized = TestcaseData.getInstance();
    testcaseDataDeserialized.deserializeFromFile(dummyDataFilePath);
    Assert.assertEquals(
        "expected InvocationContext not equals to de-serialized",
        invocationContextExpected,
        testcaseDataDeserialized.getInvocationContext());
    Assert.assertEquals(
        "AccessTokenPrescribingEntity are not equal - de-serialization not correct?",
        "AccessTokenPrescribingEntity",
        testcaseDataDeserialized.getAccessTokenPrescribingEntity());
    Assert.assertEquals(
        "AccessTokenDispensingEntity are not equal - de-serialization not correct?",
        "AccessTokenDispensingEntity",
        testcaseDataDeserialized.getAccessTokenDispensingEntity());
  }

  @Test
  public void serializeToFileTest() {
    TestcaseData testcaseData = TestcaseData.getInstance();
    testcaseData.setAccessTokenPrescribingEntity("AccessTokenPrescribingEntity");
    testcaseData.setAccessTokenDispensingEntity("AccessTokenDispensingEntity");
    testcaseData.setInvocationContext(invocationContextExpected);
    testcaseData.serializeToFile(dummyDataFilePath);
    Path path = Paths.get(dummyDataFilePath);
    Assert.assertTrue(dummyDataFilePath + " ist keine Datei", path.toFile().isFile());
    File file = path.toFile();
    Assert.assertNotNull("file object ist null", file);
    Assert.assertTrue("file not readable ", file.canRead());
    Assert.assertTrue("file len should greater than 1 byte", file.length() > 1);
  }
}
