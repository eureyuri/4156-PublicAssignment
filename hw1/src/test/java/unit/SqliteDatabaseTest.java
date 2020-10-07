package unit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import utils.SqliteDatabase;

public class SqliteDatabaseTest {
  
  @Test
  public void testDbContructor() {
    try {
      SqliteDatabase db = new SqliteDatabase();
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }
  
  @Test
  public void testAddGameBoardData() {
//    SqliteDatabase db = new SqliteDatabase();
  }


}
