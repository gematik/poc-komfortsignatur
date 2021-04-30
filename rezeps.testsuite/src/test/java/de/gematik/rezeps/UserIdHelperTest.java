package de.gematik.rezeps;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserIdHelperTest {

  private static final String USER_ID = "1234567890";
  private static final Logger LOGGER = LoggerFactory.getLogger(UserIdHelper.class);

  @Test
  public void shouldWriteUserIdToFile() throws IOException {
    ensureUserIdFileDoesNotExist();
    UserIdHelper.writeUserIdToFile(USER_ID);
    String userId = UserIdHelper.readUserIdFromFile();
    Assert.assertEquals(USER_ID, userId);
  }

  private void ensureUserIdFileDoesNotExist() {
    if (UserIdHelper.USER_ID_FILE.exists()) {
      boolean wasDeletedSuccessfully = UserIdHelper.USER_ID_FILE.delete();
      Assert.assertTrue("User ID file could not be deleted", wasDeletedSuccessfully);
    }
  }

  @Test
  public void shouldOverwriteUserIdInFile() throws IOException {
    UserIdHelper.writeUserIdToFile("0987654321");
    UserIdHelper.writeUserIdToFile(USER_ID);
    String userId = UserIdHelper.readUserIdFromFile();
    Assert.assertEquals(USER_ID, userId);
  }

  @Test
  public void shouldReturnNullOnReadWhenUserIdFileDoesNotExist() {
    ensureUserIdFileDoesNotExist();
    String userId = null;
    try {
      userId = UserIdHelper.readUserIdFromFile();
    } catch (IOException ioException) {
      LOGGER.error(ioException.getMessage());
    }
    Assert.assertNull(userId);
  }
}
