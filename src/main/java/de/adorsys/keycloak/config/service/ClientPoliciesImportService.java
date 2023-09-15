/*-
 * ---license-start
 * keycloak-config-cli
 * ---
 * Copyright (C) 2017 - 2021 adorsys GmbH & Co. KG @ https://adorsys.com
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package de.adorsys.keycloak.config.service;

import de.adorsys.keycloak.config.model.RealmImport;
import de.adorsys.keycloak.config.repository.ClientPoliciesRepository;
import org.keycloak.representations.idm.ClientPoliciesRepresentation;
import org.keycloak.representations.idm.ClientProfilesRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientPoliciesImportService {

    private static final Logger logger = LoggerFactory.getLogger(ClientPoliciesImportService.class);

    private final ClientPoliciesRepository clientPoliciesRepository;

    @Autowired
    public ClientPoliciesImportService(ClientPoliciesRepository clientPoliciesRepository) {
        this.clientPoliciesRepository = clientPoliciesRepository;
    }

    public void doImport(RealmImport realmImport) {

        // client-profile profiles must be imported before client-profile policies
        ClientProfilesRepresentation parsedClientProfiles = realmImport.getParsedClientProfiles();
        clientPoliciesRepository.updateClientPoliciesProfiles(realmImport, parsedClientProfiles);
        logger.trace("Updated client-policy profiles.");

        ClientPoliciesRepresentation parsedClientPolicies = realmImport.getParsedClientPolicies();
        clientPoliciesRepository.updateClientPoliciesPolicies(realmImport, parsedClientPolicies);
        logger.trace("Updated client-policy policies.");
    }
}
