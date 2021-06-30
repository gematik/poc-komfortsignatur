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

package de.gematik.rezeps.fd;

import de.gematik.rezeps.dataexchange.TaskAcceptData;
import de.gematik.rezeps.dataexchange.TaskActivateData;
import de.gematik.rezeps.dataexchange.TaskCloseData;
import de.gematik.rezeps.dataexchange.TaskCreateData;
import java.nio.charset.StandardCharsets;
import org.junit.Assert;
import org.junit.Test;

public class TaskValidatorTest {

  private static final String STATUS_IN_PROGRESS = "in-progress";
  private static final int STATUS_CODE_OK = 200;

  @Test
  public void shouldValidateTaskCreated() {
    TaskCreateData taskCreateData =
        new TaskCreateData(201, "4711", "prescription.0.8.15", "access-code", "draft");
    TaskValidator taskValidator = new TaskValidator();
    Assert.assertTrue(taskValidator.validateTaskCreatedOk(taskCreateData));
  }

  @Test
  public void shouldNotValidateTaskCreatedOnWrongStatusCode() {
    TaskCreateData taskCreateData =
        new TaskCreateData(401, "4711", "prescription.0.8.15", "access-code", "draft");
    TaskValidator taskValidator = new TaskValidator();
    Assert.assertFalse(taskValidator.validateTaskCreatedOk(taskCreateData));
  }

  @Test
  public void shouldNotValidateTaskCreatedOnMissingTaskId() {
    TaskCreateData taskCreateData =
        new TaskCreateData(201, "", "prescription.0.8.15", "access-code", "draft");
    TaskValidator taskValidator = new TaskValidator();
    Assert.assertFalse(taskValidator.validateTaskCreatedOk(taskCreateData));
  }

  @Test
  public void shouldNotValidateTaskCreatedOnMissingPrescriptionId() {
    TaskCreateData taskCreateData = new TaskCreateData(201, "4711", null, "access-code", "draft");
    TaskValidator taskValidator = new TaskValidator();
    Assert.assertFalse(taskValidator.validateTaskCreatedOk(taskCreateData));
  }

  @Test
  public void shouldNotValidateTaskCreatedOnMissingAccessCode() {
    TaskCreateData taskCreateData =
        new TaskCreateData(201, "4711", "prescription.0.8.15", "", "draft");
    TaskValidator taskValidator = new TaskValidator();
    Assert.assertFalse(taskValidator.validateTaskCreatedOk(taskCreateData));
  }

  @Test
  public void shouldNotValidateTaskCreatedOnWrongStatus() {
    TaskCreateData taskCreateData =
        new TaskCreateData(201, "4711", "prescription.0.8.15", "access-code", "final");
    TaskValidator taskValidator = new TaskValidator();
    Assert.assertFalse(taskValidator.validateTaskCreatedOk(taskCreateData));
  }

  @Test
  public void shouldNotValidateTaskCreatedOnMissingStatus() {
    TaskCreateData taskCreateData =
        new TaskCreateData(201, "4711", "prescription.0.8.15", "access-code", null);
    TaskValidator taskValidator = new TaskValidator();
    Assert.assertFalse(taskValidator.validateTaskCreatedOk(taskCreateData));
  }

  @Test
  public void shouldValidateTaskActivateData() {
    TaskActivateData taskActivateData = new TaskActivateData(STATUS_CODE_OK, "ready");
    TaskValidator taskValidator = new TaskValidator();
    Assert.assertTrue(taskValidator.validateTaskActivateDataOk(taskActivateData));
  }

  @Test
  public void shouldNotValidateTaskActivateDataOnWrongStatusCode() {
    TaskActivateData taskActivateData = new TaskActivateData(401, "ready");
    TaskValidator taskValidator = new TaskValidator();
    Assert.assertFalse(taskValidator.validateTaskActivateDataOk(taskActivateData));
  }

  @Test
  public void shouldNotValidateTaskActivateDataOnWrongStatus() {
    TaskActivateData taskActivateData = new TaskActivateData(STATUS_CODE_OK, "draft");
    TaskValidator taskValidator = new TaskValidator();
    Assert.assertFalse(taskValidator.validateTaskActivateDataOk(taskActivateData));
  }

  @Test
  public void shouldNotValidateTaskActivateDataOnMissingStatus() {
    TaskActivateData taskActivateData = new TaskActivateData(STATUS_CODE_OK, "");
    TaskValidator taskValidator = new TaskValidator();
    Assert.assertFalse(taskValidator.validateTaskActivateDataOk(taskActivateData));
  }

