package com.databasesandlife.util;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.databasesandlife.util.gwtsafe.ConfigurationException;

public class PlaintextParameterReplacer {

    /** Replaces variables such as ${XYZ} in the template. Variables which are not found remain in their original unreplaced form. */
    public static String replacePlainTextParameters(String template, Map<String,String> parameters) {
        for (Entry<String,String> paramEntry : parameters.entrySet())
            template = template.replace("${" + paramEntry.getKey() + "}", paramEntry.getValue());
        return template;
    }

    public static String replacePlainTextParametersWithBlanks(String template) {
        return template.replaceAll("\\$\\{([\\w-]+)\\}", "");
    }

    public static void assertParametersSuffice(Collection<String> params, CharSequence template, String msg) throws ConfigurationException {
        if (template == null) return;
        Matcher m = Pattern.compile("\\$\\{([\\w-]+)\\}").matcher(template);
        while (m.find())
            if ( ! params.contains(m.group(1)))
                throw new ConfigurationException(msg+": Pattern '"+template+"' contains parameter ${"+m.group(1)+"} but it is not available; "
                    + "available parameters are "+params.stream().map(p -> "${"+p+"}").collect(Collectors.joining(", ")));
    }
}
