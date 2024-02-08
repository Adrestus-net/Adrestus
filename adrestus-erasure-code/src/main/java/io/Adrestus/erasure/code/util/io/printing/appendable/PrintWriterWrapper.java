package io.Adrestus.erasure.code.util.io.printing.appendable;


import java.io.IOException;
import java.io.PrintWriter;


/**
 * @param <P>
 * @author Ricardo Fonseca &lt;rfonseca&#064;lasige.di.fc.ul.pt&gt;
 */
final class PrintWriterWrapper<P extends PrintWriter> extends WriterWrapper<P> {

    public PrintWriterWrapper(P pw) {

        super(pw);
    }

    @Override
    public void println() {

        appendable.println();
    }

    @Override
    public void println(char c) {

        appendable.println(c);
    }

    @Override
    public void println(char[] c) {

        appendable.println(c);
    }

    @Override
    public void println(CharSequence csq) throws IOException {

        if (csq instanceof String) {
            appendable.println((String) csq);
        } else {
            super.println(csq);
        }
    }

    @Override
    public void printf(String format, Object... args) {

        appendable.printf(format, args);
    }
}
