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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/** Prüft ob ein Task ein gewünschtes Ergebnis geliefert hat. */
public class TaskValidator {

  private static final Logger LOGGER = LoggerFactory.getLogger(TaskValidator.class);

  private static final int STAUS_CODE_CREATED = 201;
  private static final int STAUS_CODE_OK = 200;
  private static final String STATUS_DRAFT = "draft";
  private static final String STATUS_READY = "ready";
  private static final String STATUS_IN_PROGRESS = "in-progress";
  public static final String STATUS_CODE = "Status Code: {}";
  public static final String STATUS = "Status: {}";

  /**
   * Validiert die Antwort des Fachdienstes auf den Task create.
   *
   * @param taskCreateData Enthält die zu validierenden Daten.
   * @return True, falls die Antwort erfolgreich validiert werden konnte, andernfalls false.
   */
  public boolean validateTaskCreatedOk(TaskCreateData taskCreateData) {
    LOGGER.info("Validiere die Antwort auf Task create: ");

    LOGGER.info(STATUS_CODE, taskCreateData.getStatusCode());
    if (taskCreateData.getStatusCode() != STAUS_CODE_CREATED) {
      return false;
    }

    LOGGER.info("Task-ID: {}", taskCreateData.getTaskId());
    if (StringUtils.isEmpty(taskCreateData.getTaskId())) {
      return false;
    }

    LOGGER.info("Prescription-ID: {}", taskCreateData.getPrescriptionId());
    if (StringUtils.isEmpty(taskCreateData.getPrescriptionId())) {
      return false;
    }

    LOGGER.info("Access Code: {}", taskCreateData.getAccessCode());
    if (StringUtils.isEmpty(taskCreateData.getAccessCode())) {
      return false;
    }

    LOGGER.info(STATUS, taskCreateData.getStatus());

    return !StringUtils.isEmpty(taskCreateData.getStatus())
        && taskCreateData.getStatus().equals(STATUS_DRAFT);
  }

  /**
   * Validiert die Antwort des Fachdienstes auf den Task activate aus Sicht des verordnenden LEs.
   *
   * @param taskActivateData Enthält die zu validierenden Daten.
   * @return True, falls die Antwort erfolgreich validiert werden konnte, andernfalls false.
   */
  public boolean validateTaskActivateDataOk(TaskActivateData taskActivateData) {
    LOGGER.info("Validiere die Antwort auf Task activate aus Sicht des verordnenden LEs: ");
    LOGGER.info(STATUS_CODE, taskActivateData.getStatusCode());
    if (taskActivateData.getStatusCode() != STAUS_CODE_OK) {
      return false;
    }

    LOGGER.info(STATUS, taskActivateData.getStatus());
    return !StringUtils.isEmpty(taskActivateData.getStatus())
        && taskActivateData.getStatus().equals(STATUS_READY);
  }

  /**
   * Validiert die Antwort des Fachdienstes auf den Task accept aus Sicht des abgebenden LEs.
   *
   * @param taskAcceptData Enthält die zu validierenden Daten.
   * @return True, falls die Antwort erfolgreich validiert werden konnte, andernfalls false.
   */
  public boolean validateTaskAcceptDataOk(TaskAcceptData taskAcceptData) {
    LOGGER.info("Validiere die Antwort auf Task activate aus Sicht des abgebenden LEs: ");
    LOGGER.info(STATUS_CODE, taskAcceptData.getStatusCode());
    if (taskAcceptData.getStatusCode() != STAUS_CODE_OK) {
      return false;
    }

    LOGGER.info(STATUS, taskAcceptData.getStatus());
    if (StringUtils.isEmpty(taskAcceptData.getStatus())
        || !taskAcceptData.getStatus().equals(STATUS_IN_PROGRESS)) {
      return false;
    }

    LOGGER.info("Secret: {}", taskAcceptData.getSecret());
    if (StringUtils.isEmpty(taskAcceptData.getSecret())) {
      return false;
    }

    byte[] signedPrescription = taskAcceptData.getSignedPrescription();
    if (signedPrescription == null || signedPrescription.length == 0) {
      LOGGER.info("Es liegt kein signierter Verordnungsdatensatz vor.");
      return false;
    }

    LOGGER.info(
        "Es liegt ein signierter Verordnungsdatensatz der Länge {} vor.",
        signedPrescription.length);
    return true;
  }

  /**
   * Validiert die Antwort des Fachdienstes auf den Task close aus Sicht des abgebenden LEs.
   *
   * @param taskCloseData Enthält die zu validierenden Daten.
   * @return True, falls die Antwort erfolgreich validiert werden konnte, andernfalls false.
   */
  public boolean validateTaskCloseDataOk(TaskCloseData taskCloseData) {
    LOGGER.info("Validiere die Antwort auf Task close aus Sicht des abgebenden LEs: ");
    LOGGER.info(STATUS_CODE, taskCloseData.getStatusCode());

    if (taskCloseData.getStatusCode() != STAUS_CODE_OK) {
      return false;
    }

    byte[] signature = taskCloseData.getSignature();
    if (signature == null || signature.length == 0) {
      LOGGER.info("Es liegt keine Signatur vor.");
      return false;
    }
    LOGGER.info("Es liegt eine Signatur der Länge {} vor.", signature.length);

    return true;
  }
}
