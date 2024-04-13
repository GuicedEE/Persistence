package com.guicedee.guicedpersistence.readers.systemproperties;

import com.google.common.base.Strings;
import com.guicedee.guicedpersistence.services.IPropertiesEntityManagerReader;
import lombok.extern.java.Log;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.guicedee.client.Environment.getProperty;

/**
 * A default connection string builder for H2 Databases
 */
@Log
public class SystemEnvironmentVariablesPropertiesReader
        implements IPropertiesEntityManagerReader
{
    private static final String pat = "\\$\\{([a-zA-Z_\\-\\.]*)\\:?(.*)?\\}$";
    private static final Pattern pattern = Pattern.compile(pat);

    @Override
    public Map<String, String> processProperties(ParsedPersistenceXmlDescriptor persistenceUnit, Properties incomingProperties)
    {
        for (String prop : incomingProperties.stringPropertyNames())
        {
            String value = incomingProperties.getProperty(prop);
            String valueNew;
            Matcher matcher = pattern.matcher(value);
            if (matcher.find())
            {
                String group = matcher.group(1);
                String defaultText = "";
                if (matcher.groupCount() == 2)
                {
                    defaultText = matcher.group(2);
                }else {
                    defaultText = group;
                }
                group = getProperty(group, defaultText);
                valueNew = Strings.isNullOrEmpty(group) ? defaultText : group;
                if (!Strings.isNullOrEmpty(valueNew))
                {
                    incomingProperties.put(prop, valueNew);
                }
            }
            else
            {
                incomingProperties.put(prop, value);
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
