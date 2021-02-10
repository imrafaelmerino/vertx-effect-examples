package vertx.effect.examples.slides.vertxeffectintro;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Generators {

    public static String strGen(int length) {
        return Stream.generate(randomChar)
                     .limit(length)
                     .reduce("",
                             (a, b) -> a + b);

    }

    public static int intGen(int max) {
        return ThreadLocalRandom.current()
                                .nextInt(0,
                                         max);
    }

    static String letters = "abcdefghijklmnopqrstuvwzyz";
    public static final Supplier<String> randomChar = () -> {
        return String.valueOf(letters.charAt(intGen(letters.length())));
    };
}
