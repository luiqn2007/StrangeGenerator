package lq2007.mod.strangegenerator.util;

public class ByteUtils {

    public static byte pack(boolean... values) {
        if (values.length == 0) return 0;
        if (values.length == 1) return (byte) (values[0] ? 1 : 0);
        if (values.length > 8) {
            throw new NumberFormatException("Byte can only save 7 boolean values at most");
        }
        byte b = 0;
        if (values[0]) {
            b |= 0b1;
        }
        for (int i = 1; i < values.length; i++) {
            b <<= 1;
            if (values[i]) {
                b |= 0b1;
            }
        }
        return b;
    }

    public static boolean[] unpack(byte b) {
        return unpack(b, 8);
    }

    public static boolean[] unpack(byte b, int count) {
        if (count > 8) {
            throw new NumberFormatException("Byte can only save 7 boolean values at most");
        }
        boolean[] result = new boolean[count];
        for (int i = 0; i < count; i++) {
            result[i] = check(b, i);
        }
        return result;
    }

    public static boolean check(byte b, int position) {
        if (position >= 8) {
            throw new NumberFormatException("Byte can only save 7 boolean values at most");
        }
        byte b1 = (byte) (0b1 << position);
        return (b & b1) == b1;
    }
}
