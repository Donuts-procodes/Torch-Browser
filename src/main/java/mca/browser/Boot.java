package mca.browser;

public class Boot {
    public static void main(String[] args) {
        // This tricks the Java Virtual Machine into bypassing strict JavaFX module checks
        App.main(args);
    }
}