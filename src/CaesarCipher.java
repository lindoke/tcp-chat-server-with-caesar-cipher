import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CaesarCipher {


    public String encode(String message, int offset) {
        char[] arrayOfSymbols = message.toCharArray();
        for (int i = 0; i < arrayOfSymbols.length; i++) {
            arrayOfSymbols[i] += offset;
        }
        return new String(arrayOfSymbols);
    }

    public char[] decode(String encodedMessage) throws IOException {
        char[] arrayOfSymbols = encodedMessage.toCharArray();

        //offset is unknown
        //finding first word in message from array with words, taken from file
        List<String> listOfStrings
                = new ArrayList<String>();

        BufferedReader bf = new BufferedReader(
                new FileReader("words.txt"));

        String line = bf.readLine();

        while (line != null) {
            listOfStrings.add(line);
            line = bf.readLine();
        }

        bf.close();

        String[] array = listOfStrings.toArray(new String[0]);
        for (int j = 0; j < 256; j++) {
            for (int i = 0; i < arrayOfSymbols.length; i++) {
                arrayOfSymbols[i] -= 1;
            }
            String[] phrase = String.valueOf(arrayOfSymbols).replaceAll("[!?,]", "").split(" ");
            for (var word : array) {
                if (Arrays.stream(phrase).anyMatch(word::equalsIgnoreCase)) {
                    return arrayOfSymbols;
                }
            }
        }
        return null;
    }
}