  @Test
  public void shouldValidateTaskAcceptData() {
    TaskAcceptData taskAcceptData = new TaskAcceptData(STATUS_CODE_OK, STATUS_IN_PROGRESS);
    taskAcceptData.setSecret("secret");
    taskAcceptData.setSignedPrescription("signed prescription".getBytes(StandardCharsets.UTF_8));
    TaskValidator taskValidator = new TaskValidator();
    Assert.assertTrue(taskValidator.validateTaskAcceptDataOk(taskAcceptData));
  }

  @Test
  public void shouldNotValidateTaskAcceptDataOnWrongStatusCode() {
    TaskAcceptData taskAcceptData = new TaskAcceptData(401, "in-progress");
    taskAcceptData.setSecret("secret");
    taskAcceptData.setSignedPrescription("signed prescription".getBytes(StandardCharsets.UTF_8));
    TaskValidator taskValidator = new TaskValidator();
    Assert.assertFalse(taskValidator.validateTaskAcceptDataOk(taskAcceptData));
  }

  @Test
  public void shouldNotValidateTaskAcceptDataOnWrongStatus() {
    TaskAcceptData taskAcceptData = new TaskAcceptData(STATUS_CODE_OK, "draft");
    taskAcceptData.setSecret("secret");
    taskAcceptData.setSignedPrescription("signed prescription".getBytes(StandardCharsets.UTF_8));
    TaskValidator taskValidator = new TaskValidator();
    Assert.assertFalse(taskValidator.validateTaskAcceptDataOk(taskAcceptData));
  }

  @Test
  public void shouldNotValidateTaskAcceptDataOnMissingStatus() {
    TaskAcceptData taskAcceptData = new TaskAcceptData(STATUS_CODE_OK, "");
    taskAcceptData.setSecret("secret");
    taskAcceptData.setSignedPrescription("signed prescription".getBytes(StandardCharsets.UTF_8));
    TaskValidator taskValidator = new TaskValidator();
    Assert.assertFalse(taskValidator.validateTaskAcceptDataOk(taskAcceptData));
  }

  @Test
  public void shouldNotValidateTaskAcceptDataOnMissingSecret() {
    TaskAcceptData taskAcceptData = new TaskAcceptData(STATUS_CODE_OK, STATUS_IN_PROGRESS);
    taskAcceptData.setSignedPrescription("signed prescription".getBytes(StandardCharsets.UTF_8));
    TaskValidator taskValidator = new TaskValidator();
    Assert.assertFalse(taskValidator.validateTaskAcceptDataOk(taskAcceptData));
  }

  @Test
  public void shouldNotValidateTaskAcceptDataOnMissingSignedPrescription() {
    TaskAcceptData taskAcceptData = new TaskAcceptData(STATUS_CODE_OK, STATUS_IN_PROGRESS);
    taskAcceptData.setSecret("secret");
    TaskValidator taskValidator = new TaskValidator();
    Assert.assertFalse(taskValidator.validateTaskAcceptDataOk(taskAcceptData));
  }

  @Test
  public void shouldNotValidateTaskAcceptDataOnEmptySignedPrescription() {
    TaskAcceptData taskAcceptData = new TaskAcceptData(STATUS_CODE_OK, STATUS_IN_PROGRESS);
    taskAcceptData.setSecret("secret");
    taskAcceptData.setSignedPrescription(new byte[] {});
    TaskValidator taskValidator = new TaskValidator();
    Assert.assertFalse(taskValidator.validateTaskAcceptDataOk(taskAcceptData));
  }

  @Test
  public void shouldValidateTaskCloseData() {
    TaskCloseData taskCloseData =
        new TaskCloseData(
            STATUS_CODE_OK, "ich bin eine signierte Quittung".getBytes(StandardCharsets.UTF_8));
    TaskValidator taskValidator = new TaskValidator();
    Assert.assertTrue(taskValidator.validateTaskCloseDataOk(taskCloseData));
  }

  @Test
  public void shouldNotValidateTaskCloseDataOnWrongStatusCode() {
    TaskCloseData taskCloseData =
        new TaskCloseData(400, "ich bin eine signierte Quittung".getBytes(StandardCharsets.UTF_8));
    TaskValidator taskValidator = new TaskValidator();
    Assert.assertFalse(taskValidator.validateTaskCloseDataOk(taskCloseData));
  }

  @Test
  public void shouldNotValidateTaskCloseDataOnMissingSignature() {
    TaskCloseData taskCloseData = new TaskCloseData(STATUS_CODE_OK, null);
    TaskValidator taskValidator = new TaskValidator();
    Assert.assertFalse(taskValidator.validateTaskCloseDataOk(taskCloseData));
  }

  @Test
  public void shouldNotValidateTaskCloseDataOnEmptySignature() {
    TaskCloseData taskCloseData = new TaskCloseData(STATUS_CODE_OK, new byte[] {});
    TaskValidator taskValidator = new TaskValidator();
    Assert.assertFalse(taskValidator.validateTaskCloseDataOk(taskCloseData));
  }
}
