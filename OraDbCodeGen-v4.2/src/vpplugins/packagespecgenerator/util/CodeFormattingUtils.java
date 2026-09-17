package vpplugins.packagespecgenerator.util;

import org.apache.log4j.Logger;

public class CodeFormattingUtils {
   static final Logger log = Logger.getLogger(CodeFormattingUtils.class);


   /**
    * Formats a string into lines of fixed width without breaking words,
    * preserves newlines and indentation, front-pads each line,
    * and additionally indents wrapped lines of numbered lists.
    *
    * @param input      the original string
    * @param width      the maximum width of each line (excluding padding and indent)
    * @param padSpaces  number of spaces to add at the beginning of each line
    * @param preLine    Insert just in front of the indented text for each line (eg. -- for PLSQL comments)
    * @return formatted string with preserved structure and styled lists
    */
   public static String docWordWrap(String input, int width, int padSpaces, String preLine) {

         StringBuilder result = new StringBuilder();
         String pad = "";
         if (padSpaces != 0) {
            pad = " ".repeat(padSpaces-preLine.length());
         }

         String[] lines = input.split("\n", -1);

         for (String rawLine : lines) {
            if (rawLine.trim().isEmpty()) {
               result.append("\n");
               continue;
            }

            // Extract leading spaces
            int leadingSpaces = rawLine.indexOf(rawLine.trim());
            String indent = rawLine.substring(0, leadingSpaces);

            String trimmedLine = rawLine.trim();

            // Check for numbered list pattern (e.g. "1. something")
            String numberPrefix = "";
            String hangingIndent = "";
            if (trimmedLine.matches("^\\d+\\.\\s+.*")) {
               int dotIndex = trimmedLine.indexOf('.') + 1;
               numberPrefix = trimmedLine.substring(0, dotIndex).trim();
               trimmedLine = trimmedLine.substring(dotIndex).trim();
               hangingIndent = " ".repeat(numberPrefix.length() + 1); // for hanging indent on wrapped lines
            }

            String[] words = trimmedLine.split("\\s+");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
               if (currentLine.length() + word.length() + 1 > width) {
                     result.append(pad).append(indent);
                     if (!numberPrefix.isEmpty()) {
                        result.append(hangingIndent);
                     }
                     result.append(preLine).append(currentLine.toString().stripTrailing()).append("\n");
                     currentLine.setLength(0);
               }

               if (currentLine.length() == 0 && !numberPrefix.isEmpty()) {
                     currentLine.append(numberPrefix).append(" ").append(word).append(" ");
                     numberPrefix = ""; // only prefix the first line
               } else {
                     currentLine.append(word).append(" ");
               }
            }

            if (currentLine.length() > 0) {
               result.append(pad).append(indent);
               if (!numberPrefix.isEmpty()) {
                     result.append(hangingIndent);
               }
               result.append(preLine).append(currentLine.toString().stripTrailing()).append("\n");
            }
         }

         // Remove trailing newline
         if (result.length() > 0 && result.charAt(result.length() - 1) == '\n') {
            result.setLength(result.length() - 1);
         }

         return result.toString();
   }

   /**
    * Formats a string into lines of fixed width without breaking words,
    * preserves newlines and indentation, front-pads each line,
    * and additionally indents wrapped lines of numbered lists.
    *
    * Overloaded form of docWordWrap, for instances in which no preLine or postLine are specified
    *
    * @param input      the original string
    * @param width      the maximum width of each line (excluding padding and indent)
    * @param padSpaces  number of spaces to add at the beginning of each line
    * @return           formatted string with preserved structure and styled lists
    */
    public static String docWordWrap(String input, int width, int padSpaces) {
      return docWordWrap(input, width, padSpaces, "");
    }   

   /**
    * Left-pads each line of the input string with a specified number of spaces.
    * Preserves existing line breaks and indentation.
    *
    * @param input      the input string (can be multiline)
    * @param padSpaces  the number of spaces to add at the beginning of each line
    * @return the padded string
    */
   public static String lPad(String input, int padSpaces) {
      String pad = " ".repeat(Math.max(0, padSpaces));
      StringBuilder result = new StringBuilder();

      String[] lines = input.split("\n", -1); // -1 keeps trailing empty lines

      for (int i = 0; i < lines.length; i++) {
            result.append(pad).append(lines[i]);
            if (i < lines.length - 1) {
               result.append("\n");
            }
      }

      return result.toString();
   }   

   
   /**
    * Formats a string so each line is exactly totalWidth characters:
    * - Pads shorter lines with spaces on the right
    * - Truncates longer lines
    * - Preserves line breaks
    *
    * @param input       the input string (may contain multiple lines)
    * @param totalWidth  the exact width for each line
    * @return formatted string with lines of uniform width
    */
   public static String rPad(String input, int totalWidth) {
      StringBuilder result = new StringBuilder();
      String[] lines = input.split("\n", -1); // keep trailing lines

      for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // Truncate if too long
            if (line.length() > totalWidth) {
               line = line.substring(0, totalWidth);
            }
            // Pad if too short
            else if (line.length() < totalWidth) {
               line += " ".repeat(totalWidth - line.length());
            }
            result.append(line);
            if (i < lines.length - 1) {
               result.append("\n");
            }
      }
      return result.toString();
   }   




   public static String covertDbDataTypeToJavaDataType(String DbType) {
      String FrontEndType;
      if (DbType.indexOf("char") == -1 && DbType.indexOf("varchar") == -1 && DbType.indexOf("varchar2") == -1 && DbType.indexOf("nclob") == -1 && DbType.indexOf("nchar") == -1) {
         if (DbType.indexOf("number") == -1 && DbType.indexOf("long") == -1 && DbType.indexOf("binary_float") == -1 && DbType.indexOf("binary_double") == -1) {
            if (DbType.indexOf("date") != -1) {
               FrontEndType = "(Date)";
            } else if (DbType.indexOf("timestamp with time zone") != -1) {
               FrontEndType = "(Timestamp w/ Time Zone)";
            } else if (DbType.indexOf("timestamp") != -1) {
               FrontEndType = "(Timestamp)";
            } else if (DbType.indexOf("blob") != -1) {
               FrontEndType = "(Blob)";
            } else {
               FrontEndType = "(Null)";
            }
         } else {
            FrontEndType = "(Integer)";
         }
      } else {
         FrontEndType = "(String)";
      }

      return FrontEndType;
   }

   public static String wrapLiteralValueByDataType(String value, String dataType) {
      return value;
   }

    /**
     * Removes trailing spaces and newlines from the end of a string.
     *
     * @param input the original string
     * @return the trimmed string with no trailing spaces or newlines
     */
    public static String stripTrailingSpacesAndNewlines(String input) {
      if (input == null) return null;
      return input.replaceAll("[ \t\r\n]+$", ".");
  }   
}
