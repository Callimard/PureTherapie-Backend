package puretherapie.crm.tool;

public class StringTool {

    private StringTool() {}

    public static String removeRemainingSpaces(String s) {
        if (s != null) {
            char[] chars = s.toCharArray();
            int lastIndex = chars.length - 1;
            for (int i = chars.length - 1; i >= 0; i--) {
                if (Character.isWhitespace(chars[i])) {
                    lastIndex--;
                } else {
                    return s.substring(0, lastIndex + 1);
                }
            }
            return s.substring(0, lastIndex + 1);
        } else
            return null;
    }

}
