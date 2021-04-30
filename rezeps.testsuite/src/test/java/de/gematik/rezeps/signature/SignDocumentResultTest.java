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

package de.gematik.rezeps.signature;

import org.junit.Assert;
import org.junit.Test;

public class SignDocumentResultTest {

  private static final byte[] SIGNED_BUNDLE = new byte[] {0x01, 0x02, 0x03};

  @Test
  public void shouldBeValidResponse() {
    SignDocumentResult signDocumentResult =
        new SignDocumentResult(
            SignDocumentResult.STATUS_OK,
            SignDocumentResult.MIME_TYPE_BASE_64_DATA,
            SignDocumentResult.TYPE_BASE_64_SIGNATURE,
            SIGNED_BUNDLE);
    Assert.assertTrue(signDocumentResult.isValidResponse());
  }

  @Test
  public void shouldBeInvalidResponseOnNoStatus() {
    SignDocumentResult signDocumentResult =
        new SignDocumentResult(
            null,
            SignDocumentResult.MIME_TYPE_BASE_64_DATA,
            SignDocumentResult.TYPE_BASE_64_SIGNATURE,
            SIGNED_BUNDLE);
    Assert.assertFalse(signDocumentResult.isValidResponse());
  }

  @Test
  public void shouldBeInvalidResponseOnInvalidStatus() {
    SignDocumentResult signDocumentResult =
        new SignDocumentResult(
            "Warning",
            SignDocumentResult.MIME_TYPE_BASE_64_DATA,
            SignDocumentResult.TYPE_BASE_64_SIGNATURE,
            SIGNED_BUNDLE);
    Assert.assertFalse(signDocumentResult.isValidResponse());
  }

  @Test
  public void shouldBeValidResponseOnNoMimeTypeOfBase64Data() {
    SignDocumentResult signDocumentResult =
        new SignDocumentResult(
            SignDocumentResult.STATUS_OK,
            null,
            SignDocumentResult.TYPE_BASE_64_SIGNATURE,
            SIGNED_BUNDLE);
    Assert.assertTrue(signDocumentResult.isValidResponse());
  }

  @Test
  public void shouldBeInvalidResponseOnWrongMimeTypeOfBase64Data() {
    SignDocumentResult signDocumentResult =
        new SignDocumentResult(
            SignDocumentResult.STATUS_OK,
            "invalid Mime-Type",
            SignDocumentResult.TYPE_BASE_64_SIGNATURE,
            SIGNED_BUNDLE);
    Assert.assertFalse(signDocumentResult.isValidResponse());
  }

  @Test
  public void shouldBeInvalidResponseOnInvalidMimeTypeOfBase64Data() {
    SignDocumentResult signDocumentResult =
        new SignDocumentResult(
            SignDocumentResult.STATUS_OK,
            "invalid mime type",
            SignDocumentResult.TYPE_BASE_64_SIGNATURE,
            SIGNED_BUNDLE);
    Assert.assertFalse(signDocumentResult.isValidResponse());
  }

  @Test
  public void shouldBeInvalidResponseOnNoTypeOfBase64Signature() {
    SignDocumentResult signDocumentResult =
        new SignDocumentResult(
            SignDocumentResult.STATUS_OK,
            SignDocumentResult.MIME_TYPE_BASE_64_DATA,
            null,
            SIGNED_BUNDLE);
    Assert.assertFalse(signDocumentResult.isValidResponse());
  }

  @Test
  public void shouldBeInvalidResponseOnInvalidTypeOfBase64Signature() {
    SignDocumentResult signDocumentResult =
        new SignDocumentResult(
            SignDocumentResult.STATUS_OK,
            SignDocumentResult.MIME_TYPE_BASE_64_DATA,
            "invalid type",
            SIGNED_BUNDLE);
    Assert.assertFalse(signDocumentResult.isValidResponse());
  }

  @Test
  public void shouldBeInvalidResponseOnNoSignedBundle() {
    SignDocumentResult signDocumentResult =
        new SignDocumentResult(
            SignDocumentResult.STATUS_OK,
            SignDocumentResult.MIME_TYPE_BASE_64_DATA,
            SignDocumentResult.TYPE_BASE_64_SIGNATURE,
            null);
    Assert.assertFalse(signDocumentResult.isValidResponse());
  }

  @Test
  public void shouldBeInvalidResponseOnEmptySignedBundle() {
    SignDocumentResult signDocumentResult =
        new SignDocumentResult(
            SignDocumentResult.STATUS_OK,
            SignDocumentResult.MIME_TYPE_BASE_64_DATA,
            SignDocumentResult.TYPE_BASE_64_SIGNATURE,
            new byte[] {});
    Assert.assertFalse(signDocumentResult.isValidResponse());
  }
}
