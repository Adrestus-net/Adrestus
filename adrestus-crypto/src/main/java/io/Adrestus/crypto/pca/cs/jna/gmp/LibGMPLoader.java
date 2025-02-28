package io.Adrestus.crypto.pca.cs.jna.gmp;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import lombok.Getter;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

@Getter
public final class LibGMPLoader {
    private static volatile LibGMPLoader instance;

    private final LibGMP gmp;
    private final SizeT8 sizeT8;
    private final SizeT4 sizeT4;
    private final String fileName;


    /**
     * private constructor to prevent client from instantiating.
     */
    private LibGMPLoader() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.fileName = LibGMPHelper.getFileName();
        this.gmp = Native.load(fileName, LibGMP.class);
        this.sizeT8 = Native.load(fileName, SizeT8.class);
        this.sizeT4 = Native.load(fileName, SizeT4.class);
    }

    public static LibGMPLoader getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (LibGMPLoader.class) {
                result = instance;
                if (result == null) {
                    result = new LibGMPLoader();
                    instance = result;
                }
            }
        }
        return result;
    }

    public Library getSize_T() {
        return switch (Native.SIZE_T_SIZE) {
            case 4 -> sizeT4;
            case 8 -> sizeT8;
            default -> throw new AssertionError("Unexpected Native.SIZE_T_SIZE: " + Native.SIZE_T_SIZE);
        };
    }

    public void cleanup() {
        NativeLibrary lib = NativeLibrary.getInstance(this.fileName, LibGMP.class.getClassLoader());
        NativeLibrary sizeT8 = NativeLibrary.getInstance(this.fileName, SizeT8.class.getClassLoader());
        NativeLibrary sizeT4 = NativeLibrary.getInstance(this.fileName, SizeT4.class.getClassLoader());
        lib.close();
        sizeT8.close();
        sizeT4.close();
        clearJNACache();
        instance = null;
    }

    private void clearJNACache() {
        try {
            Field librariesField = NativeLibrary.class.getDeclaredField("libraries");
            librariesField.setAccessible(true);
            ((Map<?, ?>) librariesField.get(null)).clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final class LibGMPHelper {

        public static String getFileName() {
            return SystemUtils.IS_OS_LINUX ? new File(Objects.requireNonNull(LibGMPLoader.class.getResource("/libgmp.so")).getFile()).getName() : new File(LibGMPLoader.class.getResource("/libgmp.dll").getFile()).getName();
        }

        public static int readSizeT(Pointer ptr) {
            return switch (Native.SIZE_T_SIZE) {
                case 4 -> {
                    int result = ptr.getInt(0);
                    assert result >= 0;
                    yield result;
                }
                case 8 -> {
                    long result = ptr.getLong(0);
                    assert result >= 0;
                    assert result < Integer.MAX_VALUE;
                    yield (int) result;
                }
                default -> throw new AssertionError("Unexpected Native.SIZE_T_SIZE: " + Native.SIZE_T_SIZE);
            };
        }
    }
}
