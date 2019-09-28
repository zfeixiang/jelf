package net.fornwall.jelf;

import java.io.IOException;

class ElfNoteSection extends ElfSection {

    /**
     * A possible value of the {@link #type} where the description should contain {@link GnuAbiDescriptor}.
     */
    public static final int NT_GNU_ABI_TAG = 1;
    /**
     * A possible value of the {@link #type} for a note containing synthetic hwcap information.
     *
     * The descriptor begins with two words:
     *    word 0: number of entries
     *    word 1: bitmask of enabled entries
     *    Then follow variable-length entries, one byte followed by a '\0'-terminated hwcap name string.  The byte gives the bit
     *    number to test if enabled, (1U << bit) & bitmask.
     */
    public static final int NT_GNU_HWCAP = 2;
    /**
     * A possible value of the {@link #type} for a note containing build ID bits as generated by "ld --build-id".
     *
     * The descriptor consists of any nonzero number of bytes.
     */
    public static final int NT_GNU_BUILD_ID = 3;

    /**
     * A possible value of the {@link #type} for a note containing a version string generated by GNU gold.
     */
    public static final int NT_GNU_GOLD_VERSION = 4;

    /**
     * The descriptor content of a link {@link #NT_GNU_ABI_TAG} type note.
     *
     * Accessible in {@link #descriptorAsGnuAbi()}.
     */
    public final static class GnuAbiDescriptor {

        /** A possible value of {@link #operatingSystem}. */
        public static final int ELF_NOTE_OS_LINUX = 0;
        /** A possible value of {@link #operatingSystem}. */
        public static final int ELF_NOTE_OS_GNU = 1;
        /** A possible value of {@link #operatingSystem}. */
        public static final int ELF_NOTE_OS_SOLARIS2 = 2;
        /** A possible value of {@link #operatingSystem}. */
        public static final int ELF_NOTE_OS_FREEBSD = 3;

        /** One of the ELF_NOTE_OS_* constants in this class. */
        public final int operatingSystem;
        /** Major version of the required ABI. */
        public final int majorVersion;
        /** Minor version of the required ABI. */
        public final int minorVersion;
        /** Subminor version of the required ABI. */
        public final int subminorVersion;

        public GnuAbiDescriptor(int operatingSystem, int majorVersion, int minorVersion, int subminorVersion) {
            this.operatingSystem = operatingSystem;
            this.majorVersion = majorVersion;
            this.minorVersion = minorVersion;
            this.subminorVersion = subminorVersion;
        }
    }

    public final /* uint32_t */ int nameSize;
    public final /* uint32_t */ int descriptorSize;
    public final /* uint32_t */ int type;
    private String name;
    private byte[] descriptorBytes;
    private final GnuAbiDescriptor gnuAbiDescriptor;

    ElfNoteSection(ElfParser parser, ElfSectionHeader header) throws ElfException {
        super(header);

        parser.seek(header.section_offset);
        nameSize = parser.readInt();
        descriptorSize = parser.readInt();
        type = parser.readInt();
        byte[] nameBytes = new byte[nameSize];
        descriptorBytes = new byte[descriptorSize];
        int bytesRead = parser.read(nameBytes);
        if (bytesRead != nameSize) {
            throw new ElfException("Error reading note name (read=" + bytesRead + ", expected=" + nameSize + ")");
        }
        parser.skip(bytesRead % 4);

        switch (type) {
            case NT_GNU_ABI_TAG:
                gnuAbiDescriptor = new GnuAbiDescriptor(parser.readInt(), parser.readInt(), parser.readInt(), parser.readInt());
                break;
            default:
                gnuAbiDescriptor = null;
        }

        bytesRead = parser.read(descriptorBytes);
        if (bytesRead != descriptorSize) {
            throw new ElfException("Error reading note name (read=" + bytesRead + ", expected=" + descriptorSize + ")");
        }

        name = new String(nameBytes, 0, nameSize-1); // unnecessary trailing 0
    }

    String getName() {
        return name;
    }

    byte[] descriptorBytes() {
        return descriptorBytes;
    }

    public String descriptorAsString() {
        return new String(descriptorBytes);
    }

    public GnuAbiDescriptor descriptorAsGnuAbi() {
        return gnuAbiDescriptor;
    }

}
