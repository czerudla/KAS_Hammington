package cz.hofmanladislav.kas.hammington;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Main {

    private static boolean s0, s1, s2, s3, sx;
    private static int s; // wrong bit position

    private final static String FILE_NAME = "src/cz/hofmanladislav/kas/hammington/swiss-flag.bmp";
    private final static String ENCODED_FILE_NAME = "src/cz/hofmanladislav/kas/hammington/swiss-flag2.bin";
    private final static String DECODED_FILE_NAME = "src/cz/hofmanladislav/kas/hammington/swiss-flag3.bmp";

    private static BinaryFiles binary = new BinaryFiles();

    public static void main(String[] args) throws IOException {

        readEncodeWrite(FILE_NAME, ENCODED_FILE_NAME);

        readDecodeWrite(ENCODED_FILE_NAME, DECODED_FILE_NAME);

    }

    public static void readEncodeWrite(String fileName, String encodedFileName) throws IOException {
        byte[] bytes = binary.readBinaryFile(fileName);
        byte[] bArray = stringToByteArray(encodeByteArrayToStringBuilder(bytes).toString());// osetrit kolik se na konci pridalo bitu
        binary.writeBinaryFile(bArray, encodedFileName);
    }

    public static void readDecodeWrite(String encodedFileName, String dencodedFileName) throws IOException {
        byte[] bytes = binary.readBinaryFile(encodedFileName);
        StringBuilder code = new StringBuilder();
        for (byte b : bytes) code.append(byteToString(b));// nechat ten byte s udajem o pridanych bitech v kodu a pracovat s nim az po prevodu na String
        for (int i = 0; i < code.length(); i+=13) {
            //TODO vzit 13 bitu, prevest pres short na byte[2], zkontrolovat spravnost, vytahnout z toho jeden byte, vlozit do pole
        }
    }

    private static String byteToString(byte b) {
        // metoda slouzici pro vypis daneho byte do binarniho Stringu
        StringBuilder output = new StringBuilder(Integer.toBinaryString(b));
        if (output.length() > 8)
            return output.substring(24);
        int codeLength = output.length();
        for (int j = 0; j < 8 - codeLength; j++)
            output.insert(0, "0");
        return output.toString();
    }

    private static byte getBitFromByte(byte b, int which) {
        return (byte)((b >> which) & 1); // bitovy posun a logicky soucin, vystupem je byte s hodnotou 0 nebo 1
    }

    private static boolean getBoolFromLowByte(byte b) {
        return b == 1; // pokud je byte roven 1, vrati true, pro 0 vrati false
    }

    private static char boolToCharValue(boolean b) {
        if (b) return 49; // 1
        return 48; // 0
    }

    private static StringBuilder getHammingFromByte(byte b) {
        // Rozsiri byte o parity a zapise do 2byte Short

        boolean a1 = getBoolFromLowByte(getBitFromByte(b, 7));
        boolean a2 = getBoolFromLowByte(getBitFromByte(b, 6));
        boolean a3 = getBoolFromLowByte(getBitFromByte(b, 5));
        boolean a4 = getBoolFromLowByte(getBitFromByte(b, 4));
        boolean a5 = getBoolFromLowByte(getBitFromByte(b, 3));
        boolean a6 = getBoolFromLowByte(getBitFromByte(b, 2));
        boolean a7 = getBoolFromLowByte(getBitFromByte(b, 1));
        boolean a8 = getBoolFromLowByte(getBitFromByte(b, 0));

        boolean p1 = a1 ^ a2 ^ a4 ^ a5 ^ a7;
        boolean p2 = a1 ^ a3 ^ a4 ^ a6 ^ a7;
        boolean p3 = a2 ^ a3 ^ a4 ^ a8;
        boolean p4 = a5 ^ a6 ^ a7 ^ a8;

        boolean px = p1 ^ p2 ^ a1 ^ p3 ^ a2 ^ a3 ^ a4 ^ p4 ^ a5 ^ a6 ^ a7 ^ a8;

        StringBuilder code = new StringBuilder();
        code.append(boolToCharValue(px))
                .append(boolToCharValue(p1))
                .append(boolToCharValue(p2))
                .append(boolToCharValue(a1))
                .append(boolToCharValue(p3))
                .append(boolToCharValue(a2))
                .append(boolToCharValue(a3))
                .append(boolToCharValue(a4))
                .append(boolToCharValue(p4))
                .append(boolToCharValue(a5))
                .append(boolToCharValue(a6))
                .append(boolToCharValue(a7))
                .append(boolToCharValue(a8));

        return code;
    }

    private static byte[] shortToByteArray(short n) {
        ByteBuffer bb = ByteBuffer.allocate(2).putShort(n);
        return bb.array();
    }

    private static short byteArrayToShort(byte[] b) {
        return ByteBuffer.wrap(b).getShort();
    }

    private static byte booleanToByte(boolean b) {
        if (b) return 1;
        return 0;
    }

    private static int getPositionFromSyndroms(boolean s0, boolean s1, boolean s2, boolean s3) {
        // s3 s2 s1 s0, daji dohromady 4 bity a ty pak urci pozici zameneneho bitu
        byte b = booleanToByte(s3);
        b = (byte)(b << 1);
        b += booleanToByte(s2);
        b = (byte)(b << 1);
        b += booleanToByte(s1);
        b = (byte)(b << 1);
        b += booleanToByte(s0);
        return (int)b;
    }

    private static void calculateSyndromsFromHammingCode(byte[] b) {
        // spocita syndromy z kodu ulozeneho v shortu (2 byte)
        boolean cx = getBoolFromLowByte(getBitFromByte(b[0], 4));
        boolean c1 = getBoolFromLowByte(getBitFromByte(b[0], 3));
        boolean c2 = getBoolFromLowByte(getBitFromByte(b[0], 2));
        boolean c3 = getBoolFromLowByte(getBitFromByte(b[0], 1));
        boolean c4 = getBoolFromLowByte(getBitFromByte(b[0], 0));
        boolean c5 = getBoolFromLowByte(getBitFromByte(b[1], 7));
        boolean c6 = getBoolFromLowByte(getBitFromByte(b[1], 6));
        boolean c7 = getBoolFromLowByte(getBitFromByte(b[1], 5));
        boolean c8 = getBoolFromLowByte(getBitFromByte(b[1], 4));
        boolean c9 = getBoolFromLowByte(getBitFromByte(b[1], 3));
        boolean c10 = getBoolFromLowByte(getBitFromByte(b[1], 2));
        boolean c11 = getBoolFromLowByte(getBitFromByte(b[1], 1));
        boolean c12 = getBoolFromLowByte(getBitFromByte(b[1], 0));

        s0 = c1 ^ c3 ^ c5 ^ c7 ^ c9 ^ c11; // XOR
        s1 = c2 ^ c3 ^ c6 ^ c7 ^ c10 ^ c11;
        s2 = c4 ^ c5 ^ c6 ^ c7 ^ c12;
        s3 = c8 ^ c9 ^ c10 ^ c11 ^ c12;

        // CHYBA na tabuli a v zadani, soucasti "sx" musi byt i kontrolni parita "cx"
        sx = cx ^ c1 ^ c2 ^ c3 ^ c4 ^ c5 ^ c6 ^ c7 ^ c8 ^ c9 ^ c10 ^ c11 ^ c12;
    }

    private static byte[] fixBitInCode(byte[] b, int position) {
        // zneguje bit na dane pozici. Kod je 13 bajtu dlouhy -> 5 bitu v hornim byte a 8 v dolnim
        if (position <= 4) {
            if (getBoolFromLowByte(getBitFromByte(b[0], 4-position))) // zjisteni jestli je bit 0 nebo 1, podle toho ->
                b[0] = (byte) (b[0] - (byte)Math.pow(2, 4-position)); // odecitani hodnoty bitu = negace
            else
                b[0] = (byte) (b[0] + (byte)Math.pow(2, 4-position)); // pricteni hodnoty bitu = negace
        } else {
            if (getBoolFromLowByte(getBitFromByte(b[1], Math.abs(position-12))))
                b[1] = (byte) (b[1] - (byte)Math.pow(2, Math.abs(position-12)));
            else
                b[1] = (byte) (b[1] + (byte)Math.pow(2, Math.abs(position-12)));
        }
        return b;
    }

    private static byte[] fixCodeViaSolvingTable(byte[] b) {
        if (s == 0 && !sx) {
            System.out.println("Nebyla zaznamenana chyba");
            return b;
        } else if (s != 0 && !sx) {
            System.out.println("Zaznamenana dvojnasobna chyba");
            return b;
        } else if (s == 0 && sx) {
            System.out.println("Zaznamenana trojnasobna chyba");
            return b;
        } else if (s != 0 && sx) {
            System.out.println("Zaznamenana jednonasobna chyba, opraveno");
            return fixBitInCode(b, s);
        }
        return null;
    }

    private static byte getByteFromHamming(byte[] b) {
        // z 13bitoveho hammingova kodu vybere dane bity a posklada z nich puvodni byte
        boolean a7 = getBoolFromLowByte(getBitFromByte(b[0], 1));
        boolean a6 = getBoolFromLowByte(getBitFromByte(b[1], 7));
        boolean a5 = getBoolFromLowByte(getBitFromByte(b[1], 6));
        boolean a4 = getBoolFromLowByte(getBitFromByte(b[1], 5));
        boolean a3 = getBoolFromLowByte(getBitFromByte(b[1], 3));
        boolean a2 = getBoolFromLowByte(getBitFromByte(b[1], 2));
        boolean a1 = getBoolFromLowByte(getBitFromByte(b[1], 1));
        boolean a0 = getBoolFromLowByte(getBitFromByte(b[1], 0));

        StringBuilder sb = new StringBuilder(boolToCharValue(a7));
        sb.append(boolToCharValue(a6))
                .append(boolToCharValue(a5))
                .append(boolToCharValue(a4))
                .append(boolToCharValue(a3))
                .append(boolToCharValue(a2))
                .append(boolToCharValue(a1))
                .append(boolToCharValue(a0));

        short res = Short.parseShort(sb.toString(), 2);
        byte[] result = shortToByteArray(res);

        return result[1];
    }

    private static StringBuilder encodeByteArrayToStringBuilder(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte aB : b) sb.append(getHammingFromByte(aB));
        return sb;
    }

    private static byte[] stringToByteArray(String s) {
        // Rozparsovani Stringu o tbvaru 1101010111010101.... na pole bajtu
        byte[] output = new byte[s.length()/8+1];
        int i = 0;
        String parse = "";
        int end = 0;
        while (end != s.length()) {
            if (s.length() < i*8+8) end = s.length(); else end = i*8+8; //osetreni konce parsovaneho stringu
            parse = s.substring(i*8, end);
            short a = Short.parseShort(parse, 2); // parsovani shortu ze stringu o tvaru "10101010"
            ByteBuffer bb = ByteBuffer.allocate(2).putShort(a);
            byte[] array = bb.array();
            output[i] = array[1]; // pozadovany byte ulozen v array[1]
            i++;
        }
        return output;
    }


}
