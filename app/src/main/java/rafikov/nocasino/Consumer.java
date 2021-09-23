package rafikov.nocasino;

@FunctionalInterface
public interface Consumer<T> {
    void accept(T t);
}
