package com.skillproof.backend;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class ArchitectureBoundaryTest {
    @Test void modulesUseContractsInsteadOfAnotherModulesImplementation() throws Exception {
        Path root=Path.of("src/main/java/com/skillproof/backend");
        var violations=new ArrayList<String>();
        var implementation=Pattern.compile("com\\.skillproof\\.backend\\.(\\w+)\\.(application|infrastructure|api)\\.[\\w.]+");
        try (var paths=Files.walk(root)) {
            for (var file:paths.filter(p->p.toString().endsWith(".java")).toList()) {
                String owner=root.relativize(file).getName(0).toString();
                if (owner.equals("config") || owner.equals("common")) continue;
                var matches=implementation.matcher(Files.readString(file));
                while (matches.find()) {
                    if (!owner.equals(matches.group(1)) && !matches.group(1).equals("common"))
                        violations.add(root.relativize(file)+" -> "+matches.group());
                }
            }
        }
        assertTrue(violations.isEmpty(),violations.toString());
    }
}
