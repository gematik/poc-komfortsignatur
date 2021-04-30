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

package de.gematik.rezeps.fdclient;

import static de.gematik.idp.crypto.CryptoLoader.getIdentityFromP12;

import de.gematik.idp.client.IdpTokenResult;
import de.gematik.idp.client.MockIdpClient;
import de.gematik.idp.crypto.model.PkiIdentity;
import de.gematik.test.erezept.idp.I_IDPClient;
import de.gematik.test.erezept.keystore.Identity;
import de.gematik.test.resource.Resource;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;

public class IdpClientWrapper implements I_IDPClient {

    private final MockIdpClient mockIDPClient;
    private String accessToken;


    public IdpClientWrapper(){

        PkiIdentity serverIdentity =
                getIdentityFromP12(Resource.getResourceFileContentBytes("authenticatorModule_idpServer.p12"),
                        "00");

        mockIDPClient = MockIdpClient.builder()
                .serverIdentity(serverIdentity)
                .uriIdpServer("nicht relevant")
                .clientId("erp-testsuite-fd")
                .build();
        mockIDPClient.initialize();
    }

    @Override
    public String getBearerToken() {
        return accessToken;
    }

    @Override
    public void login(Identity identity) {
        PkiIdentity clientIdentity;
        try{
            clientIdentity = PkiIdentity.builder()
                    .certificate((X509Certificate) identity.getCertificate(identity.getAliasAuthenticate()))
                    .privateKey((PrivateKey) identity.getPrivateKey(identity.getAliasAuthenticate()))
                    .build();
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
        IdpTokenResult result = mockIDPClient.login(clientIdentity);

        accessToken = result.getAccessToken().getRawString();
    }

}
