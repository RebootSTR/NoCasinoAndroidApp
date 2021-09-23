package rafikov.nocasino;

@FunctionalInterface
public interface BiConsumer<T1, T2> {
    void accept(T1 t1, T2 t2);
}
