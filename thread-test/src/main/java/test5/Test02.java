package test5;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Test02 {
    public static void main(String[] args) {
        Consumer<String> consumer = s->{
            System.out.println(s);
        };
        consumer.accept("123");

        Supplier<String> supplier = ()->{
            return  "hello";
        };

    }
}
