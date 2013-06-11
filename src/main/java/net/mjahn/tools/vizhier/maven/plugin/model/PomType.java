package net.mjahn.tools.vizhier.maven.plugin.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Mirko Jahn <mirkojahn@gmail.com>
 */
public enum PomType {
    POM("pom"), JAR("jar"), WAR("war"), PLUGIN("maven-plugin"), BUNDLE("bundle");
    
    private static final Map<String, PomType> lookup = new HashMap<String, PomType>();

    static {
        for (PomType s : EnumSet.allOf(PomType.class)) {
            lookup.put(s.getCode(), s);
        }
    }
    private String code;

    private PomType(String c) {
        code = c;
    }

    public String getCode() {
        return code;
    }

    public static PomType get(String code) {
        return lookup.get(code);
    }
}
