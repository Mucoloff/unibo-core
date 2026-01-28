package dev.sweety.unibo.utils;

import dev.sweety.core.util.ObjectUtils;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class ColorUtils {

    public final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    public final char COLOR_CHAR = '\u00A7';

    public String color(Object o) {
        if (o == null) return "";
        String message = (o instanceof String m) ? m : o.toString();
        if (ObjectUtils.isNull(message)) return "";
        return colorize(translateHexColorCodes(message));
    }

    private String colorize(final String message) {
        char[] b = message.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1) {
                b[i] = COLOR_CHAR;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }

    private String translateHexColorCodes(final String message) {

        final Matcher matcher = HEX_PATTERN.matcher(message);
        final StringBuilder buffer = new StringBuilder(message.length() + 4 * 8);

        while (matcher.find()) {
            final String group = matcher.group(1);

            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5));
        }

        return matcher.appendTail(buffer).toString();
    }

    public static List<String> colorList(@NotNull List<String> list) {
        return list.stream().map(ColorUtils::color).toList();
    }
}
