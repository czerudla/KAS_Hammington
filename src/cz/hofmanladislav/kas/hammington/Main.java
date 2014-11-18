package cz.hofmanladislav.kas.hammington;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

public class Main {

    private static boolean s0, s1, s2, s3, sx;
    private static int s; // wrong bit position

    private final static String FILE_NAME = "src/cz/hofmanladislav/kas/hammington/swiss-flag.bmp";
    private final static String ENCODED_FILE_NAME = "src/cz/hofmanladislav/kas/hammington/swiss-flag2.bin";
    private final static String DECODED_FILE_NAME = "src/cz/hofmanladislav/kas/hammington/swiss-flag3.bmp";

    /*private final static String FILE_NAME = "src/cz/hofmanladislav/kas/hammington/soubor.txt";
    private final static String ENCODED_FILE_NAME = "src/cz/hofmanladislav/kas/hammington/soubor2.txt";
    private final static String DECODED_FILE_NAME = "src/cz/hofmanladislav/kas/hammington/soubor3.txt";*/

    private static BinaryFiles binary = new BinaryFiles();

    public static void main(String[] args) throws IOException {

        readEncodeWrite(FILE_NAME, ENCODED_FILE_NAME); // nacteni, zakodovani, zapsani do souboru

        readDecodeWrite(ENCODED_FILE_NAME, DECODED_FILE_NAME); // nacteni, dekodovani, zapsani do souboru

    }

    public static void readEncodeWrite(String fileName, String encodedFileName) throws IOException {
        byte[] bytes = binary.readBinaryFile(fileName); // nacteni souboru do pole bytu
        StringBuilder code = encodeByteArrayToStringBuilderHamming(bytes); // zakodovani do hammingova kodu

        code = generateErrors(code, 1000); // vygenerovani poctu chyb do vysledneho kodu

        byte addedBits = (byte)(8-code.length()%8); // vypocet pridanych bitu do posledniho byte
        byte[] bArray = new byte[code.length()/8+2]; // definice vysledneho pole, ktere bude ulozeno do souboru
        byte[] bCode = stringBuilderToByteArray(code); // prevod hammingovakodu na pole bytu
        bArray[0] = addedBits; // do prvniho byte v souboru je ulozen pocet pridanych bitu do posledniho byte souboru
        for (int i = 0; i < bCode.length; i++) {
            bArray[i+1] = bCode[i]; // prekopirovani pole s hammingovym kodem do vystupniho pole
        }
        binary.writeBinaryFile(bArray, encodedFileName); // zapis vystupniho pole bytu do souboru
    }

    public static void readDecodeWrite(String encodedFileName, String decodedFileName) throws IOException {
        byte[] bytes = binary.readBinaryFile(encodedFileName); // nacteni souboru
        StringBuilder code = loadCodeFromByteArray(bytes); // nacteni kodu do retezce bitu
        code = fixLastByteInLoadedCoode(bytes, code); // oprava posledniho byte o pridane bity

        byte[] output = new byte[(code.length()-8)/13]; // definice vystupniho pole bytu
        for (int i = 8; i+13 <= code.length(); i+=13) { // prochazeni kodu
            byte[] singleCode = shortToByteArray(Short.parseShort(code.substring(i,i+13), 2)); // nactenych 13 bitu prevedeno na byte[2]
            calculateSyndromsFromHammingCode(singleCode); // vypocet syndromu
            s = getPositionFromSyndroms(s0, s1, s2, s3); // vypocet pozice chybneho bitu
            byte[] fixedCode = fixCodeViaSolvingTable(singleCode); // pripadna oprava - dle rozhodovaci tabulky
            output[(i-8)/13] = getByteFromHamming(fixedCode); // prevod z hammingova kodu zpet na jeden byte
        }

        binary.writeBinaryFile(output, decodedFileName); // zapis do souboru
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

    private static byte stringToByte(String s) {
        byte[] bytes = shortToByteArray(Short.parseShort(s, 2));
        return bytes[1];
    }

    private static StringBuilder getHammingFromByte(byte b) {
        // Rozsiri byte o parity a zapise do StringBuilderu jako retezec 13 znaku (0,1)

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
        // spocita syndromy z kodu, dle ni se pak da urcit chybny bit
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

        // CHYBA na tabuli (v zadani), soucasti "sx" musi byt i kontrolni parita "cx"
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

    /*private static byte[] fixBitInCode(byte[] b, int position) {
        // zneguje bit na dane pozici v kodu
        StringBuilder sb = new StringBuilder(byteToString(b[0]) + byteToString(b[1]));
        if (sb.charAt(position) == 49) //1
            sb.replace(position, position+1, "0");
        else
            sb.replace(position, position+1, "1");
        return stringBuilderToByteArray(sb);
    }*/

    private static byte[] fixCodeViaSolvingTable(byte[] b) {
        /* Dle rozhodovaci tabulky opravi chybny bit v kodu
         *      S  Sx
         *      0  0  OK, bez chyby
         *   != 0  0  2x chyba
         *      0  1  3x chyba
         *   != 0  1  1x chyba, mozno opravit chybny bit
         */
        if (s == 0 && !sx) {
            //System.out.println("Nebyla zaznamenana chyba");
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

        StringBuilder sb = new StringBuilder();
        sb.append(boolToCharValue(a7))
                .append(boolToCharValue(a6))
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

    private static StringBuilder encodeByteArrayToStringBuilderHamming(byte[] b) {
        // zakoduje pole bytu do hammingova kodu
        StringBuilder sb = new StringBuilder();
        for (byte aB : b) sb.append(getHammingFromByte(aB));
        return sb;
    }

    private static byte[] stringBuilderToByteArray(StringBuilder s) {
        // projde cely retezec kodu a rozparsuje jej do pole bytu
        byte[] output = new byte[s.length()/8+1];
        int i = 0; // pocatecni pozice parsovani (dale vypocitana)
        int end = 0;// koncova pozice parsovani
        while (end != s.length()) {
            if (s.length() < i*8+8) end = s.length(); else end = i*8+8; //osetreni konce parsovaneho stringu
            String bits = s.substring(i*8, end);
            byte[] array = shortToByteArray(Short.parseShort(bits, 2)); // parsovani shortu ze stringu o tvaru "10101010"
            output[i] = array[1]; // pozadovany byte ulozen v array[1]
            i++;
        }
        return output;
    }

    private static StringBuilder loadCodeFromByteArray(byte[] b) {
        // prevod na retezec bitu bez posledniho byte z pole
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < b.length-1; i++)
            code.append(byteToString(b[i]));
        return code;
    }

    private static StringBuilder fixLastByteInLoadedCoode(byte[] b, StringBuilder code) {
        // opravi posledni bit a pripoji jej ke kodu
        byte addedBits = stringToByte(code.substring(0,8)); // pocet pridanych bitu do posledniho byte
        byte lastByte = b[b.length-1]; // posledni byte ze souboru
        for (int i = 0; i < addedBits; i++)
            lastByte = (byte)(lastByte << 1); // bitovy posun o pridane bity
        code.append(byteToString(lastByte)); // pripojeni k retezci bitu
        return code;
    }

    private static StringBuilder generateErrors(StringBuilder code, int frequency) {
        // vygeneruje chyby v kodu. frequency oznacuje pocet chyb
        Random rand = new Random();

        for (int i = 0; i < frequency; i++) {
            int changedBit = rand.nextInt(code.length());
            if (code.charAt(changedBit) == 49) // 1
                code.replace(changedBit, changedBit+1, "0");
            else code.replace(changedBit, changedBit+1, "1");
        }
        return code;
    }
}
