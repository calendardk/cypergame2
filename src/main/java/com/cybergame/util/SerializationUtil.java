package com.cybergame.util;

import java.io.*;
import java.util.Base64;

public class SerializationUtil {

    public static String serialize(Object obj) {
        try (
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos)
        ) {
            oos.writeObject(obj);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialize(String data) {
        try (
            ByteArrayInputStream bais =
                new ByteArrayInputStream(Base64.getDecoder().decode(data));
            ObjectInputStream ois = new ObjectInputStream(bais)
        ) {
            return (T) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
