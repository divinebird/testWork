import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Main {

    final static int CARD_WIDTH = 64;
    final static int CARD_HEIGHT = 87;

    final static int[] CARD_FIELD_START_X = {143, 215, 286, 358, 429};
    final static int CARD_FIELD_START_Y = 586;

    final static int BACK_TEST_X = 44;
    final static int BACK_TEST_Y = 13;

    final static BufferedImage[] RATING;
    final static String[] RATINGS_NAME = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
    final static BufferedImage[] SUIT;
    final static String[] SUIT_NAME = {"d", "h", "c", "s"};

    static {
        RATING = new BufferedImage[RATINGS_NAME.length];
        SUIT = new BufferedImage[SUIT_NAME.length];
        try {
            ClassLoader classLoader = Main.class.getClassLoader();
            for (int i = 0; i < RATING.length; i++)
                RATING[i] = ImageIO.read(classLoader.getResourceAsStream("rating" + i + ".png"));
            for (int i = 0; i < SUIT.length; i++)
                SUIT[i] = ImageIO.read(classLoader.getResourceAsStream("suit" + i + ".png"));
        } catch (Exception e) {
            System.err.println("Template images does not inited! Error: " + e.getLocalizedMessage());
            System.exit(1);
        }
    }

    private static void normalize(BufferedImage img, int backColor) {
        for (int i = 0; i < img.getHeight(); i++) {
            for (int j = 0; j < img.getWidth(); j++) {
                int pColor = img.getRGB(j, i);
                if (pColor == backColor)
                    img.setRGB(j, i, Color.WHITE.getRGB());
                else
                    img.setRGB(j, i, 0);
            }
        }
    }

    private static int getIndex(BufferedImage img, BufferedImage[] templArr) {
        int sum = 0;
        int index = -1;
        for (int k = 0; k < templArr.length; k++) {
            int lSum = 0;
            for (int i = 0; i < img.getHeight(); i++) {
                for (int j = 0; j < img.getWidth(); j++) {
                    if (img.getRGB(j, i) == templArr[k].getRGB(j, i))
                        lSum++;
                }
                if (sum < lSum) {
                    sum = lSum;
                    index = k;
                }
            }
        }
        return index;
    }

    private static void processFile(Path file) {
        if (!file.getFileName().toString().toLowerCase().endsWith(".png"))
            return;

        BufferedImage fullImg;
        try {
            fullImg = ImageIO.read(file.toFile());
        } catch (IOException e) {
            return;
        }

        System.out.print(file.getFileName());
        System.out.print(" - ");
        for (int i = 0; i < 5; i++) {
            BufferedImage card = fullImg.getSubimage(CARD_FIELD_START_X[i], CARD_FIELD_START_Y, CARD_WIDTH, CARD_HEIGHT);
            Color backColor = new Color(card.getRGB(BACK_TEST_X, BACK_TEST_Y));
            if (backColor.getRed() < 100 || backColor.getGreen() < 100 || backColor.getBlue() < 100)
                break;
            BufferedImage rating = card.getSubimage(7, 4, 27, 27);
            BufferedImage suit = card.getSubimage(25, 47, 34, 35);
            normalize(rating, backColor.getRGB());
            normalize(suit, backColor.getRGB());
            System.out.print(RATINGS_NAME[getIndex(rating, RATING)]);
            System.out.print(SUIT_NAME[getIndex(suit, SUIT)]);
        }

        System.out.println();
    }

    public static void main(String[] args) throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get(args[0]))) {
            paths.filter(Files::isRegularFile).forEach(Main::processFile);
        }
    }
}
