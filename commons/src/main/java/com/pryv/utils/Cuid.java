package com.pryv.utils;

import java.lang.management.ManagementFactory;
import java.util.Date;

/**
 * Generates collision-resistant unique ids.
 */
public class Cuid {
    private static final int BASE = 36;
    private static final int BLOCK_SIZE = 4;
    private static final int DISCRETE_VALUES = (int) Math.pow(BASE, BLOCK_SIZE);
    private static final String LETTER = "c";

    private static final String FINGERPRINT;

    static {
        FINGERPRINT = getFingerprint();
    }

    private static int counter = 0;

    // TODO: make this crossplatform
    private static String getHostInfo(String idFallback, String nameFallback) {
        String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final int index = jvmName.indexOf('@');
        if (index < 1) {
            return String.format("%s@%s", idFallback, nameFallback);
        }

        return jvmName;
    }

    private static String getFingerprint() {
        // TODO: adapt this according to crossplatform getHostInfo
        String hostInfo = getHostInfo(Long.toString(new Date().getTime()), "dummy-host");
        String hostId = hostInfo.split("@")[0];
        String hostname = hostInfo.split("@")[1];

        int acc = hostname.length() + BASE;
        for (int i = 0; i < hostname.length(); i++) {
            acc += acc + (int) hostname.charAt(i);
        }

        String idBlock = pad(Long.toString(Long.parseLong(hostId), BASE), 2);
        String nameBlock = pad(Integer.toString(acc), 2);

        return idBlock + nameBlock;
    }

    private static String pad(String input, int size) {
        // See: http://stackoverflow.com/a/4903603/1176596
        String repeatedZero = new String(new char[size]).replace("\0", "0");
        String padded = repeatedZero + input;
        return (padded).substring(padded.length() - size);
    }

    private static String getRandomBlock() {
        return pad(Integer.toString((int) (Math.random() * DISCRETE_VALUES), BASE), BLOCK_SIZE);
    }

    private static int safeCounter() {
        counter = counter < DISCRETE_VALUES ? counter : 0;
        return counter++;
    }

    /**
     * Generates collision-resistant unique ids.
     *
     * @return a collision-resistant unique id
     */
    public static String createCuid() {
        String timestamp = Long.toString(new Date().getTime(), BASE);
        String counter = pad(Integer.toString(safeCounter(), BASE), BLOCK_SIZE);
        String random = getRandomBlock() + getRandomBlock();

        return LETTER + timestamp + counter + FINGERPRINT + random;
    }
}