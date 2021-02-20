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

package de.adorsys.keycloak.config.service.print;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import de.adorsys.keycloak.config.exception.ImportProcessingException;
import de.adorsys.keycloak.config.model.RealmImport;
import de.adorsys.keycloak.config.repository.RealmRepository;
import org.keycloak.representations.idm.RealmRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

@Service
public class PrintDiffService {
    private static final Logger logger = LoggerFactory.getLogger(PrintDiffService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper(
            new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
    );

    private final RealmRepository realmRepository;

    @Autowired
    public PrintDiffService(RealmRepository realmRepository) {
        this.realmRepository = realmRepository;
    }

    public void print(RealmImport realmImport) {
        String importYaml;
        String existingYaml;

        RealmRepresentation existingRealm = realmRepository
                .partialExport(realmImport.getRealm(), true, true);

        try {
            importYaml = objectMapper.writeValueAsString(realmImport);
            existingYaml = objectMapper.writeValueAsString(existingRealm);
        } catch (JsonProcessingException e) {
            throw new ImportProcessingException(e);
        }

        DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(false)
                .inlineDiffByWord(false)
                .oldTag(f -> "~")
                .newTag(f -> "**")
                .build();

        List<DiffRow> rows = generator.generateDiffRows(
                Collections.singletonList(existingYaml),
                Collections.singletonList(importYaml)
        );

        try (PrintWriter out = new PrintWriter("filename.txt")) {
            rows.forEach(out::println);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
