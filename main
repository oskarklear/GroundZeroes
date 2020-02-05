import java.io.File;
import java.io.PrintStream;
import java.util.*;
public class GroundZeroes
{
  public static final boolean CAN_SAVE = false;
  public static final String FILE_EXTENSION = ".gz";
  
  public static void main(String[] args) throws InterruptedException
  {
    System.out.println("Game saving has been disabled in this version.\n");
    Game newGame = new Game();
    newGame.play();
  }
  
  private static void listFiles()
  {
    System.out.println("\nExisting Game Saves:");
    File f = new File(System.getProperty("user.dir"));
    File[] fileArray = f.listFiles();
    for (File file : fileArray) {
      if ((file != null) && (file.getName().endsWith(".sfsilentstorm"))) {
        System.out.println(file.getName().substring(0, file.getName().length() - ".sfsilentstorm".length()));
      }
    }
  }
}
