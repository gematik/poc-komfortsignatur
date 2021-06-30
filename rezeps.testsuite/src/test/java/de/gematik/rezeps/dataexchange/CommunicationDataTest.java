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

package de.gematik.rezeps.dataexchange;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.junit.Assert.*;

import de.gematik.rezeps.TestUtils;
import de.gematik.test.erezept.fd.fhir.adapter.CommunicationType;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class CommunicationDataTest {

  private static final String HR_PICKUP_CODE = "38471237410";
  private static final String DMC_PICKUP_CODE = "000000043423471237410325432h";
  private static final String TASK_REFERENCE =
      "/Task/2605/$accept?ac=777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea";

  @Test
  public void getHrPickupCode() {
    CommunicationData communicationData = new CommunicationData();
    Assert.assertEquals("", communicationData.getHrPickupCode());
  }

  @Test
  public void setHrPickupCode() {
    CommunicationData communicationData = new CommunicationData();
    communicationData.setHrPickupCode(HR_PICKUP_CODE);
    Assert.assertEquals(HR_PICKUP_CODE, communicationData.getHrPickupCode());
  }

  @Test
  public void getDmcCode() {
    CommunicationData communicationData = new CommunicationData();
    Assert.assertEquals("", communicationData.getDmcCode());
  }

  @Test
  public void setDmcCode() {
    CommunicationData communicationData = new CommunicationData();
    communicationData.setDmcCode(DMC_PICKUP_CODE);
    Assert.assertEquals(DMC_PICKUP_CODE, communicationData.getDmcCode());
  }

  @Test
  public void getTaskReference() {
    CommunicationData communicationData = new CommunicationData();
    Assert.assertNull(communicationData.getTaskReference());
  }

  @Test
  public void setTaskReference() {
    CommunicationData communicationData = new CommunicationData();
    communicationData.setTaskReference(TASK_REFERENCE);
    Assert.assertEquals(TASK_REFERENCE, communicationData.getTaskReference());
  }

  @Test
  public void getMessage() {
    CommunicationData communicationData = new CommunicationData();
    Assert.assertNull(communicationData.getMessage());
  }

  @Test
  public void setMessage() {
    CommunicationData communicationData = new CommunicationData();
    communicationData.setMessage("Hello World");
    Assert.assertEquals("Hello World", communicationData.getMessage());
  }

  @Test
  public void setType() {
    CommunicationData communicationData = new CommunicationData();
    communicationData.setType(CommunicationType.ERX_COMMUNICATION_DISP_REQ);
    Assert.assertEquals(CommunicationType.ERX_COMMUNICATION_DISP_REQ, communicationData.getType());
  }

  @Test
  public void getType() {
    CommunicationData communicationData = new CommunicationData();
    Assert.assertNull(communicationData.getType());
  }

  @Test
  public void setSender() {
    CommunicationData communicationData = new CommunicationData();
    communicationData.setSender("SENDER");
    Assert.assertEquals("SENDER", communicationData.getSender());
  }

  @Test
  public void getSender() {
    CommunicationData communicationData = new CommunicationData();
    Assert.assertNull(communicationData.getSender());
  }

  @Test
  public void getReplayContent() {
    Map<String, String> expectedMap = new HashMap<>();
    expectedMap.put("info_text", "MESSAGE");
    expectedMap.put("pickUpCodeHR", "");
    expectedMap.put("pickUpCodeDMC", "");
    CommunicationData communicationData = new CommunicationData();
    communicationData.setMessage("MESSAGE");
    Map<String, String> actualResponseContent = communicationData.getReplayContent();

    Assert.assertEquals("getReplayContent mismatch size", 3, actualResponseContent.size());
    Assert.assertEquals("invalid getReplayContent", is(expectedMap), is(actualResponseContent));

    Assert.assertTrue("maps are not equal", TestUtils.areEqual(expectedMap, actualResponseContent));
  }
}
