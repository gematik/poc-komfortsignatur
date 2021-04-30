package de.gematik.rezeps;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/** Hilfsklasse zur persistenten Verwaltung der UserID. */
public class UserIdHelper {

  private static final String USER_ID_FILE_NAME = "rezeps_user_id.txt";
  protected static final File USER_ID_FILE =
      new File(
          System.getProperty("java.io.tmpdir") + File.separatorChar + USER_ID_FILE_NAME); // NOSONAR

  private UserIdHelper() {}

  /** Schreibt die UserID in eine temporäre Datei. */
  public static synchronized void writeUserIdToFile(String userId) throws IOException {
    deleteUserIdFile();
    boolean wasCreatedSuccessfully = USER_ID_FILE.createNewFile();
    if (wasCreatedSuccessfully) {
      Path path = Paths.get(USER_ID_FILE.toURI());
      Files.write(path, userId.getBytes(StandardCharsets.UTF_8));
    }
  }

  /** Löscht die temporäre Datei, in der die UserID gespeichert wird, falls diese vorhanden ist. */
  public static synchronized void deleteUserIdFile() throws IOException {
    if (USER_ID_FILE.exists()) {
      Files.delete(USER_ID_FILE.toPath());
    }
  }

  /**
   * Liest die User-ID aus einer temporären Datei.
   *
   * @return Die User-ID.
   * @throws IOException Falls beim Lesen ein Fehler auftritt.
   */
  public static synchronized String readUserIdFromFile() throws IOException {
    String userId = null;
    if (USER_ID_FILE.exists()) {
      Path path = Paths.get(USER_ID_FILE.toURI());
      List<String> allLines = Files.readAllLines(path);
      if (allLines != null && !allLines.isEmpty()) {
        userId = allLines.get(0);
      }
    }
    return userId;
  }
}
