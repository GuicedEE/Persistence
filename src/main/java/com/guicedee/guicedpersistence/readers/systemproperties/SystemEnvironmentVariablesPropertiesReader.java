package com.guicedee.guicedpersistence.readers.systemproperties;

import com.google.common.base.Strings;
import com.guicedee.guicedinjection.properties.GlobalProperties;
import com.guicedee.guicedpersistence.services.IPropertiesEntityManagerReader;
import lombok.extern.java.Log;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A default connection string builder for H2 Databases
 */
@Log
public class SystemEnvironmentVariablesPropertiesReader
        implements IPropertiesEntityManagerReader {
    private static final String pat = "\\$\\{([a-zA-Z_\\-\\.]*)\\}";
    private static final Pattern pattern = Pattern.compile(pat);
    
    @Override
    public Map<String, String> processProperties(ParsedPersistenceXmlDescriptor persistenceUnit, Properties incomingProperties) {
        for (String prop : incomingProperties.stringPropertyNames()) {
            String value = incomingProperties.getProperty(prop);
            String valueNew = incomingProperties.getProperty(prop);
            Matcher matcher = pattern.matcher(value);

            for (MatchResult a : matcher.results().collect(Collectors.toList())) {
                String matchedText = a.group(0);
                matchedText = matchedText.replace("$", "\\$");
                matchedText = matchedText.replace("{", "\\{");
                matchedText = matchedText.replace("}", "\\}");
                String searchProperty = a.group(1);
                String systemProperty = GlobalProperties.getSystemPropertyOrEnvironment(searchProperty, matchedText);
                if (searchProperty.equals(matchedText)) {
                    log.warning("Missing System Property - " + matchedText);
                } else {
                    valueNew = valueNew.replaceAll(matchedText, systemProperty);
                }
            }
            if (!Strings.isNullOrEmpty(valueNew)) {
                incomingProperties.put(prop, valueNew);
            }
        }
        return new HashMap<>();
    }
    
    @Override
    public boolean applicable(ParsedPersistenceXmlDescriptor persistenceUnit)
    {
        return true;
    }
}
