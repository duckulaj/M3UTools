
package com.hawkins.m3utoolsjpa.utils;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.List;

import com.hawkins.m3utoolsjpa.data.M3UItem;

public class Writer {

    private static final String M3U_START_MARKER = "#EXTM3U";
    private static final String M3U_INFO_MARKER = "#EXTINF:";
    private static final String DURATION_PATTERN = "{0} ";
    private static final String TVG_ID_PATTERN = "tvg-id=\"{0}\" ";
    private static final String TVG_CHNO_PATTERN = "tvg-chno=\"{0}\" ";
    private static final String TVG_NAME_PATTERN = "tvg-name=\"{0}\" ";
    private static final String TVG_LOGO_PATTERN = "tvg-logo=\"{0}\" ";
    private static final String TVG_SHIFT_PATTERN = "tvg-shift=\"{0}\" ";
    private static final String GROUP_TITLE_PATTERN = "group-title=\"{0}\" ";
    private static final String RADIO_PATTERN = "radio=\"{0}\" ";
    private static final String CHANNEL_NAME_PATTERN = ",{0}";

    public static void write(List<M3UItem> entries, OutputStream stream) {
        try (PrintWriter pw = new PrintWriter(stream)) {
            pw.println(M3U_START_MARKER);

            StringBuilder builder = new StringBuilder();
            for (M3UItem entry : entries) {
                builder.setLength(0); // Clear the builder
                builder.append(M3U_INFO_MARKER);
                appendIfNotNull(builder, entry.getTvgChNo(), TVG_CHNO_PATTERN);
                appendIfNotNull(builder, StringUtils.removeCountryIdentifier(entry.getTvgName()), TVG_NAME_PATTERN);
                appendIfNotNull(builder, entry.getTvgId(), TVG_ID_PATTERN);
                appendIfNotNull(builder, entry.getTvgLogo(), TVG_LOGO_PATTERN);
                appendIfNotNull(builder, entry.getDuration(), DURATION_PATTERN);
                appendIfNotNull(builder, entry.getTvgShift(), TVG_SHIFT_PATTERN);
                appendIfNotNull(builder, entry.getRadio(), RADIO_PATTERN);
                appendIfNotNull(builder, entry.getDuration(), DURATION_PATTERN);
                appendIfNotNull(builder, entry.getGroupTitle(), GROUP_TITLE_PATTERN);
                builder.deleteCharAt(builder.length() - 1);
                appendIfNotNull(builder, StringUtils.removeCountryIdentifier(entry.getChannelName()), CHANNEL_NAME_PATTERN);
                builder.append(System.lineSeparator());
                builder.append(entry.getChannelUri());
                if (entries.indexOf(entry) != entries.size() - 1) {
                    builder.append(System.lineSeparator());
                }
                pw.print(builder.toString());
            }
        }
    }

    private static void appendIfNotNull(StringBuilder sb, String toWrite, String pattern) {
        if (toWrite != null) {
            sb.append(MessageFormat.format(pattern, toWrite));
        }
    }
}
