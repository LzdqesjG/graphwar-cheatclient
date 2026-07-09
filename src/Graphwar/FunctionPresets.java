package Graphwar;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FunctionPresets {
    private Map<String, PresetFunction> presets;
    private List<PresetFunction> presetList;

    public FunctionPresets() {
        presets = new HashMap<>();
        presetList = new ArrayList<>();
        loadPresets();
    }

    private void loadPresets() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("functions.conf"));
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                parseLine(line);
            }

            reader.close();
        } catch (Exception e) {
            System.err.println("Could not load functions.conf: " + e.getMessage());
        }
    }

    private void parseLine(String line) {
        Pattern pattern = Pattern.compile("^(.+)\\(([^)]+)\\)=(.+)\\|(.+)$");
        Matcher matcher = pattern.matcher(line);

        if (matcher.matches()) {
            String namesPart = matcher.group(1);
            String params = matcher.group(2);
            String expression = matcher.group(3);
            String defaults = matcher.group(4);

            String[] names = namesPart.split(",");
            Map<String, String> defaultValues = parseDefaults(defaults);

            PresetFunction preset = new PresetFunction(names, params.split(","), expression, defaultValues);

            presetList.add(preset);

            for (String name : names) {
                presets.put(name.trim().toLowerCase(), preset);
            }
        }
    }

    private Map<String, String> parseDefaults(String defaults) {
        Map<String, String> map = new HashMap<>();

        if (defaults == null || defaults.isEmpty()) {
            return map;
        }

        String[] pairs = defaults.split("\\|");
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                map.put(parts[0].trim(), parts[1].trim());
            }
        }

        return map;
    }

    public String expandFunction(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String result = input;
        boolean changed;

        do {
            changed = false;
            result = result.replace("--", "+");
            result = result.replace("+-", "-");
            result = result.replace("-+", "-");
            result = result.replace("++", "+");
            Pattern callPattern = Pattern.compile("(-?)([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(([^()]*)\\)");
            Matcher matcher = callPattern.matcher(result);

            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String sign = matcher.group(1);
                String funcName = matcher.group(2).toLowerCase();
                String argsStr = matcher.group(3);

                PresetFunction preset = presets.get(funcName);

                if (preset != null) {
                    String expanded = preset.expand(argsStr);
                    if (!sign.isEmpty()) {
                        expanded = sign + "(" + expanded + ")";
                    }
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(expanded));
                    changed = true;
                } else {
                    matcher.appendReplacement(sb, matcher.group(0));
                }
            }
            matcher.appendTail(sb);
            result = sb.toString();
        } while (changed);

        return result;
    }

    public List<PresetFunction> getAllPresets() {
        return presetList;
    }

    public static class PresetFunction {
        String[] names;
        String[] parameters;
        String expression;
        Map<String, String> defaultValues;

        public PresetFunction(String[] names, String[] parameters, String expression, Map<String, String> defaultValues) {
            this.names = names;
            this.parameters = parameters;
            this.expression = expression;
            this.defaultValues = defaultValues;
        }

        public String getName() {
            return names.length > 0 ? names[0].trim() : "";
        }

        public String getSecondAlias() {
            return names.length > 1 ? names[1].trim() : (names.length > 0 ? names[0].trim() : "");
        }

        public String[] getParameters() {
            String[] result = new String[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                result[i] = parameters[i].trim();
            }
            return result;
        }

        public String getParametersString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parameters.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(parameters[i].trim());
            }
            return sb.toString();
        }

        public String getExpression() {
            return expression;
        }

        public String getExpandedExpression() {
            Map<String, String> values = new HashMap<>(defaultValues);
            String result = expression;
            for (String param : parameters) {
                param = param.trim();
                String value = values.getOrDefault(param, param);
                result = result.replace(param, value);
            }
            return result;
        }

        public Map<String, String> getDefaultValues() {
            return defaultValues;
        }

        public String getUnexpandedCall() {
            String alias = getSecondAlias();
            return alias + "(" + getParametersString() + ")";
        }

        public String expand(String argsStr) {
            String[] args = argsStr.split(",");
            Map<String, String> values = new HashMap<>(defaultValues);

            for (int i = 0; i < parameters.length && i < args.length; i++) {
                String arg = args[i].trim();
                if (!arg.isEmpty()) {
                    values.put(parameters[i].trim(), arg);
                }
            }

            String result = expression;
            for (String param : parameters) {
                param = param.trim();
                String value = values.getOrDefault(param, "0");
                result = result.replace(param, value);
            }

            return result;
        }
    }
}